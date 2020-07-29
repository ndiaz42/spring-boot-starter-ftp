package com.github.ndiaz.ftp.factory;

import com.github.ndiaz.ftp.FtpClient;
import com.github.ndiaz.ftp.config.FtpClientConfig;
import com.github.ndiaz.ftp.config.FtpClientConfig.Protocol;
import com.github.ndiaz.ftp.impl.ApacheFtpClient;
import com.github.ndiaz.ftp.impl.JCraftSftpClient;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FtpFactory {

  private final Map<String, FtpClientConfig> availableFtpClients;

  public FtpFactory(final Map<String, FtpClientConfig> availableFtpClients) {
    this.availableFtpClients = availableFtpClients;
  }

  public FtpClient getFtp(@NonNull final String name) {
    final FtpClientConfig ftpClientConfig = availableFtpClients.get(name);
    if (ftpClientConfig.getProtocol().equals(Protocol.FTP)) {
      return new ApacheFtpClient(ftpClientConfig);
    } else if (ftpClientConfig.getProtocol().equals(Protocol.SFTP)) {
      return new JCraftSftpClient(ftpClientConfig);
    }
    return null;
  }

}
