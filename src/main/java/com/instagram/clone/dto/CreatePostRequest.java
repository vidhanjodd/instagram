package com.instagram.clone.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class CreatePostRequest {

    private String caption;

    @Size(min = 1, max = 10, message = "A post must contain between 1 and 10 media items")
    private List<MultipartFile> mediaFiles;
}