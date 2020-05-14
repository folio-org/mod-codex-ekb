package org.folio.spring;

import io.vertx.core.Vertx;
import org.folio.cache.VertxCache;
import org.folio.holdingsiq.service.ConfigurationService;
import org.folio.holdingsiq.service.impl.ConfigurationServiceCache;
import org.folio.holdingsiq.service.impl.ConfigurationServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

@Configuration
@ComponentScan(basePackages = {
  "org.folio.converter.hld2cdx",
  "org.folio.validator",
  "org.folio.codex",
  "org.folio.parser"})
public class ApplicationConfig {

  @Bean
  public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
    PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
    configurer.setLocation(new ClassPathResource("application.properties"));
    return configurer;
  }

  @Bean
  public VertxCache<String, org.folio.holdingsiq.model.Configuration> rmApiConfigurationCache(Vertx vertx, @Value("${configuration.cache.expire}") long expirationTime) {
    return new VertxCache<>(vertx, expirationTime, "rmApiConfigurationCache");
  }

  @Bean
  public ConfigurationService configurationService(Vertx vertx, @Value("${configuration.cache.expire}") long expirationTime) {
    return new ConfigurationServiceCache(
      new ConfigurationServiceImpl(vertx),
      new VertxCache<>(vertx, expirationTime, "rmApiConfigurationCache")
    );
  }
}
