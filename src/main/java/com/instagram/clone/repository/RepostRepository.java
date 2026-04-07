package com.instagram.clone.repository;

import com.instagram.clone.entity.Post;
import com.instagram.clone.entity.Repost;
import com.instagram.clone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepostRepository extends JpaRepository<Repost, Long> {


    Optional<Repost> findByUserAndPost(User user, Post post);

    boolean existsByUserAndPost(User user, Post post);


    List<Repost> findByUserOrderByCreatedAtDesc(User user);

    long countByPost(Post post);

    @Query("""
        SELECT r FROM Repost r
        JOIN FETCH r.post p
        JOIN FETCH p.user pu
        JOIN FETCH p.carouselMedia
        WHERE r.user IN (
            SELECT f.following FROM Follow f
            WHERE f.follower = :currentUser
            AND f.status = 'ACCEPTED'
        )
        ORDER BY r.createdAt DESC
    """)
    List<Repost> findFeedRepostsByFollowedUsers(@Param("currentUser") User currentUser);
}