package project.friendmeapp.springboot.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.friendmeapp.springboot.services.PostService;
import project.friendmeapp.springboot.services.UserService;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/home")
public class HomeController {
  @Autowired
  PostService postService;

  @Autowired
  UserService userService;

  @GetMapping("/get-posts")
  public ResponseEntity<?> getPosts(HttpServletRequest request){
    return postService.getPost(request);
  }

  @GetMapping("/profile")
  public ResponseEntity<?> getProfilePosts(HttpServletRequest request, @RequestParam Long id){
    return userService.getUserInfo(request, id);
  }

}
