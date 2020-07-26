package com.github.ndiaz.ftp.exception;

public class FtpException extends Exception {

  public FtpException() {
    super();
  }

  public FtpException(final String message) {
    super(message);
  }

  public FtpException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public FtpException(final Throwable cause) {
    super(cause);
  }

}

