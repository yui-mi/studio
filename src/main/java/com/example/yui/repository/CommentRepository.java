package com.example.yui.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.yui.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}