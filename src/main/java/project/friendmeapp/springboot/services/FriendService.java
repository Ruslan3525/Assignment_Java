package project.friendmeapp.springboot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.friendmeapp.springboot.models.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FriendService {
    @Autowired
    UserService userService;

    @Transactional
    public ResponseEntity<?> sendReq(HttpServletRequest request, Long id) {
        // from request if in header "Authorization" there is token, we get user by this token
        // we detemine current user
        User currentUser = userService.getUserByTokenFromRequest(request);

        // using id from parameter we get user_id of person whom we send request
        User person = userService.findById(id);
        // if user doesn't exist we return user not found response
        if (person == null) {
            return ResponseEntity.notFound().build();
        }
        // if id specified the same as for current user, get message that user cannot send request to himself
        if(currentUser.getId().equals(person.getId())){
            return ResponseEntity.badRequest().body("User cannot send request to himself!");
        }

        // if they are already friends we get message
        if(currentUser.hasFriend(person)){
            return ResponseEntity.ok("You are already friends with: "+person.getUsername());
        }
        // if request was already sent we also get message for that
        if(person.hasFriendPendedReq(currentUser)){
            return ResponseEntity.ok("Request have already sent to: "+person.getUsername());
        }

        // if two users are not friends yet only in that case we can send
        if (!currentUser.hasFriendSentReq(person)) {
            // using Sets in User class we manage requests
            currentUser.sentReq(person);

            return ResponseEntity.ok("Request was sent successfully to: " + person.getUsername());
        }

        // otherwise there is no any request to do
        return ResponseEntity.badRequest().body("There is no any request sent to or from user.");
    }

    @Transactional
    public ResponseEntity<?> cancelSentReq(HttpServletRequest request, Long id) {
        User currentUser = userService.getUserByTokenFromRequest(request);

        User person = userService.findById(id);
        if (person == null) {
            return ResponseEntity.notFound().build();
        }

        if (currentUser.hasFriendSentReq(person)) {
            currentUser.cancelSentReq(person);

            return ResponseEntity.ok("You successfully canceled request to: " + person.getUsername());
        }

        return ResponseEntity.badRequest().body("There is no any request sent to or from user.");
    }

    @Transactional
    public ResponseEntity<?> acceptReq(HttpServletRequest request, Long id) {
        //the same for accept we checking all conditions
        // and only  after that we accept the request from user

        User currentUser = userService.getUserByTokenFromRequest(request);

        User person = userService.findById(id);
        if (person == null) {
            return ResponseEntity.notFound().build();
        }
        if(currentUser.getId().equals(person.getId())){
            return ResponseEntity.badRequest().body("Not allowed to accept yourself as a friend.");
        }
        if (!currentUser.hasFriend(person) && currentUser.hasFriendPendedReq(person)) {

            currentUser.acceptPendingReq(person);

            return ResponseEntity.ok("You successfully added: " + person.getUsername());
        }

        return ResponseEntity.badRequest().body("There is no any request sent to or from user.");
    }

    @Transactional
    public ResponseEntity<?> rejectReq(HttpServletRequest request, Long id) {
        User currentUser = userService.getUserByTokenFromRequest(request);

        User person = userService.findById(id);
        if (person == null) {
            return ResponseEntity.notFound().build();
        }
        if(currentUser.getId().equals(person.getId())){
            return ResponseEntity.badRequest().body("Not allowed to reject this request.");
        }
        if (currentUser.hasFriendPendedReq(person)) {
            currentUser.rejectPendingReq(person);
            return ResponseEntity.ok("You rejected request from: " + person.getUsername());
        }

        return ResponseEntity.badRequest().body("There is no any request sent to or from user.");
    }

    public ResponseEntity<?> getFriends(HttpServletRequest request) {
        // from request we take user if exist
        User currentUser = userService.getUserByTokenFromRequest(request);

        // check it here
        if(currentUser.getFriends() == null){
            return ResponseEntity.notFound().build();
        }

        // and return response in Json format for postman
        return ResponseEntity.ok("\"friends\": " +currentUser.getFriends().stream()
                .map(s -> "\"id\": "+"\""+s.getId()+"\","+
                        "\"username\": "+ "\""+s.getUsername()+"\","+
                        "\"email\": " +  "\""+s.getEmail()+"\"")
                .collect(Collectors.toSet()));
    }

    public ResponseEntity<?> JSON_format(User user){
        Set<String> sentReqs = user.getSentRequests().stream()
                .map(s -> "\"id\": "+"\""+s.getId()+"\","+
                        "\"username\": "+ "\""+s.getUsername()+"\","+
                        "\"email\": " +  "\""+s.getEmail()+"\"\n")
                .collect(Collectors.toSet());

        Set<String> pendedReqs = user.getPendingRequests().stream()
                .map(s -> "\"id\": "+"\""+s.getId()+"\","+
                        "\"username\": "+ "\""+s.getUsername()+"\","+
                        "\"email\": " +  "\""+s.getEmail()+"\"\n")
                .collect(Collectors.toSet());

        Set<String> friends = user.getFriends().stream()
                .map(s -> "\"id\": "+"\""+s.getId()+"\","+
                        "\"username\": "+ "\""+s.getUsername()+"\","+
                        "\"email\": " +  "\""+s.getEmail()+"\"\n")
                .collect(Collectors.toSet());

        Set<String> profileInfo = Collections.singleton("\"id\": " + "\"" + user.getId() + "\"," +
                "\"username\": " + "\"" + user.getUsername() + "\"," +
                "\"email\": " + "\"" + user.getEmail() + "\","+
                "\"visibility\": " +  "\""+user.getRestrictions()+"\"");

        return ResponseEntity.ok("\"Profile info\": "+profileInfo + ",\"friends\": " +
                friends +","+ "\"sentRequests\": " + sentReqs +","+
                "\"pendedRequests\": " + pendedReqs);
    }

}