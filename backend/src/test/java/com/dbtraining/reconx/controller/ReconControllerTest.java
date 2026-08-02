package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.repository.ReconBreakRepository;
import com.dbtraining.reconx.repository.entity.ReconBreak;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ReconControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReconBreakRepository breaksRepository;

    @Test
    @DisplayName("ADV068: POST /v1/recon/run returns 202 Accepted with jobId and QUEUED status")
    void runRecon_validRequest_returns202AcceptedWithJobId() throws Exception {
        String jsonPayload = """
                {
                    "from": "2026-03-01",
                    "to": "2026-03-31"
                }
                """;

        mockMvc.perform(post("/v1/recon/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").exists())
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    @DisplayName("ADV069: GET /v1/recon/jobs/{jobId}/results returns recon breaks list")
    void getResults_returnsBreaksList() throws Exception {
        ReconBreak reconBreak = new ReconBreak();
        reconBreak.setTradeId(100L);
        reconBreak.setDiscrepancyType("PRICE_MISMATCH");

        when(breaksRepository.findAll()).thenReturn(List.of(reconBreak));

        mockMvc.perform(get("/v1/recon/jobs/test-job-id/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tradeId").value(100))
                .andExpect(jsonPath("$[0].discrepancyType").value("PRICE_MISMATCH"));
    }

    @Test
    @DisplayName("ADV070: PUT /v1/recon/results/{id}/resolve marks break as RESOLVED")
    void resolveBreak_validId_returns200OK() throws Exception {
        ReconBreak reconBreak = new ReconBreak();
        reconBreak.setTradeId(100L);
        reconBreak.setDiscrepancyType("PRICE_MISMATCH");

        when(breaksRepository.findById(eq(1L))).thenReturn(Optional.of(reconBreak));
        when(breaksRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/v1/recon/results/1/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\": \"Manually resolved price discrepancy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolutionNote").value("Manually resolved price discrepancy"));
    }

    @Test
    @DisplayName("ADV070: PUT /v1/recon/results/{id}/resolve with missing id returns 404 Not Found")
    void resolveBreak_notFound_returns404NotFound() throws Exception {
        when(breaksRepository.findById(eq(999L))).thenReturn(Optional.empty());

        mockMvc.perform(put("/v1/recon/results/999/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\": \"Resolution attempt\"}"))
                .andExpect(status().isNotFound());
    }
}
