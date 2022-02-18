package project.friendmeapp.springboot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import project.friendmeapp.springboot.models.User;
import project.friendmeapp.springboot.repository.UserRepository;
import project.friendmeapp.springboot.security.jwt.JwtUtils;

import javax.servlet.http.HttpServletRequest;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtils jwtUtils;

    //cycling error not fixed by removing annotation @Autowired, it return null for services
    @Lazy
    @Autowired
    PostService postService;
    //cycling error not fixed by removing annotation @Autowired, it return null for services
    @Lazy
    @Autowired
    FriendService friendService;

    public ResponseEntity<?> getUserInfo(HttpServletRequest request, Long id){
        String status = "all";
        // get user by token from request
        User currentUser = getUserByTokenFromRequest(request);

        // find user by id from User Repository
        User userFromId = userRepository.findUserById(id);
        // get his restriction for profile
        String visibilityOfUser = userFromId.getRestrictions();

        //if user doesn't exist not found message
        if(!userRepository.existsByUsername(userFromId.getUsername())){
            return ResponseEntity.notFound().build();
        }

        // otherwise, set status "authorized"
        if(userRepository.existsByUsername(currentUser.getUsername())){
            status = "authorized";
        }

        // if user authorized and visibility of user friends return message
        // profile visible for friends and set status friends
        if(status.equals("authorized") && visibilityOfUser.equals("friends")){
            if(!currentUser.hasFriend(userFromId)){
                return ResponseEntity.badRequest().body("Profile is visible only for friends.");
            }
            status = "friends";
        }

        // if status "all" and visibility of user not "all"
        // return profile visible for authorized
        if(status.equals("all") && !visibilityOfUser.equals("all")){
            return ResponseEntity.badRequest().body("Profile is visible for authorized users.");
        }

        if(currentUser.equals(userFromId)){
            status = "friends";
        }

        // get post by parsing json format
        String posts = parseResponseEntity(postService.getUserPosts(id, status));
        // get friends by parsing json format
        String friends = parseResponseEntity(friendService.JSON_format(userFromId));

        // output the posts and friends
        return ResponseEntity.ok(posts+"\n\n"+friends);
    }

    public User getUserByTokenFromRequest(HttpServletRequest request){
        // get token from request
        String token = jwtUtils.parseJwt(request);

        // create object for user
        User currentUser = new User();

        // if token not null and token valid , find user
        if (token != null && jwtUtils.validateJwtToken(token)) {
            String username = jwtUtils.getUserNameFromJwtToken(token);
            if(userRepository.existsByUsername(username)){
                currentUser = userRepository.findByUsername(username).orElseThrow(
                        () -> new UsernameNotFoundException("User Not Found with username: " + username));
            }
        }
        return currentUser;
    }

    public String parseResponseEntity(ResponseEntity<?> entity){
        ResponseEntity<String> responseEntity = new ResponseEntity<>((String) entity.getBody(), HttpStatus.OK);

        return responseEntity.getBody();
    }

    public User findById(Long id){
        return userRepository.findUserById(id);
    }

    public Boolean checkExistsByUsername(User user){
        return userRepository.existsByUsername(user.getUsername());
    }
}
