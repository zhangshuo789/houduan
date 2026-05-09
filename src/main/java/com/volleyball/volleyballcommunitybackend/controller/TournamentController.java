package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.MatchResultRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.TournamentTeamRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.BracketResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.EventRegistrationResponse;
import com.volleyball.volleyballcommunitybackend.entity.EventRegistration;
import com.volleyball.volleyballcommunitybackend.service.EventService;
import com.volleyball.volleyballcommunitybackend.service.TournamentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/event")
public class TournamentController {

    private final TournamentService tournamentService;
    private final EventService eventService;

    public TournamentController(TournamentService tournamentService, EventService eventService) {
        this.tournamentService = tournamentService;
        this.eventService = eventService;
    }

    /**
     * 获取对阵图（所有人可见）
     */
    @GetMapping("/{id}/bracket")
    public ResponseEntity<ApiResponse<BracketResponse>> getBracket(@PathVariable Long id) {
        BracketResponse bracket = tournamentService.getBracket(id);
        return ResponseEntity.ok(ApiResponse.success(bracket));
    }

    /**
     * 重建对阵图（修复已有数据）
     */
    @PostMapping("/{id}/bracket/rebuild")
    public ResponseEntity<ApiResponse<Void>> rebuildBracket(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        if (!isOrganizerOrAdmin(id, userId, authentication)) {
            throw new RuntimeException("无权操作此赛事");
        }
        tournamentService.rebuildBracket(id);
        return ResponseEntity.ok(ApiResponse.success("对阵图已重建", null));
    }

    /**
     * 组织者/管理员手动开赛
     */
    @PostMapping("/{id}/bracket/start")
    public ResponseEntity<ApiResponse<Void>> startEvent(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        if (!isOrganizerOrAdmin(id, userId, authentication)) {
            throw new RuntimeException("无权操作此赛事");
        }
        tournamentService.startEvent(id);
        return ResponseEntity.ok(ApiResponse.success("赛事已开赛", null));
    }

    /**
     * 组织者/管理员记录比赛结果（选胜者）
     */
    @PutMapping("/{id}/match/{matchId}/result")
    public ResponseEntity<ApiResponse<Void>> selectWinner(
            @PathVariable Long id,
            @PathVariable Long matchId,
            @Valid @RequestBody MatchResultRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        if (!isOrganizerOrAdmin(id, userId, authentication)) {
            throw new RuntimeException("无权操作此赛事");
        }
        tournamentService.selectWinner(id, matchId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("比赛结果已记录", null));
    }

    /**
     * 组织者/管理员手动添加队伍
     */
    @PostMapping("/{id}/bracket/team")
    public ResponseEntity<ApiResponse<EventRegistrationResponse>> addTeam(
            @PathVariable Long id,
            @Valid @RequestBody TournamentTeamRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        if (!isOrganizerOrAdmin(id, userId, authentication)) {
            throw new RuntimeException("无权操作此赛事");
        }
        EventRegistration reg = tournamentService.addTeam(id, request, userId);
        EventRegistrationResponse response = new EventRegistrationResponse(
                reg.getId(), reg.getEventId(), reg.getUserId(), reg.getTeamName(),
                reg.getBracketPosition(), reg.getEliminated(), reg.getIsChampion(), reg.getCreatedAt()
        );
        return ResponseEntity.ok(ApiResponse.success("队伍添加成功", response));
    }

    /**
     * 组织者/管理员删除队伍
     */
    @DeleteMapping("/{id}/bracket/team/{regId}")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(
            @PathVariable Long id,
            @PathVariable Long regId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        if (!isOrganizerOrAdmin(id, userId, authentication)) {
            throw new RuntimeException("无权操作此赛事");
        }
        tournamentService.deleteTeam(id, regId, userId);
        return ResponseEntity.ok(ApiResponse.success("队伍已删除", null));
    }

    /**
     * 判断是否是组织者或管理员
     */
    private boolean isOrganizerOrAdmin(Long eventId, Long userId, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return true;
        return eventService.isEventOrganizer(eventId, userId);
    }
}
