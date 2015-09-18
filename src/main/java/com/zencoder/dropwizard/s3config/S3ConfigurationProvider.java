/**
 * 
 */
package com.zencoder.dropwizard.s3config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import io.dropwizard.configuration.ConfigurationSourceProvider;

/**
 * Provider to read a Dropwizard configuration file from an S3 location. Accepts
 * an S3 URI referring to the S3 bucket and key where the configuration file is
 * stored. The region reported by the EC2 instance metadata will be used to
 * retrieve the S3 configuration. The standard AWS environment variables
 * <code>AWS_ACCESS_KEY</code> and <code>AWS_SECRET_ACCESS_KEY</code> are used
 * by the AWS SDK.
 * 
 * @author Scott Kidder
 *
 */
public class S3ConfigurationProvider implements ConfigurationSourceProvider {

    private static final String AWS_S3_ENDPOINT_ENV_VAR = "AWS_S3_ENDPOINT";

    private static final String S3_URI_SCHEME = "s3";

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.dropwizard.configuration.ConfigurationSourceProvider#open(java.lang.
     * String)
     */
    @Override
    public InputStream open(String path) throws IOException {
	if (path == null || path.trim().length() == 0) {
	    throw new IOException("S3 URI to configuration file was unspecified or empty");
	}
	
	final URI uri;
	try {
	    uri = new URI(path);
	} catch (URISyntaxException e) {
	    throw new IOException("Unable to parse S3 URI for configuration file", e);
	}

	if (S3_URI_SCHEME.equalsIgnoreCase(uri.getScheme())) {
	    final String bucket = uri.getHost();

	    final String key;
	    if (uri.getPath().length() > 1) {
		// remove leading '/' in path
		key = uri.getPath().substring(1);
	    } else {
		key = uri.getPath();
	    }

	    final AmazonS3 s3Client = new AmazonS3Client();
	    final String alternateS3Endpoint = System.getenv(AWS_S3_ENDPOINT_ENV_VAR);
	    if (alternateS3Endpoint != null) {
		// specify the S3 endpoint, useful for testing against a local
		// fake S3, which means we probably also want to use the bucket
		// name in the path, not as a sub-domain in the host
		s3Client.setEndpoint(alternateS3Endpoint);
		s3Client.setS3ClientOptions(new S3ClientOptions().withPathStyleAccess(true));
	    }
	    final S3Object configFileObject;
	    try {
		configFileObject = s3Client.getObject(new GetObjectRequest(bucket, key));
	    } catch (RuntimeException e) {
		throw new IOException("Error retrieving configuration from S3", e);
	    }

	    return configFileObject.getObjectContent();
	} else {
	    throw new IllegalArgumentException("Configuration file S3 URI uses unsupported scheme: " + uri.getScheme());
	}
    }
}
