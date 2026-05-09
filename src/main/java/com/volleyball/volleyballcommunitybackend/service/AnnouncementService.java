package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.AnnouncementRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.AnnouncementResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UserSimpleResponse;
import com.volleyball.volleyballcommunitybackend.entity.Announcement;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.repository.AnnouncementRepository;
import com.volleyball.volleyballcommunitybackend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    public AnnouncementService(AnnouncementRepository announcementRepository,
                               UserRepository userRepository, FileService fileService) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.fileService = fileService;
    }

    public Page<AnnouncementResponse> getList(Pageable pageable, HttpServletRequest request) {
        return announcementRepository.findAllByOrderByPinnedDescCreatedAtDesc(pageable)
                .map(a -> toResponse(a, request));
    }

    public AnnouncementResponse getById(Long id, HttpServletRequest request) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
        return toResponse(announcement, request);
    }

    @Transactional
    public AnnouncementResponse create(AnnouncementRequest request, Long adminId, HttpServletRequest httpRequest) {
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setPinned(request.getPinned() != null ? request.getPinned() : false);
        announcement.setPublishedBy(adminId);

        Announcement saved = announcementRepository.save(announcement);
        return toResponse(saved, httpRequest);
    }

    @Transactional
    public AnnouncementResponse update(Long id, AnnouncementRequest request, HttpServletRequest httpRequest) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));

        if (request.getTitle() != null) announcement.setTitle(request.getTitle());
        if (request.getContent() != null) announcement.setContent(request.getContent());
        if (request.getPinned() != null) announcement.setPinned(request.getPinned());

        Announcement saved = announcementRepository.save(announcement);
        return toResponse(saved, httpRequest);
    }

    @Transactional
    public void delete(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new RuntimeException("公告不存在");
        }
        announcementRepository.deleteById(id);
    }

    private AnnouncementResponse toResponse(Announcement a, HttpServletRequest request) {
        User publisher = userRepository.findById(a.getPublishedBy()).orElse(null);
        UserSimpleResponse publisherInfo = null;
        if (publisher != null) {
            publisherInfo = new UserSimpleResponse(
                    publisher.getId(),
                    publisher.getNickname(),
                    getAvatarUrl(publisher.getAvatar(), request)
            );
        }
        return new AnnouncementResponse(
                a.getId(), a.getTitle(), a.getContent(), a.getPinned(),
                publisherInfo, a.getCreatedAt(), a.getUpdatedAt()
        );
    }

    private String getAvatarUrl(String avatar, HttpServletRequest request) {
        if (avatar == null || avatar.isEmpty() || request == null) return null;
        try {
            return fileService.getFileUrl(Long.parseLong(avatar), request);
        } catch (NumberFormatException e) {
            return avatar;
        }
    }
}
