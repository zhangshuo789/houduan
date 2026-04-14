package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.UserPrivacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserPrivacyRepository extends JpaRepository<UserPrivacy, Long> {
    Optional<UserPrivacy> findByUserId(Long userId);
}
