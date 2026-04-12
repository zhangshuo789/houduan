package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByBoardId(Long boardId, Pageable pageable);
}
