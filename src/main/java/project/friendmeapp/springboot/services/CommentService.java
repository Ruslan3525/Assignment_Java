package project.friendmeapp.springboot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.friendmeapp.springboot.models.Comment;
import project.friendmeapp.springboot.repository.CommentRepository;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    CommentRepository commentRepository;

    public void saveComment(Comment comment){
        commentRepository.save(comment);
    }

    public List<Comment> getComments(){
        return commentRepository.findAll();
    }
}
