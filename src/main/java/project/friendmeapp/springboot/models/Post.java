package project.friendmeapp.springboot.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @Getter @Setter
    private User user;

    @Column(columnDefinition = "TEXT")
    @Getter @Setter
    private String content;

    @NotNull
    @Getter @Setter
    private String visibility;

    @Getter @Setter
    private Boolean commentAllowed;

    @Column(name = "createdDate", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Getter @Setter
    private Date createdDate;

    public Post() {
    }

}
