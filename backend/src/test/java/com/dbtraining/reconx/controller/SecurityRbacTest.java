package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.dto.TradeMapper;
import com.dbtraining.reconx.dto.TradeResponse;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.security.JwtTokenProvider;
import com.dbtraining.reconx.service.TradeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityRbacTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradeService tradeService;

    @MockBean
    private TradeMapper tradeMapper;

    @Test
    @DisplayName("ADV074: Unauthenticated GET /v1/trades returns 401 Unauthorized")
    void unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/v1/trades"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "viewer@db.com", roles = {"VIEWER"})
    @DisplayName("ADV074: VIEWER role GET /v1/trades returns 200 OK")
    void viewer_canGetTrades_returns200() throws Exception {
        when(tradeService.list(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/v1/trades"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "viewer@db.com", roles = {"VIEWER"})
    @DisplayName("ADV074: VIEWER role POST /v1/trades returns 403 Forbidden")
    void viewer_cannotCreateTrade_returns403() throws Exception {
        String jsonPayload = """
                {
                    "tradeRef": "TRD-20260315-0001",
                    "instrumentId": 1,
                    "counterpartyId": 1,
                    "assetClass": "EQUITY",
                    "side": "BUY",
                    "quantity": 100.0,
                    "price": 245.50,
                    "tradeDate": "2026-03-15"
                }
                """;

        mockMvc.perform(post("/v1/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "trader@db.com", roles = {"TRADER"})
    @DisplayName("ADV074: TRADER role POST /v1/trades returns 201 Created")
    void trader_canCreateTrade_returns201() throws Exception {
        Trade trade = new Trade();
        trade.setId(10L);

        TradeResponse response = new TradeResponse(
                10L, "TRD-20260315-0001", 1L, "AAPL", 1L, "Apex",
                "EQUITY", "BUY", new BigDecimal("100.00"), new BigDecimal("245.50"),
                LocalDate.of(2026, 3, 15), "PENDING", null, null
        );

        when(tradeService.create(any(), any())).thenReturn(trade);
        when(tradeMapper.toResponse(trade)).thenReturn(response);

        String jsonPayload = """
                {
                    "tradeRef": "TRD-20260315-0001",
                    "instrumentId": 1,
                    "counterpartyId": 1,
                    "assetClass": "EQUITY",
                    "side": "BUY",
                    "quantity": 100.0,
                    "price": 245.50,
                    "tradeDate": "2026-03-15"
                }
                """;

        mockMvc.perform(post("/v1/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "trader@db.com", roles = {"TRADER"})
    @DisplayName("ADV074: TRADER role DELETE /v1/trades/1 returns 403 Forbidden")
    void trader_cannotDeleteTrade_returns403() throws Exception {
        mockMvc.perform(delete("/v1/trades/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@db.com", roles = {"ADMIN"})
    @DisplayName("ADV074: ADMIN role DELETE /v1/trades/1 returns 204 No Content")
    void admin_canDeleteTrade_returns204() throws Exception {
        mockMvc.perform(delete("/v1/trades/1"))
                .andExpect(status().isNoContent());
    }
}
