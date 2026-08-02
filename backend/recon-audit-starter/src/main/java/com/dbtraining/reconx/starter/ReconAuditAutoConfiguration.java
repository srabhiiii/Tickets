package com.dbtraining.reconx.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name = "com.dbtraining.reconx.ReconxApplication")
public class ReconAuditAutoConfiguration {

    @Bean
    public ReconAuditService reconAuditService() {
        return new ReconAuditService();
    }
}
