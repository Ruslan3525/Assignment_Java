package project.friendmeapp.springboot.payload.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;
import java.util.Set;

public class SignupRequest {
  @NotBlank
  @Size(min = 3, max = 20)
  @Getter @Setter
  private String username;

  @NotBlank
  @Size(max = 50)
  @Email
  @Getter @Setter
  private String email;

  @Getter @Setter
  private Set<String> role;

  @NotBlank
  @Size(min = 6, max = 40)
  @Getter @Setter
  private String password;

  @Size(max = 20)
  @Getter @Setter
  private String restrictions;

}
