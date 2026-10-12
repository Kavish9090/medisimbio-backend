package com.medisimbio.backend.specialty.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisimbio.backend.security.DevAuthenticationFilter;
import com.medisimbio.backend.specialty.dto.CreateSpecialtyRequestDto;
import com.medisimbio.backend.specialty.entity.SpecialtyEntity;
import com.medisimbio.backend.specialty.repository.SpecialtyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SpecialtyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    private static final String TEST_ADMIN_USER = "auth_admin_001";

    @Test
    @DisplayName("GET /api/v1/specialties: Publicly accessible and returns seeded specialties")
    void getSpecialties_publicAccess() throws Exception {
        mockMvc.perform(get("/api/v1/specialties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.items", not(empty())))
                .andExpect(jsonPath("$.data.totalElements", greaterThanOrEqualTo(10)));
    }

    @Test
    @DisplayName("GET /api/v1/specialties/code/{code}: Returns cardiology specialty")
    void getSpecialtyByCode_success() throws Exception {
        mockMvc.perform(get("/api/v1/specialties/code/CARDIOLOGY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.code", is("CARDIOLOGY")))
                .andExpect(jsonPath("$.data.displayName", is("Cardiology")));
    }

    @Test
    @DisplayName("GET /api/v1/specialties/code/{code}: 404 for non-existent specialty code")
    void getSpecialtyByCode_notFound() throws Exception {
        mockMvc.perform(get("/api/v1/specialties/code/NON_EXISTENT_SPECIALTY"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("RESOURCE_NOT_FOUND")));
    }

    @Test
    @DisplayName("POST /api/v1/specialties: Successfully creates new specialty when authenticated")
    void createSpecialty_success() throws Exception {
        CreateSpecialtyRequestDto dto = new CreateSpecialtyRequestDto(
                "RHEUMATOLOGY",
                "Rheumatology",
                "Diagnosis and therapy of rheumatic diseases"
        );

        mockMvc.perform(post("/api/v1/specialties")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.code", is("RHEUMATOLOGY")))
                .andExpect(jsonPath("$.data.displayName", is("Rheumatology")))
                .andExpect(jsonPath("$.data.active", is(true)));
    }

    @Test
    @DisplayName("POST /api/v1/specialties: 401 Unauthorized when unauthenticated")
    void createSpecialty_unauthorized() throws Exception {
        CreateSpecialtyRequestDto dto = new CreateSpecialtyRequestDto(
                "IMMUNOLOGY",
                "Immunology",
                "Immune system medical study"
        );

        mockMvc.perform(post("/api/v1/specialties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("UNAUTHORIZED")));
    }

    @Test
    @DisplayName("POST /api/v1/specialties: 409 Conflict on duplicate specialty code")
    void createSpecialty_conflict() throws Exception {
        CreateSpecialtyRequestDto dto = new CreateSpecialtyRequestDto(
                "CARDIOLOGY",
                "Duplicate Cardiology",
                "Duplicate"
        );

        mockMvc.perform(post("/api/v1/specialties")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("RESOURCE_ALREADY_EXISTS")));
    }
}
