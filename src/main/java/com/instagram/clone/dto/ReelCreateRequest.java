package com.instagram.clone.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReelCreateRequest {

    private String videoUrl;
    private String caption;

}