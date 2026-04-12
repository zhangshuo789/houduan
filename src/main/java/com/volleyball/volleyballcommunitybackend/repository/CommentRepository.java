package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPostIdAndParentIdIsNull(Long postId, Pageable pageable);
    Page<Comment> findByPostId(Long postId, Pageable pageable);
    List<Comment> findByParentId(Long parentId);
    long countByPostId(Long postId);
}
