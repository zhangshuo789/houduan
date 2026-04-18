package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.response.ContentStatsResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.StatsOverviewResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UserStatsResponse;
import com.volleyball.volleyballcommunitybackend.entity.Message;
import com.volleyball.volleyballcommunitybackend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class StatsService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final ReportRepository reportRepository;
    private final UserStatusRepository userStatusRepository;
    private final MessageRepository messageRepository;

    public StatsService(UserRepository userRepository, PostRepository postRepository,
                        CommentRepository commentRepository, EventRepository eventRepository,
                        ReportRepository reportRepository,
                        UserStatusRepository userStatusRepository, MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.eventRepository = eventRepository;
        this.reportRepository = reportRepository;
        this.userStatusRepository = userStatusRepository;
        this.messageRepository = messageRepository;
    }

    // 运营概览
    public StatsOverviewResponse getOverview() {
        long totalUsers = userRepository.count();
        long totalPosts = postRepository.count();
        long totalComments = commentRepository.count();
        long totalEvents = eventRepository.count();
        long totalGroups = countGroups();
        long totalReports = reportRepository.count();
        long pendingReports = reportRepository.countByStatus("PENDING");
        long disabledUsers = countDisabledUsers();

        return new StatsOverviewResponse(
                totalUsers, totalPosts, totalComments, totalEvents,
                totalGroups, totalReports, pendingReports, disabledUsers
        );
    }

    // 用户统计
    public UserStatsResponse getUserStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.with(LocalTime.MIN);
        LocalDateTime startOfMonth = now.withDayOfMonth(1).with(LocalTime.MIN);

        long totalUsers = userRepository.count();
        long newUsersToday = countNewUsersSince(startOfToday);
        long newUsersThisMonth = countNewUsersSince(startOfMonth);
        long activeUsersToday = countActiveUsersSince(startOfToday);
        long activeUsersThisMonth = countActiveUsersSince(startOfMonth);

        return new UserStatsResponse(
                totalUsers, newUsersToday, newUsersThisMonth,
                activeUsersToday, activeUsersThisMonth
        );
    }

    // 内容统计
    public ContentStatsResponse getContentStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.with(LocalTime.MIN);
        LocalDateTime startOfMonth = now.withDayOfMonth(1).with(LocalTime.MIN);

        long totalPosts = postRepository.count();
        long totalComments = commentRepository.count();
        long postsToday = countPostsSince(startOfToday);
        long commentsToday = countCommentsSince(startOfToday);
        long postsThisMonth = countPostsSince(startOfMonth);
        long commentsThisMonth = countCommentsSince(startOfMonth);

        return new ContentStatsResponse(
                totalPosts, totalComments, postsToday,
                commentsToday, postsThisMonth, commentsThisMonth
        );
    }

    private long countGroups() {
        // Groups are stored as Message with type='group'
        return messageRepository.findAll().stream()
                .filter(m -> "group".equals(m.getType()))
                .count();
    }

    private long countDisabledUsers() {
        return userStatusRepository.findAll().stream()
                .filter(us -> Boolean.TRUE.equals(us.getDisabled()))
                .count();
    }

    private long countNewUsersSince(LocalDateTime since) {
        return userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(since))
                .count();
    }

    private long countActiveUsersSince(LocalDateTime since) {
        // Active users = users who have created posts or comments since the given time
        Page<Message> messages = messageRepository.findAll(PageRequest.of(0, Integer.MAX_VALUE));
        // For simplicity, we count users who have sent messages since the given time
        return messages.getContent().stream()
                .filter(m -> m.getCreatedAt() != null && m.getCreatedAt().isAfter(since))
                .map(Message::getSenderId)
                .distinct()
                .count();
    }

    private long countPostsSince(LocalDateTime since) {
        return postRepository.findAll().stream()
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(since))
                .count();
    }

    private long countCommentsSince(LocalDateTime since) {
        return commentRepository.findAll().stream()
                .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(since))
                .count();
    }
}
