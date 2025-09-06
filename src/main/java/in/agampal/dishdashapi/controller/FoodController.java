package in.agampal.dishdashapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.agampal.dishdashapi.dto.ApiResponse;
import in.agampal.dishdashapi.exception.ResourceNotFoundException;
import in.agampal.dishdashapi.io.FoodRequest;
import in.agampal.dishdashapi.io.FoodResponse;
import in.agampal.dishdashapi.service.FoodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@AllArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@Tag(name = "Food Management", description = "APIs for managing food items")
public class FoodController {

    private final FoodService foodService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Add a new food item", description = "Creates a new food item with image upload")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Food item created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<FoodResponse>> addFood(
            @Parameter(description = "Food details in JSON format", required = true)
            @RequestPart("food") String foodString,
            @Parameter(description = "Food image file", required = true)
            @RequestPart("file") MultipartFile file) {
        try {
            log.info("Adding new food item");
            
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File is required", "FILE_REQUIRED"));
            }
            
            if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File size too large. Maximum allowed size is 10MB", "FILE_TOO_LARGE"));
            }
            
            // Parse and validate food request
            FoodRequest request = objectMapper.readValue(foodString, FoodRequest.class);
            FoodResponse response = foodService.addFood(request, file);
            log.info("Food item added successfully with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Food item added successfully"));
                    
        } catch (JsonProcessingException ex) {
            log.error("Invalid JSON format in food request", ex);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid JSON format", "INVALID_JSON"));
        } catch (Exception ex) {
            log.error("Error adding food item", ex);
            throw ex;
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FoodResponse>>> readFoods() {
        try {
            log.info("Fetching all food items");
            List<FoodResponse> foods = foodService.readFoods();
            log.info("Successfully fetched {} food items", foods.size());
            return ResponseEntity.ok(ApiResponse.success(foods, "Food items retrieved successfully"));
        } catch (Exception ex) {
            log.error("Error fetching food items", ex);
            throw ex;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodResponse>> readFood(@PathVariable String id) {
        try {
            log.info("Fetching food item with ID: {}", id);
            FoodResponse food = foodService.readFood(id);
            log.info("Successfully fetched food item: {}", food.getName());
            return ResponseEntity.ok(ApiResponse.success(food, "Food item retrieved successfully"));
        } catch (ResourceNotFoundException ex) {
            log.warn("Food item not found with ID: {}", id);
            throw ex;
        } catch (Exception ex) {
            log.error("Error fetching food item with ID: {}", id, ex);
            throw ex;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFood(@PathVariable String id) {
        try {
            log.info("Deleting food item with ID: {}", id);
            foodService.deleteFood(id);
            log.info("Food item deleted successfully with ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(null, "Food item deleted successfully"));
        } catch (ResourceNotFoundException ex) {
            log.warn("Food item not found for deletion with ID: {}", id);
            throw ex;
        } catch (Exception ex) {
            log.error("Error deleting food item with ID: {}", id, ex);
            throw ex;
        }
    }
}
