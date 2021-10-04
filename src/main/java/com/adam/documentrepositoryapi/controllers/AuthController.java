package com.adam.documentrepositoryapi.controllers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.validation.Valid;

import com.adam.documentrepositoryapi.models.ERole;
import com.adam.documentrepositoryapi.models.Role;
import com.adam.documentrepositoryapi.models.User;
import com.adam.documentrepositoryapi.payload.request.LoginRequest;
import com.adam.documentrepositoryapi.payload.request.RegisterUserRequest;
import com.adam.documentrepositoryapi.payload.response.JwtResponse;
import com.adam.documentrepositoryapi.payload.response.MessageResponse;
import com.adam.documentrepositoryapi.repository.RoleRepository;
import com.adam.documentrepositoryapi.repository.UserRepository;
import com.adam.documentrepositoryapi.security.jwt.JwtUtils;
import com.adam.documentrepositoryapi.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Transactional
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

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                roles));
    }

    @PostMapping("/change/password/{userId}")
    public ResponseEntity<?> changePassword(@RequestParam("currentPassword") String currentPassword, @RequestParam("newPassword") String newPassword, @RequestParam("newPasswordAgain") String newPasswordAgain, @PathVariable("userId") Long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: User with id " + userId + " does not exist!"));
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userRepository.getById(userId).getUsername(), currentPassword)
        );
        if (!newPassword.equals(newPasswordAgain)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Entered passwords do not match"));
        }
        User user = userRepository.getById(userId);
        user.setPassword(encoder.encode(newPassword));
        return ResponseEntity.ok(new MessageResponse("Password changed successfully!"));
    }

    @PostMapping("/signup")
    @PreAuthorize("hasRole('ROLE_ADD_USERS')")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUserRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        User user = new User(signUpRequest.getUsername(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();

        if (strRoles != null) {
            strRoles.forEach(role -> {
                switch (role) {
                    case "ROLE_VIEW_USERS":
                        Role viewUsersRole = roleRepository.findByName(ERole.ROLE_VIEW_USERS)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        user.getRoles().add(viewUsersRole);

                        break;
                    case "ROLE_ADD_USERS":
                        Role addUsersRole = roleRepository.findByName(ERole.ROLE_ADD_USERS)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        user.getRoles().add(addUsersRole);

                        break;
                    case "ROLE_EDIT_USERS":
                        Role editUsersRole = roleRepository.findByName(ERole.ROLE_EDIT_USERS)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        user.getRoles().add(editUsersRole);

                        break;
                    case "ROLE_DELETE_USERS":
                        Role deleteUsersRole = roleRepository.findByName(ERole.ROLE_DELETE_USERS)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        user.getRoles().add(deleteUsersRole);

                        break;
                    case "ROLE_VIEW_DOCUMENTS":
                        Role viewDocumentsRole = roleRepository.findByName(ERole.ROLE_VIEW_DOCUMENTS)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        user.getRoles().add(viewDocumentsRole);

                        break;
                    case "ROLE_ADD_DOCUMENTS":
                        Role addDocumentsRole = roleRepository.findByName(ERole.ROLE_ADD_DOCUMENTS)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        user.getRoles().add(addDocumentsRole);

                        break;
                    case "ROLE_EDIT_DOCUMENTS":
                        Role editDocumentsRole = roleRepository.findByName(ERole.ROLE_EDIT_DOCUMENTS)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        user.getRoles().add(editDocumentsRole);

                        break;
                    case "ROLE_DELETE_DOCUMENTS":
                        Role deleteDocumentsRole = roleRepository.findByName(ERole.ROLE_DELETE_DOCUMENTS)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        user.getRoles().add(deleteDocumentsRole);

                        break;

                }
            });
        }

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
