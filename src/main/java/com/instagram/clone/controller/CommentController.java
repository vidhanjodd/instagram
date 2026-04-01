//package com.instagram.clone.controller;
//
//import com.instagram.clone.dto.CommentRequest;
//import com.instagram.clone.service.CommentService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//@Controller
//@RequestMapping("/comments")
//@RequiredArgsConstructor
//public class CommentController {
//
//    private final CommentService commentService;
//
//    @PostMapping("/add")
//    public String create(@ModelAttribute CommentRequest request) {
//
//        request.setParentId(null);
//
//        commentService.createTopLevelComment(request);
//
//        // CHANGED: redirect back to same post details page
//        return "redirect:/posts/" + request.getPostId();
//    }
//}


package com.instagram.clone.controller;

import com.instagram.clone.dto.CommentRequest;
import com.instagram.clone.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/add")
    public String create(@ModelAttribute CommentRequest request) {

        // Route the request based on the presence of a parentId
        if (request.getParentId() == null) {
            commentService.createTopLevelComment(request);
        } else {
            commentService.addReply(request);
        }

        // Redirect back to the post or wherever you need the user to go
        return "redirect:/posts";
    }
}