package com.dbtraining.reconx.observability;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource(objectName = "reconx:type=ReconConfig")
public class ReconConfig {

    private double priceTolerance = 0.01;
    private boolean cachingEnabled = true;

    @ManagedAttribute(description = "Reconciliation price tolerance")
    public double getPriceTolerance() {
        return priceTolerance;
    }

    @ManagedAttribute(description = "Reconciliation price tolerance")
    public void setPriceTolerance(double priceTolerance) {
        if (priceTolerance < 0 || priceTolerance > 1) {
            throw new IllegalArgumentException("priceTolerance must be between 0 and 1");
        }
        this.priceTolerance = priceTolerance;
    }

    @ManagedAttribute(description = "Whether cache-based lookups are enabled")
    public boolean isCachingEnabled() {
        return cachingEnabled;
    }

    @ManagedAttribute(description = "Whether cache-based lookups are enabled")
    public void setCachingEnabled(boolean cachingEnabled) {
        this.cachingEnabled = cachingEnabled;
    }

    @ManagedOperation(description = "Clear runtime caches")
    public void clearCache() {
        // runtime hook for future cache invalidation
    }
}
