package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.response.PostResponse;
import com.volleyball.volleyballcommunitybackend.entity.Board;
import com.volleyball.volleyballcommunitybackend.entity.Favorite;
import com.volleyball.volleyballcommunitybackend.entity.Post;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.repository.BoardRepository;
import com.volleyball.volleyballcommunitybackend.repository.FavoriteRepository;
import com.volleyball.volleyballcommunitybackend.repository.PostRepository;
import com.volleyball.volleyballcommunitybackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository,
                          PostRepository postRepository, BoardRepository boardRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.boardRepository = boardRepository;
    }

    public void favorite(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        if (favoriteRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new RuntimeException("已收藏");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setPostId(postId);
        favoriteRepository.save(favorite);
    }

    public void unfavorite(Long postId, Long userId) {
        if (!favoriteRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new RuntimeException("未收藏");
        }
        favoriteRepository.deleteByUserIdAndPostId(userId, postId);
    }

    public Boolean getFavoriteStatus(Long postId, Long userId) {
        if (userId == null) {
            return null;
        }
        return favoriteRepository.existsByUserIdAndPostId(userId, postId);
    }

    public Long getFavoriteCount(Long postId) {
        return favoriteRepository.countByPostId(postId);
    }

    public Page<PostResponse> getUserFavorites(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Favorite> favorites = favoriteRepository.findByUserId(userId, pageable);

        return favorites.map(fav -> {
            Post post = postRepository.findById(fav.getPostId()).orElse(null);
            if (post == null) {
                return null;
            }
            User user = userRepository.findById(post.getUserId()).orElse(null);
            Board board = boardRepository.findById(post.getBoardId()).orElse(null);
            return toPostResponse(post, user, board);
        });
    }

    private PostResponse toPostResponse(Post post, User user, Board board) {
        PostResponse.UserInfo userInfo = null;
        PostResponse.BoardInfo boardInfo = null;

        if (user != null) {
            userInfo = new PostResponse.UserInfo(user.getId(), user.getNickname(), user.getAvatar());
        }
        if (board != null) {
            boardInfo = new PostResponse.BoardInfo(board.getId(), board.getName());
        }

        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                userInfo,
                boardInfo,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
