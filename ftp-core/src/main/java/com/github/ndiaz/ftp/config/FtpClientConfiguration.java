package com.github.ndiaz.ftp.config;

import lombok.Data;

@Data
public class FtpClientConfiguration {

  private String name;
  private String protocol;
  private String host;
  private Integer port;
  private String username;
  private String password;
  private String root;

}
