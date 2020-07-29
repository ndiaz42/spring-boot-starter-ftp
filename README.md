# Spring Boot Starter FTP
> A Spring Boot starter to easily connect, download and upload files to a server via FTP or SFTP.
>
> Uses [Apache Commons Net library](https://commons.apache.org/proper/commons-net/) for FTP and [Jcraft's jsch library](http://www.jcraft.com/jsch/) for SFTP.

## How to install

#### Requirements

- [Maven](https://maven.apache.org/download.cgi)
- [Spring Boot 2.2.x](https://spring.io/projects/spring-boot)

#### Installation

```bash
git clone https://github.com/ndiaz42/spring-boot-starter-ftp.git
cd spring-boot-starter-ftp
mvn clean install
```

## Usage

#### Adding the dependency
Add this dependecy to your project's `pom.xml`
```xml
<dependency>
    <groupId>com.github.ndiaz42</groupId>
    <artifactId>ftp-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

#### Injecting the `FtpFactory`
The `FtpFactory` is managed by the application context. You can use this class to instantiate a new `FtpClient`. 
```java
@Autowired
private FtpFactory ftpFactory;
```

#### Using the `FtpFactory` and `FtpClient`

Obtain a new instance of `FtpClient` via its name (see [Configuration](#Configuration)).

```java
  FtpClient myFtp = ftpFactory.getFtp("myFtp");
```

To download a file, provide a path to it. The `FtpClient` manages the opening and closing of the connection, changing
folders, etc. If the folder or file doesn't exists, it throws an exception.

```java
try {
  FtpFile file = myFtp.download("path/to/file.txt");
  log.info("File '{}' downloaded!", file.getName());
} catch (FtpException e) {
  log.error("Oops, something bad happened!", e);
}
```

To upload a file, construct a `FtpFile` and providing a name, a path to save and the actual file as a byte array. 
The `FtpClient` manages the opening and closing of the connection, changing folders, etc. 
If the folder doesn't exists, it creates it.

```java
byte[] byteArray; // file to upload
try {
  FtpFile file2 = new FtpFile();
  file2.setName("file2.txt");
  file2.setPath("path/to/");
  file2.setFile(byteArray);
  myFtp.upload(file2);
} catch (FtpException e) {
  log.error("Oops, something bad happened!", e);
}
```

## Configuration

Add the following properties to your `application.properties` or `application.yml` file.
You can add as many as FTP clients you need.

```yaml
ftp:
  clients:
    myFtp:
      host: [ftp server IP or name]
      port: [ftp server port]
      username: [client's username]
      password: [client's password]
      protocol: [FTP or SFTP]
      root: [optional - root folder to use in all ftp operations]
    anotherFtp:
      host: 
      port: 
      username: 
      password: 
      protocol: 
```