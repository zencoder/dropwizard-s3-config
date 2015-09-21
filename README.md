# dropwizard-s3-config
Dropwizard provider to retrieve application configuration from an S3 URI.

## Maven
```xml
<dependency>
  <groupId>com.zencoder</groupId>
  <artifactId>dropwizard-s3-config</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Gradle
```
    dependencies {
        compile(
        ...
                'com.zencoder:dropwizard-s3-config:1.0.0',
```

## Usage

Simply set the `ConfigurationSourceProvider` in the `initialize` method to refer to an `S3ConfigurationProvider` instance:
```java
public class MyApplication extends Application<MyConfiguration> {

    @Override
    public void initialize(Bootstrap<MyConfiguration> bootstrap) {
        // load configuration from an S3 URI
	    bootstrap.setConfigurationSourceProvider(new S3ConfigurationProvider());
    }
}
```

# Configuration
## S3 URI
Supply the S3 URI for the configuration file to the `server` Dropwizard command.  For example:
```shell
java -jar target/my-app.jar server s3://bucket/key/config.yml
```

## Environment Variables
| Name  | Purpose  | Required?  |
|---|---|---|
|  `AWS_ACCESS_KEY` |  Access key ID | Yes  |
|  `AWS_SECRET_ACCESS_KEY` | Secret Access Key  | Yes  |
| `AWS_S3_ENDPOINT`  |  Alternate endpoint for S3 server, useful for testing against a local fake S3 server | No  |
| `AWS_REGION` | AWS region to use when accessing S3  |  No |
