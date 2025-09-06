package in.agampal.dishdashapi.controller;

import in.agampal.dishdashapi.io.UserRequest;
import in.agampal.dishdashapi.io.UserResponse;
import in.agampal.dishdashapi.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // Admin creates a new user
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody UserRequest request) {
        return userService.registerUser(request);
    }
}

