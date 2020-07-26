package com.github.ndiaz.ftp.impl;

import com.github.ndiaz.ftp.config.FtpClientConfiguration;
import com.github.ndiaz.ftp.exception.FtpException;
import com.github.ndiaz.ftp.model.FtpFile;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class JCraftSftpClient extends GenericFtpClient {

  private Session session;
  private ChannelSftp channel;

  public JCraftSftpClient(final FtpClientConfiguration configuration) {
    super(configuration);
  }

  @Override
  protected void connect() throws FtpException {
    super.connect();
    if (channel != null && channel.isConnected() || session != null && session.isConnected()) {
      throw new FtpException(
          "SFTP Client is already connected to " + session.getHost() + ":" + session.getPort());
    }
    try {
      log.info("Connecting to {}:{} with '{}'...", configuration.getHost(), configuration.getPort(),
          configuration.getUsername());
      session = new JSch().getSession(configuration.getUsername(), configuration.getHost(),
          configuration.getPort());
      session.setPassword(configuration.getPassword());
      session.setConfig("StrictHostKeyChecking", "no");
      session.setTimeout(10000); // 10 seconds
      session.connect();
      channel = (ChannelSftp) session.openChannel("sftp");
      channel.connect();
      log.info("Connected to {}:{} with '{}'!", configuration.getHost(), configuration.getPort(),
          configuration.getUsername());
      changeToDirectory(configuration.getRoot());
    } catch (final JSchException ex) {
      log.error("Failed to connect to {}:{} with '{}'!", configuration.getHost(),
          configuration.getPort(),
          configuration.getUsername());
      throw new FtpException(ex.getMessage(), ex);
    }
  }

  @Override
  protected void disconnect() {
    if (session != null) {
      session.disconnect();
      session = null;
    }
    if (channel != null) {
      channel.disconnect();
      channel = null;
    }
    log.info("Disconnected from {}:{} with '{}'", configuration.getHost(), configuration.getPort(),
        configuration.getUsername());
  }

  @Override
  protected List<String> listFiles() throws FtpException {
    try {
      log.info("{}@{}:{} - Listing files in current directory...", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort());
      final List<String> filenames = new ArrayList<>();
      final Vector lsEntries = channel.ls(channel.pwd());
      for (final Object lsEntry : lsEntries) {
        final ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) lsEntry;
        if (!entry.getAttrs().isDir()) {
          filenames.add(entry.getFilename());
          log.info("{}@{}:{} -    {}", configuration.getUsername(), configuration.getHost(),
              configuration.getPort(), entry.getFilename());
        }
      }
      if (filenames.isEmpty()) {
        log.info("{}@{}:{} -    [empty directory]", configuration.getUsername(),
            configuration.getHost(),
            configuration.getPort());
      }
      return filenames;
    } catch (final SftpException ex) {
      log.error("{}@{}:{} - Could not list files in current directory", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort());
      throw new FtpException(ex.getMessage(), ex);
    }
  }

  @Override
  protected InputStream downloadFile(@NonNull final String name) throws FtpException {
    try {
      log.info("{}@{}:{} - Downloading file '{}'...", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), name);
      final InputStream file = channel.get(name);
      log.info("{}@{}:{} - File '{}' downloaded!", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), name);
      return file;
    } catch (final SftpException ex) {
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
      final SftpATTRS lstat = channel.lstat(name);
      if (lstat.isDir()) {
        throw new FtpException("'" + name + "' is not a file");
      }
      final byte[] bytes = IOUtils.toByteArray(file);
      final FtpFile ftpFile = new FtpFile();
      ftpFile.setName(name);
      ftpFile.setExtension(FilenameUtils.getExtension(name));
      ftpFile.setFile(bytes);
      ftpFile.setSize(lstat.getSize());
      ftpFile.setAccessed(LocalDateTime.ofEpochSecond(lstat.getMTime(), 0, ZoneOffset.UTC));
      ftpFile.setModified(LocalDateTime.ofEpochSecond(lstat.getMTime(), 0, ZoneOffset.UTC));
      ftpFile.setPath(configuration.getRoot() + "/" + channel.pwd());
      return ftpFile;
    } catch (final Exception ex) {
      log.error("{}@{}:{} - Could not populate file details for file '{}'...",
          configuration.getUsername(),
          configuration.getHost(), configuration.getPort(), name);
      throw new FtpException(ex.getMessage(), ex);
    }
  }

  @Override
  protected void uploadFile(@NonNull final InputStream file, @NonNull final String name)
      throws FtpException {
    try {
      log.info("{}@{}:{} - Uploading file as '{}'...", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), name);
      channel.put(file, name);
      log.info("{}@{}:{} - File '{}' uploaded!", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), name);
    } catch (final SftpException ex) {
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
      channel.rm(name);
      log.info("{}@{}:{} - File '{}' deleted!", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), name);
    } catch (final SftpException ex) {
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
            channel.cd(path);
          } catch (final SftpException ex) {
            channel.mkdir(path);
            channel.cd(path);
          }
        }
      }
      log.info("{}@{}:{} - Directory '{}' created!", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), name);
    } catch (final SftpException ex) {
      log.error("{}@{}:{} - Could not create directory '{}'...", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), name);
      throw new FtpException(ex.getMessage(), ex);
    }
  }

  @Override
  protected void changeToDirectory(@NonNull final String path) throws FtpException {
    try {
      log.info("{}@{}:{} - Changing to directory '{}'...", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), path);
      channel.cd(path);
    } catch (final SftpException ex) {
      log.error("{}@{}:{} - Error changing to directory '{}'...", configuration.getUsername(),
          configuration.getHost(),
          configuration.getPort(), path);
      throw new FtpException(ex.getMessage(), ex);
    }
  }

}
