package project.friendmeapp.springboot.sockets.console.posts;

import project.friendmeapp.springboot.models.Post;
import project.friendmeapp.springboot.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostServiceImpl {

    private Long last_id = 0L;
    private Long numberOfPosts = 0L;

    public Post savePost(String content, String visibility, String commentPermission, User user) {

        // savind post using insert into query in prepared statement
        try {
            PreparedStatement preparedStatement =
                    JdbcConnection.getConnection().prepareStatement
                            ("INSERT INTO `posts` (`id`, `content`, `created_date`, `visibility`, `user_id`, `comment_allowed`)" +
                                    " VALUES (?, ?, ?, ?, ?, ?) ", Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setNull(1, Types.BIGINT);
            preparedStatement.setString(2, content);
            preparedStatement.setNull(3, Types.TIMESTAMP);
            preparedStatement.setString(4, visibility);
            preparedStatement.setLong(5, user.getId());
            preparedStatement.setBoolean(6, Boolean.parseBoolean(commentPermission));

            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if(rs.next()){
                // get last id
                last_id = rs.getLong(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        // putting data to Post object and returning it
        Post post = new Post();

        post.setId(last_id);
        post.setUser(user);
        post.setContent(content);
        post.setCreatedDate(new Date());
        post.setCommentAllowed(Boolean.valueOf(commentPermission));
        post.setVisibility(visibility);

        return post;
    }

    public void savePostForUser(Long userId, Long postId) {
        // in order to get posts which user already seen
        // we should save it everytime to database in table check_in
        try {
            PreparedStatement preparedStatement =
                    JdbcConnection.getConnection().prepareStatement
                            ("INSERT INTO `check_in` (`user_id`, `saved_last_post_id`)" +
                                    " VALUES (?, ?) ");

            preparedStatement.setLong(1,userId);
            preparedStatement.setLong(2,postId);

            preparedStatement.executeUpdate();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public Long getPostCount() {
        // query to find count of posts
        try {
            Statement statement = JdbcConnection.getConnection().createStatement();
            String query = "SELECT COUNT(*) as row_count FROM posts";
            ResultSet resultSet = statement.executeQuery(query);

            resultSet.next();

            numberOfPosts = resultSet.getLong("row_count");

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return numberOfPosts;
    }

    public List<Post> getAllPostsFromUserPostId(Long id){
        // query to get all posts from id of post
        List<Post> posts = new ArrayList<>();

        try {
            Statement statement = JdbcConnection.getConnection().createStatement();
            String query = "SELECT id, content,visibility,comment_allowed, user_id FROM posts WHERE id > "+id;
            ResultSet resultSet = statement.executeQuery(query);
            UserServiceImpl userService = new UserServiceImpl();

            while (resultSet.next()){
                Post post = new Post();

                post.setId(resultSet.getLong("id"));
                post.setContent(resultSet.getString("content"));
                post.setVisibility(resultSet.getString("visibility"));
                post.setCommentAllowed(resultSet.getBoolean("comment_allowed"));;
                post.setUser(userService.getUserById(resultSet.getLong("user_id")));
                posts.add(post);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return posts;
    }

    public Long getUpdatedPost(Long userId) {
        // we saved posts to check_in table in database and should be able to get them
        // query to return these post's id using user_id
        long lastSavedPost_id = 0L;
        try {
            PreparedStatement preparedStatement =
                    JdbcConnection.getConnection().prepareStatement
                            ("SELECT saved_last_post_id from check_in where user_id = ?");

            preparedStatement.setLong(1,userId);
            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()){
                lastSavedPost_id = rs.getLong("saved_last_post_id");
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return lastSavedPost_id;
    }

    public Boolean userHasUpdatedPosts(Long userId){
        // checking query for is post already updated for user using user_id

        Long postId = null;

        try {
            PreparedStatement preparedStatement =
                    JdbcConnection.getConnection().prepareStatement
                            ("SELECT saved_last_post_id from check_in where user_id = ?");

            preparedStatement.setLong(1,userId);
            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()){
                postId = rs.getLong("saved_last_post_id");
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return postId != null;
    }

    public void updateNewPostList(Long userId, Long postId) {
        // query to update post id and user id in table check_in

        try {
            PreparedStatement preparedStatement =
                    JdbcConnection.getConnection().prepareStatement
                            ("UPDATE `check_in` SET `saved_last_post_id` = ? WHERE `check_in`.`user_id` = ?");

            preparedStatement.setLong(1,postId);
            preparedStatement.setLong(2,userId);

            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
