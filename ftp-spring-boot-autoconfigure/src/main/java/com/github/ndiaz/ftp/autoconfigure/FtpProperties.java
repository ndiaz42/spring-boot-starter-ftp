package com.github.ndiaz.ftp.autoconfigure;

import com.github.ndiaz.ftp.FtpClient;
import com.github.ndiaz.ftp.config.FtpClientConfiguration;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.support.AbstractApplicationContext;

@Slf4j
@ConfigurationProperties(prefix = FtpProperties.PREFIX_FTP)
public class FtpProperties {

  public static final String PREFIX_FTP = "ftp";

  @Autowired
  private AbstractApplicationContext applicationContext;
  @Getter
  @Setter
  private List<FtpClientConfiguration> clients;

  @PostConstruct
  public void init() {
    final BeanDefinitionRegistry registry =
        (BeanDefinitionRegistry) applicationContext.getBeanFactory();
    for (final FtpClientConfiguration ftpConfiguration : clients) {
      final Class<? extends FtpClient> ftpClientClass =
          Protocol.valueOf(ftpConfiguration.getProtocol()).getFtpClientClass();
      final GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
      beanDefinition.setBeanClass(ftpClientClass);
      beanDefinition.setLazyInit(false);
      beanDefinition.setAbstract(false);
      beanDefinition.setAutowireCandidate(true);
      beanDefinition.addQualifier(new AutowireCandidateQualifier(ftpConfiguration.getName()));
      final ConstructorArgumentValues cav = new ConstructorArgumentValues();
      cav.addIndexedArgumentValue(0, ftpConfiguration);
      beanDefinition.setConstructorArgumentValues(cav);
      beanDefinition.setScope("prototype");
      registry.registerBeanDefinition(ftpConfiguration.getName(), beanDefinition);
      log.info("Registered FtpClient '{}' in application context", ftpConfiguration.getName());
    }
  }
}