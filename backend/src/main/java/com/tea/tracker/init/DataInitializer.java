package com.tea.tracker.init;

import com.tea.tracker.service.TeaTemplateCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TeaTemplateCacheService templateCacheService;

    @Override
    public void run(String... args) {
        log.info("Initializing/refreshing tea brewing templates in Redis (with version check)...");
        templateCacheService.refreshAllTemplates();
        log.info("Data initialization complete - all templates refreshed with latest version and parameter ranges");
    }
}
