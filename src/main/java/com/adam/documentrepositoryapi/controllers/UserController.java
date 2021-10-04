package com.adam.documentrepositoryapi.controllers;

import com.adam.documentrepositoryapi.models.ERole;
import com.adam.documentrepositoryapi.models.Role;
import com.adam.documentrepositoryapi.models.User;
import com.adam.documentrepositoryapi.payload.request.EditUserRequest;
import com.adam.documentrepositoryapi.payload.response.MessageResponse;
import com.adam.documentrepositoryapi.repository.RoleRepository;
import com.adam.documentrepositoryapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Transactional
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @PostMapping("/edit/{userId}")
    @PreAuthorize("hasRole('ROLE_EDIT_USERS')")
    public ResponseEntity<?> editUser(@Valid @RequestBody EditUserRequest userRequest, @PathVariable("userId") Long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: User with id " + userId + " does not exist!"));
        }
        if(userRepository.existsByUsername(userRequest.getUsername()) && userRepository.findByUsername(userRequest.getUsername()).get().getId() != userId) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username " + userRequest.getUsername() + " is already taken!"));
        }
        Set<String> strRoles = userRequest.getRoles();
        User user = userRepository.findById(userId).get();
        user.setUsername(userRequest.getUsername());
        user.getRoles().clear();

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


        return ResponseEntity.ok(new MessageResponse("User updated successfully!"));
    }

    @GetMapping("/get")
    @PreAuthorize("hasRole('ROLE_VIEW_USERS')")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasRole('ROLE_DELETE_USERS')")
    public ResponseEntity<?> deleteUser(@Valid @PathVariable("userId") Long userId) {
        if(!userRepository.existsById(userId)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: User with id " + userId + " does not exist!"));
        }
        userRepository.deleteById(userId);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
    }
}
