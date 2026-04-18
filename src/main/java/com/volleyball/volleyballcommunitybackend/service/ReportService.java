package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.HandleReportRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.ReportRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ReportResponse;
import com.volleyball.volleyballcommunitybackend.entity.*;
import com.volleyball.volleyballcommunitybackend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final AdminLogRepository adminLogRepository;

    public ReportService(ReportRepository reportRepository, PostRepository postRepository,
                        CommentRepository commentRepository, EventRepository eventRepository,
                        UserRepository userRepository, AdminLogRepository adminLogRepository) {
        this.reportRepository = reportRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.adminLogRepository = adminLogRepository;
    }

    // 用户提交举报
    @Transactional
    public void submitReport(ReportRequest request, Long userId) {
        Report report = new Report();
        report.setReporterId(userId);
        report.setTargetType(request.getTargetType());
        report.setTargetId(request.getTargetId());
        report.setReason(request.getReason());
        report.setStatus("PENDING");

        reportRepository.save(report);
    }

    // 获取举报列表
    public Page<ReportResponse> getReportList(String status, Pageable pageable) {
        Page<Report> reports;
        if (status == null || status.isEmpty() || "ALL".equals(status)) {
            reports = reportRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            reports = reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }

        return reports.map(this::toReportResponse);
    }

    // 获取待处理数量
    public long getPendingCount() {
        return reportRepository.countByStatus("PENDING");
    }

    // 处理举报
    @Transactional
    public void handleReport(Long reportId, HandleReportRequest request, Long adminId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("举报不存在"));

        if (!"PENDING".equals(report.getStatus())) {
            throw new RuntimeException("该举报已被处理");
        }

        // 更新举报状态
        report.setStatus(request.getApproved() ? "APPROVED" : "DISMISSED");
        report.setHandledBy(adminId);
        report.setHandledAt(LocalDateTime.now());
        report.setHandleResult(request.getResult());
        reportRepository.save(report);

        // 如果确认举报，处理目标内容
        if (request.getApproved()) {
            handleReportedContent(report.getTargetType(), report.getTargetId(), request.getResult(), adminId);
        }

        // 记录管理员操作日志
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction("HANDLE_REPORT");
        log.setTargetType("REPORT");
        log.setTargetId(reportId);
        log.setDetail("处理举报: targetType=" + report.getTargetType() + ", targetId=" + report.getTargetId()
                + ", approved=" + request.getApproved() + ", result=" + request.getResult());
        adminLogRepository.save(log);
    }

    private void handleReportedContent(String targetType, Long targetId, String result, Long adminId) {
        switch (targetType) {
            case "POST":
                Post post = postRepository.findById(targetId).orElse(null);
                if (post != null && "CONTENT_DELETED".equals(result)) {
                    postRepository.delete(post);
                }
                break;
            case "COMMENT":
                Comment comment = commentRepository.findById(targetId).orElse(null);
                if (comment != null && "CONTENT_DELETED".equals(result)) {
                    commentRepository.delete(comment);
                }
                break;
            case "EVENT":
                Event event = eventRepository.findById(targetId).orElse(null);
                if (event != null && "CONTENT_DELETED".equals(result)) {
                    eventRepository.delete(event);
                }
                break;
        }
    }

    private ReportResponse toReportResponse(Report report) {
        String reporterNickname = userRepository.findById(report.getReporterId())
                .map(User::getNickname).orElse(null);

        String targetTitle = getTargetTitle(report.getTargetType(), report.getTargetId());

        String handledByNickname = null;
        if (report.getHandledBy() != null) {
            handledByNickname = userRepository.findById(report.getHandledBy())
                    .map(User::getNickname).orElse(null);
        }

        return new ReportResponse(
                report.getId(),
                report.getReporterId(),
                reporterNickname,
                report.getTargetType(),
                report.getTargetId(),
                targetTitle,
                report.getReason(),
                report.getStatus(),
                report.getHandledBy(),
                handledByNickname,
                report.getHandledAt(),
                report.getHandleResult(),
                report.getCreatedAt()
        );
    }

    private String getTargetTitle(String targetType, Long targetId) {
        switch (targetType) {
            case "POST":
                return postRepository.findById(targetId).map(Post::getTitle).orElse(null);
            case "COMMENT":
                return commentRepository.findById(targetId).map(Comment::getContent).orElse(null);
            case "EVENT":
                return eventRepository.findById(targetId).map(Event::getTitle).orElse(null);
            default:
                return null;
        }
    }
}
