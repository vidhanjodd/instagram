package com.instagram.clone.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public Map<String, Object> uploadFile(MultipartFile file) {
        try {
            return cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto")
            );
        } catch (IOException e) {
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }

    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file from Cloudinary: " + e.getMessage());
        }
    }

    public Map<String, Object> uploadProfilePicture(MultipartFile file) {
        try {
            return cloudinary.uploader().upload(
                    file.getBytes(),

                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", "profile_pics",
                            "transformation", "w_400,h_400,c_fill,g_face"
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException("Profile picture upload failed: " + e.getMessage());
        }
    }
}