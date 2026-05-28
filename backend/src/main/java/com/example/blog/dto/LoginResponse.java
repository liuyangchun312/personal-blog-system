package com.example.blog.dto;

public record LoginResponse(String token, String username, String nickname, String role) {
}
