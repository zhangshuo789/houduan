package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
    Page<Follow> findByFollowerId(Long followerId, Pageable pageable);
    Page<Follow> findByFolloweeId(Long followeeId, Pageable pageable);
    long countByFollowerId(Long followerId);
    long countByFolloweeId(Long followeeId);
    void deleteByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
}
