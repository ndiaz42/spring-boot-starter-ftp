package com.github.ndiaz.ftp.factory;

import com.github.ndiaz.ftp.FtpClient;
import com.github.ndiaz.ftp.exception.FtpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@Slf4j
public class FtpFactory {

  @Autowired
  private ApplicationContext applicationContext;

  public FtpClient getFtp(final String name) throws FtpException {
    try {
      return applicationContext.getBean(name, FtpClient.class);
    } catch (final BeansException ex) {
      log.error("No FtpClient bean with name '{}' has been found", name);
      throw new FtpException("No FtpClient bean with name '" + name + "' has been found", ex);
    }
  }

  public FtpClient getFtp() throws FtpException {
    try {
      return applicationContext.getBean(FtpClient.class);
    } catch (final NoUniqueBeanDefinitionException ex) {
      log.error("No unique FtpClient bean has been found", ex);
      throw new FtpException("No unique FtpClient bean has been found", ex);
    } catch (final NoSuchBeanDefinitionException ex) {
      log.error("No FtpClient bean has been found", ex);
      throw new FtpException("No FtpClient bean has been found", ex);
    }
  }

}
