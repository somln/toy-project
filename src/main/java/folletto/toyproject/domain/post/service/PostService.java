package folletto.toyproject.domain.post.service;

import folletto.toyproject.domain.post.dto.PostListResponse;
import folletto.toyproject.domain.post.dto.PostRequest;
import folletto.toyproject.domain.post.dto.PostResponse;
import folletto.toyproject.domain.post.entity.PostEntity;
import folletto.toyproject.domain.post.respository.PostRepository;
import folletto.toyproject.domain.user.entity.UserEntity;
import folletto.toyproject.domain.user.repository.UserRepository;
import folletto.toyproject.global.exception.ApplicationException;
import folletto.toyproject.global.exception.ErrorCode;
import folletto.toyproject.global.keycloak.KeyCloakClient;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.keycloak.KeycloakPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final KeyCloakClient keyCloakClient;

    @Transactional
    public void createPost(PostRequest createPostRequest, String userUUID) throws ApplicationException {
        // 또는 userDetails에 따라 다른 방법으로 UUID를 추출
        UserEntity user = findUserByUUID(userUUID);
        postRepository.save(PostEntity.of(createPostRequest, user.getUserId()));
    }

    @Transactional
    public void updatePost(Long postId, PostRequest postRequest, String token) {
        UserEntity user = authenticateUser(token);
        PostEntity post = findPostById(postId);
        validateAuthor(user, post);
        post.update(postRequest);
    }

    @Transactional
    public void deletePost(Long postId, String token) {
        UserEntity user = authenticateUser(token);
        PostEntity post = findPostById(postId);
        validateAuthor(user, post);
        postRepository.delete(post);
    }

    public PostResponse findPost(Long postId, String token) {
        authenticateUser(token);
        PostEntity post = findPostById(postId);
        UserEntity user = findUserById(post.getUserId());
        return PostResponse.from(findPostById(postId), user);
    }

    public PostListResponse findPosts(String sort, Pageable pageable, String token) {
        authenticateUser(token);
        Page<PostEntity> posts = fetchSortedPosts(sort, pageable);
        List<PostResponse> postResponses = posts.getContent().stream()
                .map(post -> {
                    UserEntity user = findUserById(post.getUserId());
                    return PostResponse.from(post, user);
                })
                .toList();
        return PostListResponse.from(postResponses, posts);
    }

    public List<PostResponse> searchPosts(String keyword, String token) {
        UserEntity user = authenticateUser(token);
        List<PostEntity> posts = postRepository.searchByTitleOrContent(keyword);
        return posts.stream().map(post -> PostResponse.from(post, user)).toList();
    }

    private UserEntity authenticateUser(String token) {
        String userUUID = keyCloakClient.validateToken(token);
        return findUserByUUID(userUUID);
    }

    private UserEntity findUserByUUID(String userUUID) {
        return userRepository.findByUserUUID(userUUID)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
    }

    private UserEntity findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
    }


    private PostEntity findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.POST_NOT_FOUND));
    }

    private void validateAuthor(UserEntity user, PostEntity post) {
        if (!post.getUserId().equals(user.getUserId())) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private Page<PostEntity> fetchSortedPosts(String sort, Pageable pageable) {
        if (SortType.fromDescription(sort).equals(SortType.DESC)) {
            return postRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            return postRepository.findAllByOrderByCreatedAtAsc(pageable);
        }
    }
}
