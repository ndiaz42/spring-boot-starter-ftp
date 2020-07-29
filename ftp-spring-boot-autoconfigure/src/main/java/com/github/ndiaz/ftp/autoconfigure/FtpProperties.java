package com.github.ndiaz.ftp.autoconfigure;

import com.github.ndiaz.ftp.config.FtpClientConfig;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Slf4j
@ConfigurationProperties(prefix = FtpProperties.PREFIX_FTP)
public class FtpProperties {

  public static final String PREFIX_FTP = "ftp";

  @Getter
  @Setter
  private Map<String, FtpClientConfig> clients;

}