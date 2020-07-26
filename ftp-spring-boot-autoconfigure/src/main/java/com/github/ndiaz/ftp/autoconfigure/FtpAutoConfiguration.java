package com.github.ndiaz.ftp.autoconfigure;

import com.github.ndiaz.ftp.factory.FtpFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(FtpFactory.class)
@EnableConfigurationProperties(FtpProperties.class)
public class FtpAutoConfiguration {

  private final FtpProperties properties;

  public FtpAutoConfiguration(final FtpProperties properties) {
    this.properties = properties;
  }

  @Bean
  @ConditionalOnMissingBean
  public FtpFactory ftpFactory() {
    return new FtpFactory();
  }

}
