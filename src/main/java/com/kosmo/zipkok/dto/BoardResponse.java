package com.kosmo.zipkok.dto;

public record BoardResponse<T>(boolean success, String message,  T data, PagingDTO paging) {}
