package com.dbtraining.reconx.observability;

import com.dbtraining.reconx.repository.TradeRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradesByStatusGaugeTest {

    @Test
    void registersOneGaugePerStatusAndReadsCountsFromRepository() {
        TradeRepository repo = mock(TradeRepository.class);
        when(repo.countByStatus("PENDING")).thenReturn(3L);
        when(repo.countByStatus("MATCHED")).thenReturn(2L);
        when(repo.countByStatus("UNMATCHED")).thenReturn(1L);
        when(repo.countByStatus("DISPUTED")).thenReturn(0L);
        when(repo.countByStatus("CANCELLED")).thenReturn(4L);

        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        new TradesByStatusGauge(registry, repo);

        assertNotNull(registry.find("trades_by_status").tags("status", "PENDING").gauge());
        assertNotNull(registry.find("trades_by_status").tags("status", "MATCHED").gauge());
        assertNotNull(registry.find("trades_by_status").tags("status", "UNMATCHED").gauge());
        assertNotNull(registry.find("trades_by_status").tags("status", "DISPUTED").gauge());
        assertNotNull(registry.find("trades_by_status").tags("status", "CANCELLED").gauge());

        assertEquals(3.0, registry.find("trades_by_status").tags("status", "PENDING").gauge().value());
        assertEquals(2.0, registry.find("trades_by_status").tags("status", "MATCHED").gauge().value());
        assertEquals(1.0, registry.find("trades_by_status").tags("status", "UNMATCHED").gauge().value());
        assertEquals(0.0, registry.find("trades_by_status").tags("status", "DISPUTED").gauge().value());
        assertEquals(4.0, registry.find("trades_by_status").tags("status", "CANCELLED").gauge().value());
    }
}
