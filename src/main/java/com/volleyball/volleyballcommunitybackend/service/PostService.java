package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.PostRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.PostDetailResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.PostResponse;
import com.volleyball.volleyballcommunitybackend.entity.Board;
import com.volleyball.volleyballcommunitybackend.entity.Post;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.repository.BoardRepository;
import com.volleyball.volleyballcommunitybackend.repository.CommentRepository;
import com.volleyball.volleyballcommunitybackend.repository.FavoriteRepository;
import com.volleyball.volleyballcommunitybackend.repository.LikeRepository;
import com.volleyball.volleyballcommunitybackend.repository.PostRepository;
import com.volleyball.volleyballcommunitybackend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;
    private final CommentRepository commentRepository;
    private final FileService fileService;

    public PostService(PostRepository postRepository, UserRepository userRepository,
                      BoardRepository boardRepository, LikeRepository likeRepository,
                      FavoriteRepository favoriteRepository, CommentRepository commentRepository,
                      FileService fileService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
        this.likeRepository = likeRepository;
        this.favoriteRepository = favoriteRepository;
        this.commentRepository = commentRepository;
        this.fileService = fileService;
    }

    public PostResponse createPost(PostRequest request, Long userId, HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(() -> new RuntimeException("板块不存在"));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setUserId(userId);
        post.setBoardId(request.getBoardId());

        Post savedPost = postRepository.save(post);

        return toPostResponse(savedPost, user, board, httpRequest);
    }

    public PostDetailResponse getPostById(Long id, Long currentUserId, HttpServletRequest httpRequest) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        User user = userRepository.findById(post.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Board board = boardRepository.findById(post.getBoardId())
                .orElseThrow(() -> new RuntimeException("板块不存在"));

        Long likeCount = likeRepository.countByPostId(id);
        Long favoriteCount = favoriteRepository.countByPostId(id);
        Long commentCount = commentRepository.countByPostId(id);

        Boolean liked = currentUserId != null ? likeRepository.existsByUserIdAndPostId(currentUserId, id) : null;
        Boolean favorited = currentUserId != null ? favoriteRepository.existsByUserIdAndPostId(currentUserId, id) : null;

        return toPostDetailResponse(post, user, board, likeCount, favoriteCount, commentCount, liked, favorited, httpRequest);
    }

    public PostResponse updatePost(Long id, PostRequest request, Long userId, HttpServletRequest httpRequest) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权限修改此帖子");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setBoardId(request.getBoardId());

        Post updatedPost = postRepository.save(post);

        User user = userRepository.findById(post.getUserId()).orElse(null);
        Board board = boardRepository.findById(post.getBoardId()).orElse(null);

        return toPostResponse(updatedPost, user, board, httpRequest);
    }

    public void deletePost(Long id, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此帖子");
        }

        postRepository.delete(post);
    }

    public Page<PostResponse> getPostsByBoardId(Long boardId, int page, int size, HttpServletRequest httpRequest) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postRepository.findByBoardId(boardId, pageable);

        return posts.map(post -> {
            User user = userRepository.findById(post.getUserId()).orElse(null);
            Board board = boardRepository.findById(post.getBoardId()).orElse(null);
            return toPostResponse(post, user, board, httpRequest);
        });
    }

    private PostResponse toPostResponse(Post post, User user, Board board, HttpServletRequest request) {
        PostResponse.UserInfo userInfo = null;
        PostResponse.BoardInfo boardInfo = null;

        if (user != null) {
            userInfo = new PostResponse.UserInfo(user.getId(), user.getNickname(), getAvatarUrl(user, request));
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

    private PostDetailResponse toPostDetailResponse(Post post, User user, Board board,
                                                   Long likeCount, Long favoriteCount, Long commentCount,
                                                   Boolean liked, Boolean favorited, HttpServletRequest request) {
        PostResponse.UserInfo userInfo = null;
        PostResponse.BoardInfo boardInfo = null;

        if (user != null) {
            userInfo = new PostResponse.UserInfo(user.getId(), user.getNickname(), getAvatarUrl(user, request));
        }
        if (board != null) {
            boardInfo = new PostResponse.BoardInfo(board.getId(), board.getName());
        }

        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                userInfo,
                boardInfo,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                likeCount,
                favoriteCount,
                commentCount,
                liked,
                favorited
        );
    }

    private String getAvatarUrl(User user, HttpServletRequest request) {
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            return null;
        }
        try {
            Long fileId = Long.parseLong(user.getAvatar());
            return fileService.getFileUrl(fileId, request);
        } catch (NumberFormatException e) {
            return user.getAvatar();
        }
    }
}
