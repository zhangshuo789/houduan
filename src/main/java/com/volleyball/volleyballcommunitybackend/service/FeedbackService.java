package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.FeedbackReplyRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.FeedbackRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.FeedbackResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UserSimpleResponse;
import com.volleyball.volleyballcommunitybackend.entity.Feedback;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.repository.FeedbackRepository;
import com.volleyball.volleyballcommunitybackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FeedbackResponse submit(FeedbackRequest request, Long userId) {
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setTitle(request.getTitle());
        feedback.setContent(request.getContent());
        feedback.setCategory(request.getCategory());
        feedback.setStatus("PENDING");

        Feedback saved = feedbackRepository.save(feedback);
        return toResponse(saved);
    }

    public Page<FeedbackResponse> getMyFeedback(Long userId, Pageable pageable) {
        return feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    public Page<FeedbackResponse> getAll(String status, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            return feedbackRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                    .map(this::toResponse);
        }
        return feedbackRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Transactional
    public FeedbackResponse reply(Long feedbackId, FeedbackReplyRequest request, Long adminId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("反馈记录不存在"));

        feedback.setReply(request.getReply());
        feedback.setRepliedBy(adminId);
        feedback.setRepliedAt(LocalDateTime.now());
        feedback.setStatus("REPLIED");

        Feedback saved = feedbackRepository.save(feedback);
        return toResponse(saved);
    }

    @Transactional
    public void close(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("反馈记录不存在"));
        feedback.setStatus("CLOSED");
        feedbackRepository.save(feedback);
    }

    private FeedbackResponse toResponse(Feedback f) {
        String userNickname = null;
        User user = userRepository.findById(f.getUserId()).orElse(null);
        if (user != null) {
            userNickname = user.getNickname();
        }

        UserSimpleResponse repliedByInfo = null;
        if (f.getRepliedBy() != null) {
            User replier = userRepository.findById(f.getRepliedBy()).orElse(null);
            if (replier != null) {
                repliedByInfo = new UserSimpleResponse(
                        replier.getId(), replier.getNickname(), replier.getAvatar()
                );
            }
        }

        return new FeedbackResponse(
                f.getId(), f.getUserId(), userNickname,
                f.getTitle(), f.getContent(), f.getCategory(), f.getStatus(),
                f.getReply(), repliedByInfo, f.getRepliedAt(),
                f.getCreatedAt(), f.getUpdatedAt()
        );
    }
}
