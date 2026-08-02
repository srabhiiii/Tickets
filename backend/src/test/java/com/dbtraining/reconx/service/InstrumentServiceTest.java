package com.dbtraining.reconx.service;

import com.dbtraining.reconx.exception.InvalidTradeException;
import com.dbtraining.reconx.repository.InstrumentRepository;
import com.dbtraining.reconx.repository.entity.Instrument;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InstrumentServiceTest {

    @Test
    void returnsInstrumentWhenFoundAndThrowsWhenMissing() {
        InstrumentRepository repo = Mockito.mock(InstrumentRepository.class);
        Instrument instrument = new Instrument();
        instrument.setSymbol("SAP.DE");
        Mockito.when(repo.findBySymbol("SAP.DE")).thenReturn(Optional.of(instrument));

        InstrumentService service = new InstrumentService(repo);

        assertEquals("SAP.DE", service.findBySymbol("SAP.DE").getSymbol());
        assertThrows(InvalidTradeException.class, () -> service.findBySymbol("UNKNOWN"));
    }
}
