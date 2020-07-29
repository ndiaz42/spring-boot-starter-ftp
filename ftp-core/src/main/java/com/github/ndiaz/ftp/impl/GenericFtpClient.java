package com.github.ndiaz.ftp.impl;

import com.github.ndiaz.ftp.FtpClient;
import com.github.ndiaz.ftp.config.FtpClientConfig;
import com.github.ndiaz.ftp.exception.FtpException;
import com.github.ndiaz.ftp.model.FtpFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public abstract class GenericFtpClient implements FtpClient {

  protected FtpClientConfig configuration;

  public GenericFtpClient(final FtpClientConfig configuration) {
    this.configuration = configuration;
  }

  @Override
  public void upload(@NonNull final FtpFile file) throws FtpException {
    try {
      connect();
      final String path = StringUtils.trimToNull(file.getPath());
      try {
        changeToDirectory(path);
      } catch (final FtpException ex) {
        createDirectory(path);
      }
      try (ByteArrayInputStream inputStream = new ByteArrayInputStream(file.getFile())) {
        uploadFile(inputStream, file.getName());
      } catch (final IOException ex) {
        throw new FtpException(ex.getMessage(), ex);
      }
    } finally {
      disconnect();
    }
  }

  @Override
  public void upload(@NonNull final List<FtpFile> files) throws FtpException {
    try {
      connect();
      String prevPath = configuration.getRoot();
      for (final FtpFile file : files) {
        final String path = StringUtils.trimToNull(file.getPath());
        if (!prevPath.equals(path)) {
          try {
            changeToDirectory(path);
          } catch (final FtpException ex) {
            createDirectory(path);
          }
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(file.getFile())) {
          uploadFile(inputStream, StringUtils.trimToNull(file.getName()));
        } catch (final IOException ex) {
          throw new FtpException(ex.getMessage(), ex);
        }
        prevPath = path;
      }
    } finally {
      disconnect();
    }
  }

  @Override
  public FtpFile download(@NonNull final String path) throws FtpException {
    try {
      final String pathToFile = StringUtils.trimToNull(FilenameUtils.getPathNoEndSeparator(path));
      final String fileName = StringUtils.trimToNull(FilenameUtils.getName(path));
      connect();
      changeToDirectory(pathToFile);
      try (InputStream file = downloadFile(fileName)) {
        return populateFileDetails(file, fileName);
      } catch (final IOException ex) {
        log.error("{}@{}:{} - Could not download file '{}'", configuration.getUsername(),
            configuration.getHost(), configuration.getPort(), path);
        throw new FtpException(ex.getMessage(), ex);
      }
    } finally {
      disconnect();
    }
  }

  @Override
  public List<FtpFile> downloadAll(@NonNull final String path) throws FtpException {
    try {
      final List<FtpFile> files = new ArrayList<>();
      connect();
      changeToDirectory(StringUtils.trimToNull(path));
      final List<String> fileNames = listFiles();
      for (final String fileName : fileNames) {
        try (InputStream file = downloadFile(fileName)) {
          files.add(populateFileDetails(file, fileName));
        } catch (final IOException ex) {
          log.error("{}@{}:{} - Could not download file '{}'", configuration.getUsername(),
              configuration.getHost(), configuration.getPort(), path);
          throw new FtpException(ex.getMessage(), ex);
        }
      }
      return files;
    } finally {
      disconnect();
    }
  }

  @Override
  public void delete(@NonNull final String path) throws FtpException {
    try {
      final String pathToFile = StringUtils.trimToNull(FilenameUtils.getPathNoEndSeparator(path));
      final String fileName = StringUtils.trimToNull(FilenameUtils.getName(path));
      connect();
      changeToDirectory(pathToFile);
      deleteFile(fileName);
    } finally {
      disconnect();
    }
  }

  @Override
  public void deleteAll(@NonNull final String path) throws FtpException {
    try {
      connect();
      changeToDirectory(StringUtils.trimToNull(path));
      final List<String> fileNames = listFiles();
      for (final String fileName : fileNames) {
        deleteFile(fileName);
      }
    } finally {
      disconnect();
    }
  }

  protected void connect() throws FtpException {
    if (StringUtils.isAnyBlank(configuration.getHost(), String.valueOf(configuration.getPort()),
        configuration.getUsername(),
        configuration.getPassword(), configuration.getRoot())) {
      throw new FtpException("Cannot connect: properties are not configured");
    }
  }

  protected abstract void disconnect();

  protected abstract List<String> listFiles() throws FtpException;

  protected abstract InputStream downloadFile(@NonNull String name) throws FtpException;

  protected abstract FtpFile populateFileDetails(@NonNull InputStream file, @NonNull String name)
      throws FtpException;

  protected abstract void uploadFile(@NonNull InputStream file, @NonNull String name)
      throws FtpException;

  protected abstract void deleteFile(@NonNull String name) throws FtpException;

  protected abstract void createDirectory(@NonNull String name) throws FtpException;

  protected abstract void changeToDirectory(@NonNull String path) throws FtpException;

}
