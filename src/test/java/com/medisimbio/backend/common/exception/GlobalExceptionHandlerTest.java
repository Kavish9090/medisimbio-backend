package com.medisimbio.backend.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestErrorController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // Test Dummy Controller to trigger exceptions
    @RestController
    @RequestMapping("/test/api/errors")
    static class TestErrorController {

        @GetMapping("/not-found")
        public void throwNotFound() {
            throw new ResourceNotFoundException("Patient", "id", "p_12345");
        }

        @GetMapping("/bad-request")
        public void throwBadRequest() {
            throw new BadRequestException("Invalid date range provided");
        }

        @GetMapping("/forbidden")
        public void throwForbidden() {
            throw new ForbiddenException("Access to this clinical record is restricted");
        }

        @GetMapping("/server-error")
        public void throwServerError() {
            throw new RuntimeException("Unexpected internal failure simulation");
        }

        @PostMapping("/validate")
        public String validateDto(@Valid @RequestBody TestValidationDto dto) {
            return "Valid: " + dto.getName();
        }
    }

    // Test Validation DTO
    static class TestValidationDto {
        @NotBlank(message = "Name must not be blank")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        private String name;

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be valid format")
        private String email;

        public TestValidationDto() {
        }

        public TestValidationDto(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    @Test
    @DisplayName("404 ResourceNotFoundException returns structured ErrorResponse")
    void handleResourceNotFoundException_shouldReturn404() throws Exception {
        mockMvc.perform(get("/test/api/errors/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("RESOURCE_NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Patient not found with id: 'p_12345'")))
                .andExpect(jsonPath("$.path", is("/test/api/errors/not-found")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("400 BadRequestException returns structured ErrorResponse")
    void handleBadRequestException_shouldReturn400() throws Exception {
        mockMvc.perform(get("/test/api/errors/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", is("Invalid date range provided")))
                .andExpect(jsonPath("$.path", is("/test/api/errors/bad-request")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("403 ForbiddenException returns structured ErrorResponse")
    void handleForbiddenException_shouldReturn403() throws Exception {
        mockMvc.perform(get("/test/api/errors/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("FORBIDDEN")))
                .andExpect(jsonPath("$.message", is("Access to this clinical record is restricted")))
                .andExpect(jsonPath("$.path", is("/test/api/errors/forbidden")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("400 Validation Error returns structured ErrorResponse with field details")
    void handleValidationException_shouldReturn400WithDetails() throws Exception {
        TestValidationDto invalidDto = new TestValidationDto("", "invalid-email-format");

        mockMvc.perform(post("/test/api/errors/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", is("Request validation failed")))
                .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.path", is("/test/api/errors/validate")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("500 Uncaught Exception returns safe ErrorResponse without leaking stack trace")
    void handleUncaughtException_shouldReturnSafe500() throws Exception {
        mockMvc.perform(get("/test/api/errors/server-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("INTERNAL_SERVER_ERROR")))
                .andExpect(jsonPath("$.message", is("An unexpected internal error occurred. Please contact system support.")))
                .andExpect(jsonPath("$.path", is("/test/api/errors/server-error")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }
}
