package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.entity.UserPrivacy;
import com.volleyball.volleyballcommunitybackend.repository.UserPrivacyRepository;
import org.springframework.stereotype.Service;

@Service
public class PrivacyService {

    private final UserPrivacyRepository userPrivacyRepository;

    public PrivacyService(UserPrivacyRepository userPrivacyRepository) {
        this.userPrivacyRepository = userPrivacyRepository;
    }

    public UserPrivacy getOrCreatePrivacySettings(Long userId) {
        return userPrivacyRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserPrivacy privacy = new UserPrivacy();
                    privacy.setUserId(userId);
                    privacy.setFollowListVisible(true);
                    privacy.setFollowerListVisible(true);
                    privacy.setFriendsOnlyReceive(false);
                    return userPrivacyRepository.save(privacy);
                });
    }

    public boolean isFollowListVisible(Long userId, Long viewerId) {
        if (userId.equals(viewerId)) {
            return true;
        }
        UserPrivacy privacy = getOrCreatePrivacySettings(userId);
        return privacy.getFollowListVisible();
    }

    public boolean isFollowerListVisible(Long userId, Long viewerId) {
        if (userId.equals(viewerId)) {
            return true;
        }
        UserPrivacy privacy = getOrCreatePrivacySettings(userId);
        return privacy.getFollowerListVisible();
    }

    public boolean isFriendsOnlyReceive(Long userId) {
        UserPrivacy privacy = getOrCreatePrivacySettings(userId);
        return privacy.getFriendsOnlyReceive();
    }
}
