CREATE TABLE IF NOT EXISTS file_upload(ID long PRIMARY KEY AUTO_INCREMENT, branch VARCHAR(40) NOT NULL, file_size long, status VARCHAR(20),  desc VARCHAR(255), created_at DATETIME NOT NULL, updated_at DATETIME);
CREATE TABLE IF NOT EXISTS file_upload_archive(ID long PRIMARY KEY AUTO_INCREMENT, branch VARCHAR(40) NOT NULL, file_size long, status VARCHAR(20),  desc VARCHAR(255), created_at DATETIME NOT NULL, updated_at DATETIME);
