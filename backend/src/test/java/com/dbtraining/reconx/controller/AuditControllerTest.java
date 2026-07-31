package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.repository.AuditLogRepository;
import com.dbtraining.reconx.repository.entity.AuditLogEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogRepository auditRepo;

    @Test
    @DisplayName("ADV071: GET /v1/audit/trades/{tradeRef} returns audit log entries ordered by timestamp")
    void history_validTradeRef_returnsAuditEntries() throws Exception {
        AuditLogEntry entry = new AuditLogEntry(
                UUID.randomUUID().toString(),
                "TRD-20260315-0001",
                "TRADE_CREATED",
                Instant.now(),
                "trader@db.com",
                null,
                null
        );

        when(auditRepo.findByTradeRefOrderByEventTimestampAsc(eq("TRD-20260315-0001")))
                .thenReturn(List.of(entry));

        mockMvc.perform(get("/v1/audit/trades/TRD-20260315-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tradeRef").value("TRD-20260315-0001"))
                .andExpect(jsonPath("$[0].eventType").value("TRADE_CREATED"));
    }
}
