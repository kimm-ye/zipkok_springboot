package com.kosmo.zipkok.dto;

public record ApiResponse<T>(boolean success, String message, String redirectUrl) {}
