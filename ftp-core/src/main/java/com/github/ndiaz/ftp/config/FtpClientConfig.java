package com.github.ndiaz.ftp.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FtpClientConfig {

  private Protocol protocol;
  private String host;
  private Integer port;
  private String username;
  private String password;
  private String root;

  @AllArgsConstructor(access = AccessLevel.PACKAGE)
  public enum Protocol {
    FTP, SFTP
  }

}