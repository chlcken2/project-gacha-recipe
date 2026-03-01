-- Reference tables
CREATE TABLE IF NOT EXISTS difficulties (
                                            id TINYINT PRIMARY KEY,
                                            name VARCHAR(20) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS purposes (
                                        id TINYINT PRIMARY KEY AUTO_INCREMENT,
                                        name VARCHAR(50) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS cuisines (
                                        id TINYINT PRIMARY KEY AUTO_INCREMENT,
                                        name VARCHAR(50) NOT NULL UNIQUE
    );

-- Users (kept minimal - not our responsibility)
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    UNIQUE KEY uk_nickname (nickname),
    UNIQUE KEY uk_email (email),
    INDEX idx_deleted (deleted_at)
    ) ENGINE=InnoDB;

-- Recipes
CREATE TABLE IF NOT EXISTS recipes (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       user_id BIGINT NOT NULL,
                                       title VARCHAR(255) NOT NULL,
    title_image VARCHAR(512) NOT NULL,
    description TEXT,
    cooking_time INT,
    difficulty_id TINYINT NOT NULL,
    purpose_id TINYINT NOT NULL,
    cuisine_id TINYINT NOT NULL,
    is_ai_generated BOOLEAN DEFAULT TRUE,
    is_public BOOLEAN NOT NULL DEFAULT false,
    view_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    popularity_score INT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_recipe_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (difficulty_id) REFERENCES difficulties(id),
    FOREIGN KEY (purpose_id) REFERENCES purposes(id),
    FOREIGN KEY (cuisine_id) REFERENCES cuisines(id),
    INDEX idx_popularity (popularity_score DESC),
    INDEX idx_user (user_id),
    INDEX idx_created (created_at),
    INDEX idx_deleted (deleted_at)
    ) ENGINE=InnoDB;

-- Recipe Ingredients
CREATE TABLE IF NOT EXISTS recipe_ingredients (
                                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                  recipe_id BIGINT NOT NULL,
                                                  name VARCHAR(255) NOT NULL,
    normalized_name VARCHAR(100) NOT NULL,
    quantity DECIMAL(10,2),
    unit VARCHAR(50),
    is_ai_generated BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ri_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    INDEX idx_recipe (recipe_id),
    INDEX idx_normalized (normalized_name)
    ) ENGINE=InnoDB;

-- User Ingredients
CREATE TABLE IF NOT EXISTS user_ingredients (
                                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                user_id BIGINT NOT NULL,
                                                name VARCHAR(255) NOT NULL,
    normalized_name VARCHAR(100) NOT NULL,
    quantity DECIMAL(10,2),
    unit VARCHAR(50),
    type TINYINT NOT NULL COMMENT '1=ingredient, 2=sauce',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_ui_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_normalized (user_id, normalized_name),
    INDEX idx_user (user_id),
    INDEX idx_deleted (deleted_at)
    ) ENGINE=InnoDB;

-- Ingredient Aliases
CREATE TABLE IF NOT EXISTS ingredient_aliases (
                                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                  alias_name VARCHAR(255) NOT NULL,
    normalized_name VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_alias (alias_name),
    INDEX idx_normalized (normalized_name)
    ) ENGINE=InnoDB;

-- Master Ingredients
CREATE TABLE IF NOT EXISTS ingredients (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           normalized_name VARCHAR(100) NOT NULL,
    UNIQUE KEY uk_normalized (normalized_name)
    ) ENGINE=InnoDB;

-- Recipe Nutrients
CREATE TABLE IF NOT EXISTS recipe_nutrients (
                                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                recipe_id BIGINT NOT NULL,
                                                nutrient_name VARCHAR(100) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    unit VARCHAR(20),
    CONSTRAINT fk_rn_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    INDEX idx_recipe (recipe_id)
    ) ENGINE=InnoDB;

-- Recipe Steps
CREATE TABLE IF NOT EXISTS recipe_steps (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            recipe_id BIGINT NOT NULL,
                                            step_order INT NOT NULL,
                                            content TEXT NOT NULL,
                                            CONSTRAINT fk_step_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    UNIQUE KEY uk_recipe_step (recipe_id, step_order),
    INDEX idx_recipe (recipe_id)
    ) ENGINE=InnoDB;

-- Recipe Likes
CREATE TABLE IF NOT EXISTS recipe_likes (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            user_id BIGINT NOT NULL,
                                            recipe_id BIGINT NOT NULL,
                                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                            CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_like_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_recipe (user_id, recipe_id),
    INDEX idx_recipe (recipe_id)
    ) ENGINE=InnoDB;

-- Recipe View Log
CREATE TABLE IF NOT EXISTS recipe_view_log (
                                               recipe_id BIGINT NOT NULL,
                                               user_id BIGINT NOT NULL,
                                               created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                               PRIMARY KEY (recipe_id, user_id),
    CONSTRAINT fk_view_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    CONSTRAINT fk_view_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

-- Recipe Comments
CREATE TABLE IF NOT EXISTS recipe_comments (
                                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               recipe_id BIGINT NOT NULL,
                                               user_id BIGINT NOT NULL,
                                               content TEXT NOT NULL,
                                               created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                               updated_at DATETIME NULL,
                                               deleted_at DATETIME NULL,
                                               CONSTRAINT fk_comment_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_recipe_created (recipe_id, created_at DESC),
    INDEX idx_deleted (deleted_at)
    ) ENGINE=InnoDB;
