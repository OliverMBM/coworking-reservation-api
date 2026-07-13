package com.coworking.reservation.space;

import com.coworking.reservation.AbstractIntegrationTest;
import com.coworking.reservation.dto.request.CreateSpaceRequest;
import com.coworking.reservation.entity.enums.SpaceType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class SpaceSecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void userShouldNotCreateSpace() throws Exception {
        CreateSpaceRequest request = new CreateSpaceRequest(
                "Meeting Room A",
                SpaceType.MEETING_ROOM,
                8,
                "Second floor",
                BigDecimal.valueOf(25)
        );

        mockMvc.perform(
                        post("/api/spaces")
                                .with(user("normal.user@email.com").roles("USER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }
}