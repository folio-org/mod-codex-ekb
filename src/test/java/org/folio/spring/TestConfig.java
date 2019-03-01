package org.folio.spring;

import static org.mockito.Mockito.mock;

import org.folio.holdingsiq.service.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ApplicationConfig.class)
public class TestConfig {
  @Bean
  public ConfigurationService configurationService() {
    return mock(ConfigurationService.class);
  }
}

