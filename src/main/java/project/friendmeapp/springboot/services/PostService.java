package project.friendmeapp.springboot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import project.friendmeapp.springboot.models.Comment;
import project.friendmeapp.springboot.models.Post;
import project.friendmeapp.springboot.models.User;
import project.friendmeapp.springboot.repository.PostRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {
    @Autowired
    PostRepository postRepository;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @Autowired
    FriendService friendService;

    public ResponseEntity<?> addPost(HttpServletRequest request, Post newPost){
        // from body we have content, visibility, permission for comment
        Post post = new Post();
        // from request we get user
        User user = userService.getUserByTokenFromRequest(request);

        //here we are checking new post and user for nulling
        if(newPost != null && user != null){
            // if everything okay we save post to database using post Repository
            post.setUser(user);
            post.setContent(newPost.getContent());
            post.setCommentAllowed(newPost.getCommentAllowed());
            post.setVisibility(newPost.getVisibility());
            postRepository.save(post);
            return ResponseEntity.ok("Post added successfully.");
        }


        // otherwise post wasn't added message
        return ResponseEntity.badRequest().body("Post wasn't added.");
    }

    public ResponseEntity<?> getPost(HttpServletRequest request){
        String visibility = "all";

        // get user using token from request
        User currentUser = userService.getUserByTokenFromRequest(request);

        // check for existing if he authenticated change visibility to "authorzied"
        if(userService.checkExistsByUsername(currentUser)){
            visibility = "authorized";
        }


        String finalVisibility = visibility;

        // get all post we final visibility
        List<Post> posts = postRepository.findAll()
                .stream().filter(s->s.getVisibility().equals(finalVisibility))
                .collect(Collectors.toList());

        // if visibility "authorized" we should add also posts for all and friends
        if(visibility.equals("authorized")){
            List<Post> postsForAll = postRepository.findAll()
                    .stream().filter(s->s.getVisibility().equals("all"))
                    .collect(Collectors.toList());

            List<Post> friendPosts = postRepository.findAll()
                    .stream().filter(s->s.getUser().hasFriend(currentUser)&&s.getVisibility().equals("friends"))
                    .collect(Collectors.toList());

            posts.addAll(friendPosts);
            posts.addAll(postsForAll);
        }

        // if posts not empty we output the info in JSON format
        if (!posts.isEmpty()){
            return JSON_format(posts, visibility, currentUser);
        }

        // otherwise posts not found
        return ResponseEntity.badRequest().body("Posts not found.");
    }

    public ResponseEntity<?> addCommentToPost(HttpServletRequest request, Comment comment, Long postId) {
        // comments addition the same as post addition

        // we take user if exist by token in request
        User currentUser = userService.getUserByTokenFromRequest(request);

        // search for post in database using post Repository
        Post post = postRepository.findById(postId)
                .orElse(null);

        // post not found message
        if(post == null){
            return ResponseEntity.badRequest().body("Post not found with id: "+postId);
        }

        //if comment allowed for post
        // and if content of comment is not empty
        // we add comment to post then save it to database
        // using comment Repository in comment service
        if(post.getCommentAllowed()){
            if(comment.getComment() == null){
                return ResponseEntity.badRequest().body("Comment's content is empty.");
            }
            comment.setUser(currentUser);
            comment.setPost(post);
            commentService.saveComment(comment);

            return ResponseEntity.badRequest().body("Comment is added successfully to post: "+postId);
        }

        return ResponseEntity.badRequest().body("Comment is not allowed for this post.");
    }

    public ResponseEntity<?> getUserPosts(Long id, String status) {
        // searching user using id from parameter
        User userById = userService.findById(id);

        // get posts with user and status
        List<Post> userPosts = postRepository.findPostByUserAndVisibility(userById, status);

        // if status "authorized" we should add posts for all users
        if(status.equals("authorized")){
            List<Post> allPosts = postRepository.findPostByUserAndVisibility(userById, "all");

            userPosts.addAll(allPosts);
        }
        // if status for "friends" we add all posts and posts for "authorized" users
        if(status.equals("friends")){
            List<Post> allPosts = postRepository.findPostByUserAndVisibility(userById, "all");
            List<Post> friendsPosts = postRepository.findPostByUserAndVisibility(userById, "authorized");

            userPosts.addAll(allPosts);
            userPosts.addAll(friendsPosts);
        }

        // post not found message if there are now any posts
        if(userPosts.isEmpty()){
            return ResponseEntity.badRequest().body("Posts not found.");
        }

        //otherwise in JSON format output posts
        return JSON_format(userPosts,"profile of", userById);
    }

    public ResponseEntity<?> JSON_format(List<Post> posts, String visibility, User currentUser){
        Set<String> response = posts.stream().map(s-> {
            assert currentUser != null;
            return "\"id\": "+"\""+s.getId()+"\","+
                    "\"user\": "+"\""+s.getUser().getUsername()+"\","+
                    "\"content\": "+ "\""+s.getContent()+"\","+
                    "\"visibility\": " +  "\""+s.getVisibility()+"\","+
                    "\"comments\": " +commentService.getComments()
                    .stream().filter(f->s.getId().equals(f.getPost().getId()))
                    .map(s2 -> "\"id\": "+"\""+ s2.getId()+"\","+
                            "\"comment\": "+ "\""+ s2.getComment()+"\","+
                            "\"commented username\": " +  "\""+ s2.getUser().getUsername()+"\"")
                    .collect(Collectors.toSet());
        }).collect(Collectors.toSet());
        return ResponseEntity.ok("\"Posts for "+visibility+" users\":" + response);
    }
}
