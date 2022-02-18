package project.friendmeapp.springboot.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.friendmeapp.springboot.models.Post;
import project.friendmeapp.springboot.models.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post,Integer> {
    Optional<Post> findById(Long integer);
    List<Post> findPostByUserAndVisibility(User user, String visibility);
}
