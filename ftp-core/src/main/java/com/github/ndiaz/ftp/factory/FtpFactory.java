package com.github.ndiaz.ftp.factory;

import com.github.ndiaz.ftp.FtpClient;
import com.github.ndiaz.ftp.config.FtpClientConfig;
import com.github.ndiaz.ftp.config.FtpClientConfig.Protocol;
import com.github.ndiaz.ftp.impl.ApacheFtpClient;
import com.github.ndiaz.ftp.impl.JCraftSftpClient;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory that instantiates configured FTP clients at demand.
 * The configuration for each client must be defined in the application properties file.
 */
@Slf4j
public class FtpFactory {

  private final Map<String, FtpClientConfig> availableFtpClients;

  public FtpFactory(final Map<String, FtpClientConfig> availableFtpClients) {
    this.availableFtpClients = availableFtpClients;
  }

  /**
   * Gets a FTP client by its name, as defined in the application properties file.
   *
   * @param name the name of the FTP client
   * @return the configured FTP client
   * @see FtpClient
   */
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
