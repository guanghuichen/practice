package com.example.practice.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleController {

  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully checked if chargepoint exists.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExistsChargepointDto.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized request.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
  })
  @GetMapping("example/{xxl}")
  public ExistsChargepointDto hello(@PathVariable("xxl") String xxl) {
    return new ExistsChargepointDto(xxl);
  }

}