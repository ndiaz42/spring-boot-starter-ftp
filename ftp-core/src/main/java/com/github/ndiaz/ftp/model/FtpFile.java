package com.github.ndiaz.ftp.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FtpFile {

  private String name;
  private String extension;
  private String path;
  private Long size;
  private byte[] file;
  private LocalDateTime accessed;
  private LocalDateTime modified;

}
