package project.friendmeapp.springboot.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import project.friendmeapp.springboot.services.FriendService;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/friends")
public class FriendController {

    @Autowired
    FriendService friendService;

    @GetMapping("/my-friends")
    public ResponseEntity<?> getFriends(HttpServletRequest request) {
        return friendService.getFriends(request);
    }

    @PostMapping("/send-request")
    public ResponseEntity<?> sendReqFriend(HttpServletRequest request, @RequestParam Long id){
        return friendService.sendReq(request, id);
    }

    @PostMapping("/cancel-request")
    public ResponseEntity<?> cancelSentReq(HttpServletRequest request, @RequestParam Long id) {
        return friendService.cancelSentReq(request,id);
    }

    @PostMapping("/accept-request")
    public ResponseEntity<?> acceptSentReq(HttpServletRequest request, @RequestParam Long id) {
        return friendService.acceptReq(request, id);
    }

    @PostMapping("/reject-request")
    public ResponseEntity<?> rejectSentReq(HttpServletRequest request, @RequestParam Long id) {
        return friendService.rejectReq(request, id);
    }

}
