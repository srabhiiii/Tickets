package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.dto.TradeMapper;
import com.dbtraining.reconx.dto.TradeResponse;
import com.dbtraining.reconx.exception.TradeNotFoundException;
import com.dbtraining.reconx.repository.entity.Trade;
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
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradeService tradeService;

    @MockBean
    private TradeMapper tradeMapper;

    @Test
    @DisplayName("ADV063: GET /v1/trades returns paginated envelope with items, page, size, totalElements, totalPages")
    void listTrades_returnsPagedResponseEnvelope() throws Exception {
        Trade trade = new Trade();
        trade.setId(1L);
        trade.setTradeRef("TRD-20260315-0001");
        trade.setStatus("PENDING");

        TradeResponse response = new TradeResponse(
                1L, "TRD-20260315-0001", 101L, "AAPL", 201L, "Apex",
                "EQUITY", "BUY", new BigDecimal("100.00"), new BigDecimal("250.50"),
                LocalDate.of(2026, 3, 15), "PENDING", null, null
        );

        when(tradeService.list(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(trade)));
        when(tradeMapper.toResponse(trade)).thenReturn(response);

        mockMvc.perform(get("/v1/trades")
                        .param("page", "0")
                        .param("size", "20")
                        .param("status", "PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].tradeRef").value("TRD-20260315-0001"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("ADV064: POST /v1/trades creates trade and returns 201 Created with Location header")
    void createTrade_validBody_returns201CreatedAndLocationHeader() throws Exception {
        Trade trade = new Trade();
        trade.setId(100L);
        trade.setTradeRef("TRD-20260315-0001");

        TradeResponse response = new TradeResponse(
                100L, "TRD-20260315-0001", 1L, "AAPL", 1L, "Apex",
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
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/trades/100"))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.tradeRef").value("TRD-20260315-0001"));
    }

    @Test
    @DisplayName("ADV064: POST /v1/trades with invalid body returns 400 Bad Request")
    void createTrade_invalidBody_returns400BadRequest() throws Exception {
        String invalidPayload = """
                {
                    "tradeRef": "INVALID",
                    "quantity": -5.0
                }
                """;

        mockMvc.perform(post("/v1/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ADV065: PUT /v1/trades/{id} updates trade and returns 200 OK")
    void updateTrade_validBody_returns200OK() throws Exception {
        Trade trade = new Trade();
        trade.setId(1L);
        trade.setTradeRef("TRD-20260315-0001");

        TradeResponse response = new TradeResponse(
                1L, "TRD-20260315-0001", 1L, "AAPL", 1L, "Apex",
                "EQUITY", "BUY", new BigDecimal("150.00"), new BigDecimal("250.00"),
                LocalDate.of(2026, 3, 15), "PENDING", null, null
        );

        when(tradeService.update(eq(1L), any(), any())).thenReturn(trade);
        when(tradeMapper.toResponse(trade)).thenReturn(response);

        String jsonPayload = """
                {
                    "tradeRef": "TRD-20260315-0001",
                    "instrumentId": 1,
                    "counterpartyId": 1,
                    "assetClass": "EQUITY",
                    "side": "BUY",
                    "quantity": 150.0,
                    "price": 250.00,
                    "tradeDate": "2026-03-15"
                }
                """;

        mockMvc.perform(put("/v1/trades/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.quantity").value(150.0))
                .andExpect(jsonPath("$.price").value(250.00));
    }

    @Test
    @DisplayName("ADV065: PUT /v1/trades/{id} with missing id returns 404 Not Found")
    void updateTrade_notFound_returns404NotFound() throws Exception {
        when(tradeService.update(eq(999L), any(), any()))
                .thenThrow(new TradeNotFoundException("id 999"));

        String jsonPayload = """
                {
                    "tradeRef": "TRD-20260315-0001",
                    "instrumentId": 1,
                    "counterpartyId": 1,
                    "assetClass": "EQUITY",
                    "side": "BUY",
                    "quantity": 150.0,
                    "price": 250.00,
                    "tradeDate": "2026-03-15"
                }
                """;

        mockMvc.perform(put("/v1/trades/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Trade not found: id 999"));
    }

    @Test
    @DisplayName("ADV066: PATCH /v1/trades/{id}/status updates status and returns 200 OK")
    void patchStatus_validStatus_returns200OK() throws Exception {
        Trade trade = new Trade();
        trade.setId(1L);
        trade.setStatus("MATCHED");

        TradeResponse response = new TradeResponse(
                1L, "TRD-20260315-0001", 1L, "AAPL", 1L, "Apex",
                "EQUITY", "BUY", new BigDecimal("100.00"), new BigDecimal("250.00"),
                LocalDate.of(2026, 3, 15), "MATCHED", null, null
        );

        when(tradeService.updateStatus(eq(1L), eq("MATCHED"), any())).thenReturn(trade);
        when(tradeMapper.toResponse(trade)).thenReturn(response);

        mockMvc.perform(patch("/v1/trades/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"MATCHED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MATCHED"));
    }

    @Test
    @DisplayName("ADV066: PATCH /v1/trades/{id}/status missing status returns 400 Bad Request")
    void patchStatus_missingStatus_returns400BadRequest() throws Exception {
        mockMvc.perform(patch("/v1/trades/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ADV067: DELETE /v1/trades/{id} soft deletes trade and returns 204 No Content")
    void deleteTrade_existingId_returns204NoContent() throws Exception {
        mockMvc.perform(delete("/v1/trades/1"))
                .andExpect(status().isNoContent());

        verify(tradeService).softDelete(eq(1L), any());
    }

    @Test
    @DisplayName("ADV067: DELETE /v1/trades/{id} with missing id returns 404 Not Found")
    void deleteTrade_missingId_returns404NotFound() throws Exception {
        doThrow(new TradeNotFoundException("id 999"))
                .when(tradeService).softDelete(eq(999L), any());

        mockMvc.perform(delete("/v1/trades/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Trade not found: id 999"));
    }
}
