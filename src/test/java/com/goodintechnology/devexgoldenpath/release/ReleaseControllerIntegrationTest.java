package com.goodintechnology.devexgoldenpath.release;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodintechnology.devexgoldenpath.release.dto.CreateReleaseRequest;
import com.goodintechnology.devexgoldenpath.release.dto.SubmitCheckRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ReleaseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullReleaseLifecycleGoesFromBlockedToReady() throws Exception {
        String createBody = objectMapper.writeValueAsString(new CreateReleaseRequest("1.8.4", "production"));

        String response = mockMvc.perform(post("/releases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value("1.8.4"))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/releases/{id}/readiness", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"))
                .andExpect(jsonPath("$.reasons", hasSize(CheckType.values().length)));

        for (CheckType type : CheckType.values()) {
            String checkBody = objectMapper.writeValueAsString(new SubmitCheckRequest(type, CheckStatus.PASS));
            mockMvc.perform(post("/releases/{id}/checks", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(checkBody))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/releases/{id}/readiness", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.reasons", hasSize(0)));

        String failBody = objectMapper.writeValueAsString(
                new SubmitCheckRequest(CheckType.SECURITY_SCAN, CheckStatus.FAIL));
        mockMvc.perform(post("/releases/{id}/checks", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(failBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/releases/{id}/readiness", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"))
                .andExpect(jsonPath("$.reasons", hasSize(1)))
                .andExpect(jsonPath("$.reasons[0]").value("Security scan did not pass"));
    }

    @Test
    void unknownReleaseIdReturns404() throws Exception {
        mockMvc.perform(get("/releases/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
