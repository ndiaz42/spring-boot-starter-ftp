package com.github.ndiaz.ftp.impl;

import com.github.ndiaz.ftp.config.FtpClientConfiguration;
import com.github.ndiaz.ftp.exception.FtpException;
import com.github.ndiaz.ftp.model.FtpFile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

@Slf4j
public class ApacheFtpClient extends GenericFtpClient {

  private final FTPClient client;

  public ApacheFtpClient(final FtpClientConfiguration configuration) {
    super(configuration);
    final FTPClientConfig config = new FTPClientConfig();
    config.setServerTimeZoneId(TimeZone.getTimeZone("UTC").getID());
    client = new FTPClient();
    client.configure(config);
  }

  @Override
  protected void connect() throws FtpException {
    super.connect();
    if (client.isConnected()) {
      throw new FtpException(
          "FTP Client is already connected to " + client.getRemoteAddress().getHostAddress() + ":"
              + client.getRemotePort());
    }
    try {
      log.info("Connecting to {}:{} with '{}'...", configuration.getHost(), configuration.getPort(),
          configuration.getUsername());
      client.connect(configuration.getHost(), configuration.getPort());
      if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
        throw new IOException(client.getReplyString());
      }
      client.login(configuration.getUsername(), configuration.getPassword());
      client.enterLocalPassiveMode();
      client.setFileType(FTP.BINARY_FILE_TYPE);
      log.info("Connected to {}:{} with '{}'!", configuration.getHost(), configuration.getPort(),
          configuration.getUsername());
      changeToDirectory(configuration.getRoot());
    } catch (final IOException ex) {
      log.error("Failed to connect to {}:{} with '{}'!", configuration.getHost(),
          configuration.getPort(),
          configuration.getUsername());
      disconnect();
      throw new FtpException(ex.getMessage(), ex);
    }
  }

  @Override
  protected void disconnect() {
    try {
      if (client.isConnected()) {
        client.logout();
        client.disconnect();
      }
      log.info("Disconnected from {}:{} with '{}'", configuration.getHost(),
          configuration.getPort(), configuration.getUsername());
    } catch (final IOException ex) {
      log.error("Could not disconnect from FTP server!");
      log.error(ex.getMessage(), ex);
    }
  }

  @Override
  protected List<String> listFiles() throws FtpException {
    try {
      log.info("{}@{}:{} - Listing files in current directory...", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort());
      final String[] files = client.listNames();
      if (files == null) {
        log.error("{}@{}:{} - Could not list files in current directory",
            configuration.getUsername(), configuration.getHost(),
            configuration.getPort());
        throw new IOException("List could not be obtained");
      }
      final List<String> filenames = Arrays.asList(files);
      for (final String filename : filenames) {
        log.info("{}@{}:{} -    {}", configuration.getUsername(), configuration.getHost(),
            configuration.getPort(), filename);
      }
      if (filenames.isEmpty()) {
        log.info("{}@{}:{} -    [empty directory]", configuration.getUsername(),
            configuration.getHost(),
            configuration.getPort());
      }
      return filenames;
    } catch (final IOException ex) {
      log.error("{}@{}:{} - Could not list files in current directory", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort());
      throw new FtpException(client.getReplyString(), ex);
    }
  }

  @Override
  protected InputStream downloadFile(@NonNull final String name) throws FtpException {
    log.info("{}@{}:{} - Downloading file '{}'...", configuration.getUsername(),
        configuration.getHost(),
        configuration.getPort(), name);
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      if (!client.retrieveFile(name, outputStream)) {
        throw new IOException(client.getReplyString());
      }
      final byte[] data = outputStream.toByteArray();
      final InputStream file = new ByteArrayInputStream(data);
      log.info("{}@{}:{} - File '{}' downloaded!", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), name);
      return file;
    } catch (final IOException ex) {
      log.error("{}@{}:{} - Could not download file '{}'", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), name);
      throw new FtpException(ex.getMessage(), ex);
    }
  }

  @Override
  protected FtpFile populateFileDetails(@NonNull final InputStream file, @NonNull final String name)
      throws FtpException {
    try {
      final FTPFile apacheFtpFile = client.mlistFile(name);
      final byte[] bytes = IOUtils.toByteArray(file);
      final FtpFile ftpFile = new FtpFile();
      ftpFile.setName(name);
      ftpFile.setExtension(FilenameUtils.getExtension(name));
      ftpFile.setFile(bytes);
      ftpFile.setSize(apacheFtpFile.getSize());
      ftpFile.setPath(
          configuration.getRoot() + "/" + StringUtils.trimToEmpty(client.printWorkingDirectory()));
      ftpFile
          .setModified(LocalDateTime.ofInstant(apacheFtpFile.getTimestamp().getTime().toInstant(),
              apacheFtpFile.getTimestamp().getTimeZone().toZoneId()));
      ftpFile.setAccessed(ftpFile.getModified());
      return ftpFile;
    } catch (final IOException ex) {
      log.error("{}@{}:{} - Could not populate file details for file '{}'...",
          configuration.getUsername(),
          configuration.getHost(), configuration.getPort(), name);
      throw new FtpException(client.getReplyString(), ex);
    }
  }

  @Override
  protected void uploadFile(@NonNull final InputStream file, final @NonNull String name)
      throws FtpException {
    try {
      log.info("{}@{}:{} - Uploading file as '{}'...", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), name);
      if (!client.storeFile(name, file)) {
        throw new IOException(client.getReplyString());
      }
      log.info("{}@{}:{} - File '{}' uploaded!", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), name);
    } catch (final IOException ex) {
      log.error("{}@{}:{} - Could not upload file as '{}'...", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), name);
      throw new FtpException(ex.getMessage(), ex);
    }
  }

  @Override
  protected void deleteFile(@NonNull final String name) throws FtpException {
    try {
      log.info("{}@{}:{} - Deleting file '{}'...", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), name);
      if (!client.deleteFile(name)) {
        throw new IOException(client.getReplyString());
      }
      log.info("{}@{}:{} - File '{}' deleted!", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), name);
    } catch (final IOException ex) {
      log.error("{}@{}:{} - Could not delete file '{}'...", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), name);
      throw new FtpException(ex.getMessage(), ex);
    }
  }

  @Override
  protected void createDirectory(@NonNull final String name) throws FtpException {
    try {
      log.info("{}@{}:{} - Creating directory '{}'...", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), name);
      final String[] paths = name.split("/");
      for (final String path : paths) {
        if (StringUtils.isNotEmpty(path)) {
          try {
            if (!client.changeWorkingDirectory(path)) {
              throw new IOException(client.getReplyString());
            }
          } catch (final IOException ex) {
            if (!client.makeDirectory(path)) {
              throw new IOException(client.getReplyString());
            }
            if (!client.changeWorkingDirectory(path)) {
              throw new IOException(client.getReplyString());
            }
          }
        }
      }
      log.info("{}@{}:{} - Directory '{}' created!", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), name);
    } catch (final IOException ex) {
      log.error("{}@{}:{} - Could not create directory '{}'...", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), name);
      throw new FtpException(client.getReplyString(), ex);
    }
  }

  @Override
  protected void changeToDirectory(@NonNull final String path) throws FtpException {
    try {
      log.info("{}@{}:{} - Changing to directory '{}'...", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), path);
      if (!client.changeWorkingDirectory(path)) {
        throw new IOException(client.getReplyString());
      }
    } catch (final IOException ex) {
      log.error("{}@{}:{} - Error changing to directory '{}'...", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), path);
      throw new FtpException(ex.getMessage(), ex);
    }
  }

}
