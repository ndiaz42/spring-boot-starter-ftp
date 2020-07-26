package com.github.ndiaz.ftp.autoconfigure;

import com.github.ndiaz.ftp.FtpClient;
import com.github.ndiaz.ftp.impl.ApacheFtpClient;
import com.github.ndiaz.ftp.impl.GenericFtpClient;
import com.github.ndiaz.ftp.impl.JCraftSftpClient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum Protocol {

  FTP(ApacheFtpClient.class), SFTP(JCraftSftpClient.class);

  private Class<? extends FtpClient> ftpClientClass;

}
