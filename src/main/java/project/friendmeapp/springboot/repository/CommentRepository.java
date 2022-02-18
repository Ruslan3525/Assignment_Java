package project.friendmeapp.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.friendmeapp.springboot.models.Comment;
import project.friendmeapp.springboot.models.Post;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
}
