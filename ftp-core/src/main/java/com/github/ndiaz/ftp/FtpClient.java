package com.github.ndiaz.ftp;

import com.github.ndiaz.ftp.exception.FtpException;
import com.github.ndiaz.ftp.model.FtpFile;
import java.util.List;
import lombok.NonNull;

/**
 * API for all the operations a FTP client can perform.
 */
public interface FtpClient {

  /**
   * Uploads a file to the FTP server.
   *
   * <p>The file must contain the path to where it should be uploaded.
   *
   * <p>If a folder doesn't exists, it will be created.
   *
   * @param file the file to upload
   * @throws FtpException if it can't upload the file to the server
   */
  void upload(@NonNull FtpFile file) throws FtpException;

  /**
   * Uploads files to the FTP server, in order.
   *
   * <p>Each file must contain the path to where it should be uploaded (different paths for each
   * file is supported).
   *
   * <p>If a folder doesn't exists, it will be created.
   *
   * @param files the files to upload
   * @throws FtpException if it can't upload a file to the server
   */
  void upload(@NonNull List<FtpFile> files) throws FtpException;

  /**
   * Downloads a file from the FTP server.
   *
   * @param path the path of the file (including the file name)
   * @return the ftp file
   * @throws FtpException if the file or a folder doesn't exists, or it can't download the file
   */
  FtpFile download(@NonNull String path) throws FtpException;

  /**
   * Downloads all the files from the FTP server, given a path.
   *
   * @param path the path to download files from
   * @return the lists of files present
   * @throws FtpException if the folder doesn't exists, or it can't download a file
   */
  List<FtpFile> downloadAll(@NonNull String path) throws FtpException;

  /**
   * Deletes a file from the FTP server.
   *
   * @param path the path of the file (including the file name)
   * @throws FtpException if the file can't be deleted
   */
  void delete(@NonNull String path) throws FtpException;

  /**
   * Deletes all the file from the FTP server, given a path.
   *
   * @param path the path to delete files from
   * @throws FtpException if the folder doesn't exists, or it can't delete a file
   */
  void deleteAll(@NonNull String path) throws FtpException;

}
