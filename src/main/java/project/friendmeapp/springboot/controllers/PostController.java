package project.friendmeapp.springboot.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import project.friendmeapp.springboot.models.Comment;
import project.friendmeapp.springboot.models.Post;
import project.friendmeapp.springboot.services.PostService;
import project.friendmeapp.springboot.services.UserService;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/posts/")
public class PostController {
    @Autowired
    PostService postService;

    @Autowired
    UserService userService;

    @PostMapping("/add-post")
    public ResponseEntity<?> addPosts(HttpServletRequest request, @RequestBody Post content) {
        return postService.addPost(request, content);
    }

    @PostMapping("/add-comment")
    public ResponseEntity<?> addComment(HttpServletRequest request, @RequestBody Comment comment,
                                        @RequestParam Long postId){
        return postService.addCommentToPost(request, comment, postId);
    }


}
