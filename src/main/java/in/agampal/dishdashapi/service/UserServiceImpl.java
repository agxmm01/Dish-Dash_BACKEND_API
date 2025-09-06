package in.agampal.dishdashapi.service;

import in.agampal.dishdashapi.entity.UserEntity;
import in.agampal.dishdashapi.io.UserRequest;
import in.agampal.dishdashapi.io.UserResponse;
import in.agampal.dishdashapi.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @Transactional
    public UserResponse registerUser(UserRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());
        UserEntity newUser = convertToEntity(request);
        log.info("Created user entity: {}", newUser);
        newUser = userRepository.save(newUser);
        log.info("User saved to database with ID: {}", newUser.getId());
        return convertToResponse(newUser);
    }

    @Override
    public String findByUserId() {
        String loggedInUserEmail = authenticationFacade.getAuthentication().getName();
        UserEntity loggedInUser = userRepository.findByEmail(loggedInUserEmail).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return loggedInUser.getId();
    }

    private UserEntity convertToEntity(UserRequest request) {
        return UserEntity.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .build();
    }

    private UserResponse convertToResponse(UserEntity registeredUser) {
        return UserResponse.builder()
                .id(registeredUser.getId())
                .name(registeredUser.getName())
                .email(registeredUser.getEmail())
                .build();
    }
}
