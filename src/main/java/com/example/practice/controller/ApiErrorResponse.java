package com.example.practice.controller;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record ApiErrorResponse(
    String errorId,
    List<Error> errors
) {
  public record Error(
      @JsonInclude(JsonInclude.Include.NON_NULL)
      String path,
      String message
  ) {
    public static Error of(String message) {
      Error error = new Error("", message);
      return error;
    }
  }
}
