package com.instagram.clone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String caption;

    private String mediaUrl;

    private String mediaType; // IMAGE or VIDEO

    private String publicId; // from Cloudinary

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "post" , cascade = CascadeType.ALL)
    private List<Comment> comments;
    private LocalDateTime createdAt;
}