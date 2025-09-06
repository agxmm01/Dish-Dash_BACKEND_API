package in.agampal.dishdashapi.service;

import in.agampal.dishdashapi.io.UserRequest;
import in.agampal.dishdashapi.io.UserResponse;

public interface UserService {

    UserResponse registerUser(UserRequest request);

    String findByUserId();
}
