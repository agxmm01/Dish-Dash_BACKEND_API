package in.agampal.dishdashapi.io;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodRequest {

    @NotBlank(message = "Food name is required")
    @Size(min = 3, max = 100, message = "Food name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "9999.99", message = "Price must be less than 10000")
    private Double price;

    @NotBlank(message = "Category is required")
    @Pattern(regexp = "^(Biryani|Cake|Burger|Pizza|Rolls|Salad|Ice cream)$", 
             message = "Category must be one of: Biryani, Cake, Burger, Pizza, Rolls, Salad, Ice cream")
    private String category;
}
