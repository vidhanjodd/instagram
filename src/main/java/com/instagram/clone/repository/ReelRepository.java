package com.instagram.clone.repository;

import com.instagram.clone.entity.Reel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReelRepository extends JpaRepository<Reel, Long> {

    List<Reel> findAllByOrderByCreatedAtDesc();

    List<Reel> findByUserIdOrderByCreatedAtDesc(Long userId);
}