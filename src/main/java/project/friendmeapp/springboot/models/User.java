package project.friendmeapp.springboot.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", 
    uniqueConstraints = { 
      @UniqueConstraint(columnNames = "username"),
      @UniqueConstraint(columnNames = "email") 
    })
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter @Setter
  private Long id;

  @NotBlank
  @Size(max = 20)
  @Getter @Setter
  private String username;

  @NotBlank
  @Size(max = 50)
  @Email
  @Getter @Setter
  private String email;

  @NotBlank
  @Size(max = 120)
  @Getter @Setter
  private String password;

  @Size(max = 20)
  @Getter @Setter
  private String restrictions;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(  name = "user_roles", 
        joinColumns = @JoinColumn(name = "user_id"), 
        inverseJoinColumns = @JoinColumn(name = "role_id"))
  @Getter @Setter
  private Set<Role> roles = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "sent_requests",
          joinColumns = @JoinColumn(name = "user_id"),
          inverseJoinColumns = @JoinColumn(name = "sendingToUser_id"))
  @Getter @Setter
  private Set<User> sentRequests = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "pending_requests",
          joinColumns = @JoinColumn(name = "user_id"),
          inverseJoinColumns = @JoinColumn(name = "pendingFromUser_id"))
  @Getter @Setter
  private Set<User> pendingRequests = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "user_friends",
          joinColumns = @JoinColumn(name = "user_id"),
          inverseJoinColumns = @JoinColumn(name = "friend_id"))
  @Getter @Setter
  private Set<User> friends = new HashSet<>();

  public User() {
  }

  public User(String username, String email, String password, String restrictions) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.restrictions = restrictions;
  }

  public boolean hasFriendSentReq(User friend) {
    return sentRequests.contains(friend);
  }

  public boolean hasFriendPendedReq(User friend) {
    return pendingRequests.contains(friend);
  }

  public boolean hasFriend(User friend) {
    return friends.contains(friend);
  }

  public void sentReq(User makeFriendUser) {
    // we have used "Set" for : sent requests, pending requests, friends
    // these Sets automatically adds id of users to database because
    // they are annotated with many to many relationship

    // after calling this method
    // for example we are user1 and send request to user2
    // in this case for "Set" of user1 we are adding user2
    // and for "Set" of pending requests of user2 we add user1
    sentRequests.add(makeFriendUser);
    makeFriendUser.pendingRequests.add(this);
  }

  public void cancelSentReq(User cancelMakeFriendUser) {
    sentRequests.remove(cancelMakeFriendUser);
    cancelMakeFriendUser.pendingRequests.remove(this);
  }

  public void acceptPendingReq(User sentReqUser){
    // in order to accept we remove the user1 from "Set" pending requests of user2
    pendingRequests.remove(sentReqUser);
    // and from "Set" of sent requests remove user2
    sentReqUser.sentRequests.remove(this);
    // after that we add to "Set" of friends user1 and user2 for each other
    friends.add(sentReqUser);
    sentReqUser.friends.add(this);
  }

  public void rejectPendingReq(User sentReqUser){
    pendingRequests.remove(sentReqUser);
    sentReqUser.sentRequests.remove(this);
  }

}
