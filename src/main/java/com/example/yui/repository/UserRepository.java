package com.example.yui.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.yui.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	User findByUsername(String username);
}