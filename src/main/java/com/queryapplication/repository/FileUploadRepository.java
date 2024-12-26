package com.queryapplication.repository;

import com.queryapplication.entity.FileUpload;
import com.queryapplication.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {
    List<FileUpload> findByUploadedBy(Users user); // Files uploaded by a specific user
}
