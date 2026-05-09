package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.MatchResultRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.TournamentTeamRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.BracketResponse;
import com.volleyball.volleyballcommunitybackend.entity.Event;
import com.volleyball.volleyballcommunitybackend.entity.EventRegistration;
import com.volleyball.volleyballcommunitybackend.entity.TournamentMatch;
import com.volleyball.volleyballcommunitybackend.repository.EventRegistrationRepository;
import com.volleyball.volleyballcommunitybackend.repository.EventRepository;
import com.volleyball.volleyballcommunitybackend.repository.TournamentMatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TournamentService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final TournamentMatchRepository matchRepository;

    public TournamentService(EventRepository eventRepository,
                             EventRegistrationRepository registrationRepository,
                             TournamentMatchRepository matchRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
    }

    // ==================== 位置分配 ====================

    /**
     * 为新报名的队伍分配 bracket 位置
     * 分散配对策略：保证先报名的队伍尽量分散到不同的首轮对
     */
    public int assignBracketPosition(Long eventId, int bracketSize) {
        List<EventRegistration> existing = registrationRepository.findByEventId(eventId);
        Set<Integer> takenPositions = existing.stream()
                .map(EventRegistration::getBracketPosition)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Integer> fillOrder = getDispersedFillOrder(bracketSize);
        for (int pos : fillOrder) {
            if (!takenPositions.contains(pos)) {
                return pos;
            }
        }
        throw new RuntimeException("报名已满，无可用位置");
    }

    /**
     * 分散填充顺序：8位 → [0, 7, 1, 6, 2, 5, 3, 4]
     * 确保先报名的队伍分散到不同对
     */
    private List<Integer> getDispersedFillOrder(int bracketSize) {
        List<Integer> order = new ArrayList<>();
        int left = 0;
        int right = bracketSize - 1;
        while (left <= right) {
            order.add(left);
            if (left != right) {
                order.add(right);
            }
            left++;
            right--;
        }
        return order;
    }

    // ==================== 报名时实时更新对阵图 ====================

    /**
     * 报名/添加队伍后，确保首轮 match 存在并填入该队伍
     * 同时预创建后续轮次的空 match（仅首次调用时）
     */
    @Transactional
    public void ensureFirstRoundMatch(Long eventId, int bracketSize, int position, Long registrationId) {
        int totalRounds = (int) (Math.log(bracketSize) / Math.log(2));
        int firstRoundMatches = bracketSize / 2;
        List<TournamentMatch> firstRound = matchRepository.findByEventIdAndRound(eventId, 1);

        if (firstRound.isEmpty()) {
            // 首次报名，预创建所有轮次的空 match
            for (int round = totalRounds; round >= 1; round--) {
                int matchesInRound = bracketSize / (int) Math.pow(2, round);
                for (int order = 0; order < matchesInRound; order++) {
                    TournamentMatch match = new TournamentMatch();
                    match.setEventId(eventId);
                    match.setRound(round);
                    match.setMatchOrder(order);
                    match.setPhase("KNOCKOUT");
                    match.setStatus("PENDING");
                    matchRepository.save(match);
                }
            }

            // 设置 nextMatchId / nextMatchSlot 关系
            List<List<TournamentMatch>> allRounds = new ArrayList<>();
            for (int round = 1; round <= totalRounds; round++) {
                allRounds.add(matchRepository.findByEventIdAndRound(eventId, round));
            }
            for (int roundIdx = 0; roundIdx < allRounds.size() - 1; roundIdx++) {
                List<TournamentMatch> currentRound = allRounds.get(roundIdx);
                List<TournamentMatch> nextRound = allRounds.get(roundIdx + 1);
                for (int i = 0; i < currentRound.size(); i++) {
                    TournamentMatch match = currentRound.get(i);
                    TournamentMatch nextMatch = nextRound.get(i / 2);
                    match.setNextMatchId(nextMatch.getId());
                    match.setNextMatchSlot((i % 2) + 1);
                }
                matchRepository.saveAll(currentRound);
            }

            firstRound = allRounds.get(0);
        }

        // 填入该队伍到对应位置
        int matchIndex = position / 2;
        if (matchIndex < firstRound.size()) {
            TournamentMatch match = firstRound.get(matchIndex);
            if (position % 2 == 0) {
                match.setTeam1Id(registrationId);
            } else {
                match.setTeam2Id(registrationId);
            }
            matchRepository.save(match);
        }
    }

    // ==================== 开赛 ====================

    /**
     * 开赛：处理轮空，正式进入比赛阶段
     */
    @Transactional
    public void startEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));

        if (!"REGISTERING".equals(event.getStatus())) {
            throw new RuntimeException("赛事当前状态无法开赛");
        }

        List<EventRegistration> registrations = registrationRepository.findByEventId(eventId);

        if (registrations.isEmpty()) {
            throw new RuntimeException("没有报名队伍，无法开赛");
        }

        if (registrations.size() == 1) {
            // 1支队直接冠军
            EventRegistration champion = registrations.get(0);
            champion.setIsChampion(true);
            registrationRepository.save(champion);
            event.setStatus("ENDED");
            event.setCurrentRound(0);
            eventRepository.save(event);
            return;
        }

        // 处理首轮轮空
        processFirstRoundByes(eventId);

        event.setStatus("IN_PROGRESS");
        event.setCurrentRound(1);
        eventRepository.save(event);
    }

    /**
     * 处理首轮轮空：空位的 match 标记为 BYE，有队伍的一方自动晋级
     */
    private void processFirstRoundByes(Long eventId) {
        List<TournamentMatch> firstRound = matchRepository.findByEventIdAndRound(eventId, 1);
        for (TournamentMatch match : firstRound) {
            if (match.getTeam1Id() != null && match.getTeam2Id() == null) {
                match.setWinnerId(match.getTeam1Id());
                match.setStatus("BYE");
                advanceWinner(match, match.getTeam1Id());
            } else if (match.getTeam1Id() == null && match.getTeam2Id() != null) {
                match.setWinnerId(match.getTeam2Id());
                match.setStatus("BYE");
                advanceWinner(match, match.getTeam2Id());
            } else if (match.getTeam1Id() == null && match.getTeam2Id() == null) {
                match.setStatus("BYE");
            }
            matchRepository.save(match);
        }
    }

    // ==================== 小组循环赛 Bracket 生成（GROUP_ELIMINATION 模式开赛时调用）====================

    // ==================== 小组循环赛 Bracket 生成 ====================

    private void generateGroupStageBracket(Event event, List<EventRegistration> registrations) {
        int teamCount = registrations.size();
        // 自动分组：每组4队，不足4队的组允许
        int groupSize = 4;
        int groupCount = (int) Math.ceil((double) teamCount / groupSize);

        List<String> groupNames = new ArrayList<>();
        for (int i = 0; i < groupCount; i++) {
            groupNames.add(String.valueOf((char) ('A' + i)));
        }

        // 随机打乱队伍顺序后分配到各组
        List<EventRegistration> shuffled = new ArrayList<>(registrations);
        Collections.shuffle(shuffled);

        Map<String, List<EventRegistration>> groups = new LinkedHashMap<>();
        for (int i = 0; i < groupCount; i++) {
            groups.put(groupNames.get(i), new ArrayList<>());
        }
        for (int i = 0; i < shuffled.size(); i++) {
            String groupName = groupNames.get(i % groupCount);
            groups.get(groupName).add(shuffled.get(i));
        }

        // 为每个组生成循环赛 match（每两支队打一场）
        for (Map.Entry<String, List<EventRegistration>> entry : groups.entrySet()) {
            String groupName = entry.getKey();
            List<EventRegistration> teams = entry.getValue();
            int matchOrder = 0;

            for (int i = 0; i < teams.size(); i++) {
                for (int j = i + 1; j < teams.size(); j++) {
                    TournamentMatch match = new TournamentMatch();
                    match.setEventId(event.getId());
                    match.setRound(1);
                    match.setMatchOrder(matchOrder++);
                    match.setPhase("GROUP");
                    match.setGroupName(groupName);
                    match.setTeam1Id(teams.get(i).getId());
                    match.setTeam2Id(teams.get(j).getId());
                    match.setStatus("PENDING");
                    matchRepository.save(match);
                }
            }
        }
    }

    // ==================== 选择胜者 ====================

    @Transactional
    public void selectWinner(Long eventId, Long matchId, MatchResultRequest request, Long operatorId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));
        checkOperatorPermission(event, operatorId);

        TournamentMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("比赛不存在"));

        if (!match.getEventId().equals(eventId)) {
            throw new RuntimeException("比赛不属于该赛事");
        }

        if ("COMPLETED".equals(match.getStatus()) || "BYE".equals(match.getStatus())) {
            throw new RuntimeException("该比赛已结束");
        }

        Long winnerId = request.getWinnerId();
        if (!winnerId.equals(match.getTeam1Id()) && !winnerId.equals(match.getTeam2Id())) {
            throw new RuntimeException("胜者必须是参赛队伍之一");
        }

        Long loserId = winnerId.equals(match.getTeam1Id()) ? match.getTeam2Id() : match.getTeam1Id();

        // 更新比赛结果
        match.setWinnerId(winnerId);
        match.setScore1(request.getScore1());
        match.setScore2(request.getScore2());
        match.setStatus("COMPLETED");
        matchRepository.save(match);

        // 标记败者淘汰
        if (loserId != null) {
            EventRegistration loser = registrationRepository.findById(loserId).orElse(null);
            if (loser != null) {
                loser.setEliminated(true);
                registrationRepository.save(loser);
            }
        }

        if ("KNOCKOUT".equals(match.getPhase())) {
            // 淘汰赛：胜者晋级
            advanceWinner(match, winnerId);

            // 检查是否是决赛（最后一轮所有比赛都完成）
            checkFinalMatch(event, match);
        } else if ("GROUP".equals(match.getPhase())) {
            // 循环赛：更新积分
            updateGroupStandings(eventId, match.getGroupName(), match);
        }
    }

    /**
     * 胜者晋级到下一轮对应位置
     */
    private void advanceWinner(TournamentMatch completedMatch, Long winnerId) {
        if (completedMatch.getNextMatchId() == null) {
            return; // 决赛，无需晋级
        }

        TournamentMatch nextMatch = matchRepository.findById(completedMatch.getNextMatchId())
                .orElseThrow(() -> new RuntimeException("下一轮比赛不存在"));

        if (completedMatch.getNextMatchSlot() == 1) {
            nextMatch.setTeam1Id(winnerId);
        } else {
            nextMatch.setTeam2Id(winnerId);
        }
        matchRepository.save(nextMatch);
    }

    /**
     * 检查是否所有轮次都已完成，产生冠军
     */
    private void checkFinalMatch(Event event, TournamentMatch completedMatch) {
        if (completedMatch.getNextMatchId() != null) {
            return; // 不是决赛
        }

        // 这是决赛（没有 nextMatch），胜者即冠军
        EventRegistration champion = registrationRepository.findById(completedMatch.getWinnerId())
                .orElse(null);
        if (champion != null) {
            champion.setIsChampion(true);
            registrationRepository.save(champion);
        }

        event.setStatus("ENDED");
        eventRepository.save(event);
    }

    /**
     * 更新循环赛积分榜
     */
    private void updateGroupStandings(Long eventId, String groupName, TournamentMatch match) {
        // 简单积分：胜+3, 负+0
        Long winnerId = match.getWinnerId();
        Long loserId = winnerId.equals(match.getTeam1Id()) ? match.getTeam2Id() : match.getTeam1Id();

        // 这里简化处理，不做复杂积分榜，后续可扩展
        // GROUP_ELIMINATION 的淘汰赛阶段等所有小组比赛结束后再生成
    }

    // ==================== 删除/添加队伍 ====================

    @Transactional
    public void deleteTeam(Long eventId, Long registrationId, Long operatorId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));
        checkOperatorPermission(event, operatorId);

        EventRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("报名记录不存在"));

        if (!registration.getEventId().equals(eventId)) {
            throw new RuntimeException("该队伍不属于此赛事");
        }

        // 清理所有涉及该队伍的 match
        List<TournamentMatch> matches = matchRepository.findByEventIdOrderByRoundAscMatchOrderAsc(eventId);
        for (TournamentMatch match : matches) {
            boolean modified = false;
            if (registrationId.equals(match.getTeam1Id())) {
                match.setTeam1Id(null);
                modified = true;
            }
            if (registrationId.equals(match.getTeam2Id())) {
                match.setTeam2Id(null);
                modified = true;
            }
            if (registrationId.equals(match.getWinnerId())) {
                match.setWinnerId(null);
                if ("COMPLETED".equals(match.getStatus()) || "BYE".equals(match.getStatus())) {
                    match.setStatus("PENDING");
                }
                modified = true;
            }
            if (modified) {
                matchRepository.save(match);
            }
        }

        registrationRepository.delete(registration);
    }

    @Transactional
    public EventRegistration addTeam(Long eventId, TournamentTeamRequest request, Long operatorId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));
        checkOperatorPermission(event, operatorId);

        long currentCount = registrationRepository.countByEventId(eventId);
        if (currentCount >= event.getBracketSize()) {
            throw new RuntimeException("报名已满，无可用位置");
        }

        // 自动分配位置（与普通报名相同的分散策略）
        int position = assignBracketPosition(eventId, event.getBracketSize());

        EventRegistration registration = new EventRegistration();
        registration.setEventId(eventId);
        registration.setUserId(null); // 手动添加的队伍无关联用户
        registration.setTeamName(request.getTeamName());
        registration.setBracketPosition(position);
        registration.setEliminated(false);
        registration.setIsChampion(false);
        EventRegistration saved = registrationRepository.save(registration);

        // 更新首轮 match 对应位置
        ensureFirstRoundMatch(eventId, event.getBracketSize(), position, saved.getId());

        return saved;
    }

    // ==================== 获取 Bracket ====================

    public BracketResponse getBracket(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("赛事不存在"));

        List<EventRegistration> registrations = registrationRepository.findByEventId(eventId);
        Map<Long, EventRegistration> regMap = registrations.stream()
                .collect(Collectors.toMap(EventRegistration::getId, r -> r));

        List<TournamentMatch> matches = matchRepository.findByEventIdOrderByRoundAscMatchOrderAsc(eventId);

        // 按轮次分组
        Map<Integer, List<TournamentMatch>> roundMap = new LinkedHashMap<>();
        for (TournamentMatch match : matches) {
            roundMap.computeIfAbsent(match.getRound(), k -> new ArrayList<>()).add(match);
        }

        List<BracketResponse.RoundData> rounds = new ArrayList<>();
        for (Map.Entry<Integer, List<TournamentMatch>> entry : roundMap.entrySet()) {
            List<BracketResponse.MatchData> matchDataList = new ArrayList<>();
            for (TournamentMatch match : entry.getValue()) {
                matchDataList.add(toMatchData(match, regMap));
            }

            TournamentMatch firstMatch = entry.getValue().get(0);
            rounds.add(new BracketResponse.RoundData(
                    entry.getKey(),
                    firstMatch.getPhase(),
                    firstMatch.getGroupName(),
                    matchDataList
            ));
        }

        return new BracketResponse(
                event.getId(),
                event.getFormat(),
                event.getBracketSize(),
                registrations.size(),
                event.getStatus(),
                rounds
        );
    }

    private BracketResponse.MatchData toMatchData(TournamentMatch match, Map<Long, EventRegistration> regMap) {
        return new BracketResponse.MatchData(
                match.getId(),
                match.getMatchOrder(),
                toTeamSlot(match.getTeam1Id(), regMap),
                toTeamSlot(match.getTeam2Id(), regMap),
                match.getWinnerId(),
                match.getScore1(),
                match.getScore2(),
                match.getStatus()
        );
    }

    private BracketResponse.TeamSlot toTeamSlot(Long registrationId, Map<Long, EventRegistration> regMap) {
        if (registrationId == null) return null;
        EventRegistration reg = regMap.get(registrationId);
        if (reg == null) return null;
        return new BracketResponse.TeamSlot(
                reg.getId(),
                reg.getTeamName(),
                reg.getBracketPosition(),
                reg.getEliminated(),
                reg.getIsChampion()
        );
    }

    // ==================== 权限校验 ====================

    private void checkOperatorPermission(Event event, Long userId) {
        // 组织者或管理员可以操作（管理员通过 Controller 层 @PreAuthorize 已校验）
        if (!event.getCreatedBy().equals(userId)) {
            // 这里只检查是否是组织者，管理员在 Controller 层单独处理
            throw new RuntimeException("无权操作此赛事");
        }
    }
}
