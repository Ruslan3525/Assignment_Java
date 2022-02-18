package project.friendmeapp.springboot.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import project.friendmeapp.springboot.models.ERole;
import project.friendmeapp.springboot.models.Role;
import project.friendmeapp.springboot.models.User;
import project.friendmeapp.springboot.payload.request.LoginRequest;
import project.friendmeapp.springboot.payload.request.SignupRequest;
import project.friendmeapp.springboot.payload.response.JwtResponse;
import project.friendmeapp.springboot.payload.response.MessageResponse;
import project.friendmeapp.springboot.repository.RoleRepository;
import project.friendmeapp.springboot.repository.UserRepository;
import project.friendmeapp.springboot.security.jwt.JwtUtils;
import project.friendmeapp.springboot.services.UserDetailsImpl;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    // when we created a user and added it to database, we are able to login using two fields:
    // username and password

    // this authentication interface used to ATTEMPT(try) to authenticate user's username and password
    // passed in UsernamePasswordAuthenticationToken
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    // securityContextHolder stores user details who is authenticated
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // then method of class jwtUtils generates token
    String jwt = jwtUtils.generateJwtToken(authentication);

    // we get the user from storage of authentication using method getPrincipal
    // now in userDetails object we have user's info
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    List<String> roles = userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    // it's returning user's info and access token
    return ResponseEntity.ok(new JwtResponse(jwt,
                         userDetails.getId(), 
                         userDetails.getUsername(), 
                         userDetails.getEmail(),
                         roles));
  }

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    // first, using userRepository we check if username exist in database
    // if there is user with the same username we should state that Username is already exist
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity
          .badRequest()
          .body(new MessageResponse("Error: Username is already taken!"));
    }
    // second, also using userRepository we check if email exist in database
    // if there is user with the same username we should state that Email is already exist
    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity
          .badRequest()
          .body(new MessageResponse("Error: Email is already in use!"));
    }

    // by default restriction for user profile is "all", if it's not sent we set it as "all"
    if(signUpRequest.getRestrictions() == null){
      signUpRequest.setRestrictions("all");
    }

    // if user is unique, we will create new user's account
    // During creation password in database should be encoded,
    // so that using PasswordEncoder class we are encoding the password to secret format
    User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
               encoder.encode(signUpRequest.getPassword()), signUpRequest.getRestrictions());

    // there are 2 roles for user it's admin and user,
    // but we use only user role to not make it complex
    Set<String> strRoles = signUpRequest.getRole();
    Set<Role> roles = new HashSet<>();

    // from request we should be confident that user sent correct role
    // in next conditional operations we check it

    // first condition check if role is correct
    if (strRoles == null) {
      Role userRole = roleRepository.findByName(ERole.ROLE_USER)
          .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
      roles.add(userRole);
    } else {
      // second and third conditions defines each role from request
      strRoles.forEach(role -> {
        //if it's admin was sent we add to Set of roles 'admin'
        if ("admin".equals(role)) {
          Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                  .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(adminRole);
        } else {
          //otherwise, we add to Set of roles 'user'
          Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                  .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(userRole);
        }
      });
    }

    // then if roles is correct we set role for user's account
    user.setRoles(roles);

    // after checking each fields of user we add them to database using userRepository
    userRepository.save(user);

    // at the end, it returns message that user registered
    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }

}
