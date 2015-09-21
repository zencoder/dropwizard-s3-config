/**
 * 
 */
package com.brightcove.dropwizard.s3config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.GetObjectRequest;

import io.dropwizard.configuration.ConfigurationSourceProvider;

/**
 * Provider to read a Dropwizard configuration file from an S3 location. Accepts
 * an S3 URI referring to the S3 bucket and key where the configuration file can
 * be found. The standard AWS environment variables <code>AWS_ACCESS_KEY</code>
 * and <code>AWS_SECRET_ACCESS_KEY</code> are used for authentication by the AWS
 * SDK.
 * 
 * If the <code>AWS_S3_ENDPOINT</code> environment variable is set, then it will
 * be supplied to the S3 client as the service endpoint. This is useful for
 * testing an application with a fake S3 server running locally or some other
 * machine that's not part of S3.
 * 
 * If the <code>AWS_REGION</code> environment variable is set, then it will be
 * used to override the Region that would otherwise be read from the EC2
 * instance metadata. This allows you to read from an S3 bucket in a region
 * other than the one the application is running in.
 * 
 * @author Scott Kidder
 *
 */
public class S3ConfigurationProvider implements ConfigurationSourceProvider {

    private static final String AWS_S3_ENDPOINT_ENV_VAR = "AWS_S3_ENDPOINT";
    private static final String AWS_REGION_ENV_VAR = "AWS_REGION";
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
	    if (System.getenv(AWS_S3_ENDPOINT_ENV_VAR) != null) {
		s3Client.setEndpoint(System.getenv(AWS_S3_ENDPOINT_ENV_VAR));
		s3Client.setS3ClientOptions(new S3ClientOptions().withPathStyleAccess(true));
	    } else if (System.getenv(AWS_REGION_ENV_VAR) != null) {
		s3Client.setRegion(Region.getRegion(Regions.fromName(System.getenv(AWS_REGION_ENV_VAR))));
	    }

	    try {
		return s3Client.getObject(new GetObjectRequest(bucket, key)).getObjectContent();
	    } catch (RuntimeException e) {
		throw new IOException("Error retrieving configuration from S3", e);
	    }
	} else {
	    throw new IOException("Configuration file S3 URI uses unsupported scheme: " + uri.getScheme());
	}
    }
}
