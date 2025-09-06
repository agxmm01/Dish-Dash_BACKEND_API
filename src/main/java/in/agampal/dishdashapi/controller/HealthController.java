package in.agampal.dishdashapi.controller;

import in.agampal.dishdashapi.dto.ApiResponse;
import in.agampal.dishdashapi.entity.UserEntity;
import in.agampal.dishdashapi.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@AllArgsConstructor
@Slf4j
public class HealthController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("timestamp", LocalDateTime.now());
        healthData.put("service", "Food Delivery API");
        healthData.put("version", "1.0.0");
        
        return ResponseEntity.ok(ApiResponse.success(healthData, "Service is healthy"));
    }

    @GetMapping("/mongodb")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkMongoDB() {
        Map<String, Object> response = new HashMap<>();
        try {
            long userCount = userRepository.count();
            response.put("status", "UP");
            response.put("database", "MongoDB");
            response.put("userCount", userCount);
            response.put("message", "MongoDB connection is working");
            log.info("MongoDB health check successful. User count: {}", userCount);
            return ResponseEntity.ok(ApiResponse.success(response, "MongoDB is healthy"));
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("database", "MongoDB");
            response.put("error", e.getMessage());
            response.put("message", "MongoDB connection failed");
            log.error("MongoDB health check failed", e);
            return ResponseEntity.status(500).body(ApiResponse.error("MongoDB health check failed"));
        }
    }

    @GetMapping("/test-save")
    public ResponseEntity<Map<String, Object>> testSave() {
        Map<String, Object> response = new HashMap<>();
        try {
            UserEntity testUser = UserEntity.builder()
                    .name("Test User")
                    .email("test@example.com")
                    .password("test123")
                    .build();
            
            UserEntity savedUser = userRepository.save(testUser);
            response.put("status", "SUCCESS");
            response.put("message", "Test user saved successfully");
            response.put("userId", savedUser.getId());
            log.info("Test user saved with ID: {}", savedUser.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "FAILED");
            response.put("error", e.getMessage());
            response.put("message", "Failed to save test user");
            log.error("Test save failed", e);
            return ResponseEntity.status(500).body(response);
        }
    }
}
