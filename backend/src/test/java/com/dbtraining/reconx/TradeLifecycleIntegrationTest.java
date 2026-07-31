package com.dbtraining.reconx;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TradeLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("ADV078: Full trade lifecycle - Login -> Create -> List -> Update -> Soft Delete")
    void fullTradeLifecycle() throws Exception {
        // 1. Login as trader@db.com
        String loginPayload = """
                {
                    "email": "trader@db.com",
                    "password": "trader123"
                }
                """;

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        String traderToken = objectMapper.readTree(loginResponse).get("token").asText();

        // 2. Login as admin@db.com
        String adminLoginPayload = """
                {
                    "email": "admin@db.com",
                    "password": "admin123"
                }
                """;

        String adminLoginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminLoginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        String adminToken = objectMapper.readTree(adminLoginResponse).get("token").asText();

        // 3. Create trade as trader
        String tradeRef = "TRD-20260315-9999";
        String createPayload = """
                {
                    "tradeRef": "%s",
                    "instrumentId": 1,
                    "counterpartyId": 1,
                    "assetClass": "EQUITY",
                    "side": "BUY",
                    "quantity": 100.0,
                    "price": 250.00,
                    "tradeDate": "2026-03-15"
                }
                """.formatted(tradeRef);

        String createResponse = mockMvc.perform(post("/v1/trades")
                        .header("Authorization", "Bearer " + traderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.tradeRef").value(tradeRef))
                .andReturn().getResponse().getContentAsString();

        long createdId = objectMapper.readTree(createResponse).get("id").asLong();

        // 4. List trades as trader - should include created trade
        mockMvc.perform(get("/v1/trades")
                        .header("Authorization", "Bearer " + traderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == " + createdId + ")].tradeRef").value(tradeRef));

        // 5. Update trade as trader
        String updatePayload = """
                {
                    "tradeRef": "%s",
                    "instrumentId": 1,
                    "counterpartyId": 1,
                    "assetClass": "EQUITY",
                    "side": "BUY",
                    "quantity": 200.0,
                    "price": 260.00,
                    "tradeDate": "2026-03-15"
                }
                """.formatted(tradeRef);

        mockMvc.perform(put("/v1/trades/" + createdId)
                        .header("Authorization", "Bearer " + traderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(200.0))
                .andExpect(jsonPath("$.price").value(260.00));

        // 6. Soft delete trade as admin
        mockMvc.perform(delete("/v1/trades/" + createdId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // 7. List trades - deleted trade should no longer be present
        mockMvc.perform(get("/v1/trades")
                        .header("Authorization", "Bearer " + traderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == " + createdId + ")]").isEmpty());
    }
}
