package com.dbtraining.reconx.observability;

import com.dbtraining.reconx.repository.ReconBreakRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradeMetricsTest {

    @Test
    void registersCounterAndSummaryAndIncrementsThem() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ReconBreakRepository breakRepo = Mockito.mock(ReconBreakRepository.class);
        Mockito.when(breakRepo.countByStatus("OPEN")).thenReturn(2L);

        TradeMetrics metrics = new TradeMetrics(registry, breakRepo);

        metrics.incrementTradeCreated();
        metrics.recordTradeValue(123.45);
        metrics.recordTradeValue(67.89);

        assertEquals(1.0, registry.find("trade_created_total").counter().count());
        assertEquals(2, registry.find("trade_value_total").summary().count());
        assertEquals(191.34, registry.find("trade_value_total").summary().totalAmount(), 0.001);
        assertTrue(registry.find("recon_break_count").gauge() != null);
    }
}
