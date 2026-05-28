SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS enterprise_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE enterprise_blog;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  nickname VARCHAR(64) NOT NULL,
  avatar VARCHAR(255),
  role VARCHAR(32) NOT NULL DEFAULT 'ADMIN',
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS blog_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  slug VARCHAR(80) NOT NULL UNIQUE,
  description VARCHAR(255),
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_sort(sort_order)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS blog_tag (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  slug VARCHAR(80) NOT NULL UNIQUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS blog_article (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(180) NOT NULL,
  slug VARCHAR(200) NOT NULL UNIQUE,
  summary VARCHAR(500),
  content LONGTEXT NOT NULL,
  cover_url VARCHAR(500),
  category_id BIGINT NOT NULL,
  status VARCHAR(24) NOT NULL DEFAULT 'DRAFT',
  view_count INT NOT NULL DEFAULT 0,
  comment_count INT NOT NULL DEFAULT 0,
  featured TINYINT NOT NULL DEFAULT 0,
  seo_title VARCHAR(180),
  seo_keywords VARCHAR(255),
  seo_description VARCHAR(500),
  published_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  FULLTEXT KEY ft_article(title, summary, content),
  INDEX idx_status_time(status, published_at),
  INDEX idx_category(category_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS blog_article_tag (
  article_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  PRIMARY KEY(article_id, tag_id),
  INDEX idx_tag(tag_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS blog_comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  article_id BIGINT NOT NULL,
  parent_id BIGINT,
  author_name VARCHAR(64) NOT NULL,
  author_email VARCHAR(120),
  author_ip VARCHAR(64),
  content VARCHAR(1000) NOT NULL,
  status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_article_status(article_id, status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS blog_friend_link (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  url VARCHAR(255) NOT NULL,
  logo VARCHAR(255),
  description VARCHAR(255),
  status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS blog_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  author_name VARCHAR(64) NOT NULL,
  author_email VARCHAR(120),
  content VARCHAR(1000) NOT NULL,
  status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sys_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  config_key VARCHAR(80) NOT NULL UNIQUE,
  config_value TEXT,
  description VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB;

INSERT IGNORE INTO sys_user(username, password_hash, nickname, role) VALUES
('admin', '$2a$10$lJN6MRv5SEKY6mD4M60AJOe7kN65HwZbQ7R0if1Uzwr69ejDhD9Lm', '管理员', 'ADMIN');
INSERT IGNORE INTO blog_category(id, name, slug, description, sort_order) VALUES (1, '技术', 'tech', '技术文章', 1);
INSERT IGNORE INTO blog_tag(id, name, slug) VALUES (1, 'Java', 'java'), (2, 'Spring Boot', 'spring-boot');
INSERT IGNORE INTO blog_article(title, slug, summary, content, category_id, status, published_at, seo_title, seo_keywords, seo_description) VALUES
('欢迎使用企业级个人博客系统', 'welcome', '系统初始化文章，可在后台编辑或删除。', '<h2>欢迎</h2><p>这是可容器化部署的 Java + Vue 博客系统。</p>', 1, 'PUBLISHED', NOW(), '欢迎使用企业级个人博客系统', 'Java,Spring Boot,Vue', '企业级个人博客系统初始化文章');
INSERT IGNORE INTO blog_article_tag(article_id, tag_id) VALUES (1, 1), (1, 2);
INSERT IGNORE INTO sys_config(config_key, config_value, description) VALUES
('siteTitle', '企业级个人博客', '网站标题'),
('logo', '/logo.svg', '网站 Logo'),
('icp', '请在后台配置ICP备案号', 'ICP备案'),
('about', '这里是关于我页面内容，可在后台配置。', '关于我'),
('ossMode', 'local', '图片存储模式');
