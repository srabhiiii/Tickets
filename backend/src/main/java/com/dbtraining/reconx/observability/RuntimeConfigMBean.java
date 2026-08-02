package com.dbtraining.reconx.observability;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource(objectName = "reconx:name=RuntimeConfig")
public class RuntimeConfigMBean {

    private boolean metricsDebugEnabled = false;

    @ManagedAttribute(description = "Enable verbose metrics debug output")
    public boolean isMetricsDebugEnabled() {
        return metricsDebugEnabled;
    }

    @ManagedAttribute(description = "Enable verbose metrics debug output")
    public void setMetricsDebugEnabled(boolean metricsDebugEnabled) {
        this.metricsDebugEnabled = metricsDebugEnabled;
    }

    @ManagedOperation(description = "Toggle metrics debug mode")
    public void toggleMetricsDebug() {
        this.metricsDebugEnabled = !this.metricsDebugEnabled;
    }
}
