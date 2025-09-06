package in.agampal.dishdashapi.config;

import in.agampal.dishdashapi.entity.FoodEntity;
import in.agampal.dishdashapi.entity.UserEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

@Configuration
public class DatabaseConfig {

    @Bean
    public String createIndexes(MongoTemplate mongoTemplate) {
        // Create indexes for FoodEntity
        IndexOperations foodIndexOps = mongoTemplate.indexOps(FoodEntity.class);
        
        // Index on category for faster filtering
        foodIndexOps.ensureIndex(new Index().on("category", org.springframework.data.domain.Sort.Direction.ASC));
        
        // Index on name for faster searching
        foodIndexOps.ensureIndex(new Index().on("name", org.springframework.data.domain.Sort.Direction.ASC));
        
        // Compound index for category and price
        foodIndexOps.ensureIndex(new Index().on("category", org.springframework.data.domain.Sort.Direction.ASC)
                .on("price", org.springframework.data.domain.Sort.Direction.ASC));
        
        // Text index for full-text search
        foodIndexOps.ensureIndex(new Index().on("name", org.springframework.data.domain.Sort.Direction.ASC)
                .on("description", org.springframework.data.domain.Sort.Direction.ASC));

        // Create indexes for UserEntity
        IndexOperations userIndexOps = mongoTemplate.indexOps(UserEntity.class);
        
        // Unique index on email
        userIndexOps.ensureIndex(new Index().on("email", org.springframework.data.domain.Sort.Direction.ASC).unique());
        
        // Index on name for faster searching
        userIndexOps.ensureIndex(new Index().on("name", org.springframework.data.domain.Sort.Direction.ASC));

        return "Indexes created successfully";
    }
}


