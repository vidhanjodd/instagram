package com.instagram.clone.repository;

import com.instagram.clone.entity.Follow;
import com.instagram.clone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following = :following AND (f.status = 'ACCEPTED' OR f.status IS NULL)")
    long countAcceptedByFollowing(@Param("following") User following);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower = :follower AND (f.status = 'ACCEPTED' OR f.status IS NULL)")
    long countAcceptedByFollower(@Param("follower") User follower);

    @Query("SELECT f FROM Follow f WHERE f.following = :following AND (f.status = 'ACCEPTED' OR f.status IS NULL)")
    List<Follow> findAllAcceptedByFollowing(@Param("following") User following);

    @Query("SELECT f FROM Follow f WHERE f.follower = :follower AND (f.status = 'ACCEPTED' OR f.status IS NULL)")
    List<Follow> findAllAcceptedByFollower(@Param("follower") User follower);



    long countByFollower(User follower);

    long countByFollowing(User following);

    List<Follow> findAllByFollowing(User following);

    List<Follow> findAllByFollower(User follower);

    @Query("SELECT COUNT(f) > 0 FROM Follow f WHERE f.follower = :follower AND f.following = :following AND f.status = 'PENDING'")
    boolean existsPendingRequest(@Param("follower") User follower,
                                 @Param("following") User following);

    @Query("SELECT f FROM Follow f WHERE f.following = :owner AND f.status = 'PENDING'")
    List<Follow> findPendingRequestsFor(@Param("owner") User owner);

    List<Follow> findByFollowerId(Long followerId);

    boolean existsByFollowerAndFollowing(User follower, User following);
}