package com.taskmanager.config.mongodb;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB configuration enabling:
 * - @CreatedDate / @LastModifiedDate auditing on entities
 * - Explicit repository scanning for clarity
 */
@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = {
        "com.taskmanager.users.repository",
        "com.taskmanager.tasks.repository"
})
public class MongoConfig {
    // Index creation is handled via application.yml: auto-index-creation: true
    // For production, disable auto-index-creation and use explicit index definitions.
}
