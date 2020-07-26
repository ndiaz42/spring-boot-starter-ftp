package com.github.ndiaz.ftp;

import com.github.ndiaz.ftp.exception.FtpException;
import com.github.ndiaz.ftp.model.FtpFile;
import java.util.List;
import lombok.NonNull;

public interface FtpClient {

  void upload(@NonNull FtpFile file) throws FtpException;

  void upload(@NonNull List<FtpFile> file) throws FtpException;

  FtpFile download(@NonNull String path) throws FtpException;

  List<FtpFile> downloadAll(@NonNull String path) throws FtpException;

  void delete(@NonNull String path) throws FtpException;

  void deleteAll(@NonNull String path) throws FtpException;

}
