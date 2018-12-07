package com.dj.glacierCRUD.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.dj.glacierCRUD.exception.AWSCredentialsException;
import com.dj.glacierCRUD.exception.AWSCustomException;
import com.dj.glacierCRUD.payload.GetArchiveByIdRequest;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.DescribeJobRequest;
import com.amazonaws.services.glacier.model.DescribeJobResult;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.JobParameters;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQSClient;

@Component
public class ArchiveDownloadService {
	
	private static final Logger logger = LoggerFactory.getLogger(ArchiveDownloadService.class);
	
	@Autowired
	private FileStorageService fileStorageService;
	
    private static AmazonGlacierClient glacierClient;
    private static AmazonSQSClient sqsClient;
    private static AmazonSNSClient snsClient;
    
    @SuppressWarnings("deprecation")
	public File getArchivebyId(String archiveId, String access_key, String secret_key, String vaultName){
    	
    	AWSStaticCredentialsProvider credentials = getAWSCredentials(access_key, secret_key);
        glacierClient = new AmazonGlacierClient(credentials);
        sqsClient = new AmazonSQSClient(credentials);
        snsClient = new AmazonSNSClient(credentials);
        glacierClient.setEndpoint("glacier.eu-west-1.amazonaws.com");
        sqsClient.setEndpoint("sqs.eu-west-1.amazonaws.com");
        snsClient.setEndpoint("sns.eu-west-1.amazonaws.com");
        
        File newFile = new File(fileStorageService.getFileLocationPath().toString());

        try {
            ArchiveTransferManager atm = new ArchiveTransferManager(glacierClient, sqsClient, snsClient);
            
            atm.download(vaultName, archiveId, newFile);
            System.out.println("Downloaded file to " + newFile.getAbsolutePath());
            
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        
        return newFile;
    }

	public String ArchiveRetrieval(GetArchiveByIdRequest getArchiveByIdRequest) throws AWSCredentialsException, AWSCustomException {

		if(getArchiveByIdRequest.getArchiveId() == null || getArchiveByIdRequest.getArchiveId().equals("")){
			throw new AWSCustomException("The Archive Id name was not provided");
		}

		if(getArchiveByIdRequest.getVaultName() == null || getArchiveByIdRequest.getVaultName().equals("")){
			throw new AWSCustomException("The vault name was not provided");
		}

		return lowLevelApiArchiveRetrieval(getArchiveByIdRequest);
	}

    
    @SuppressWarnings("deprecation")
	private String lowLevelApiArchiveRetrieval(GetArchiveByIdRequest getArchiveByIdRequest) throws AWSCredentialsException, AWSCustomException {

    	AWSStaticCredentialsProvider credentials = getAWSCredentials(getArchiveByIdRequest.getAccesskey(), getArchiveByIdRequest.getSecretKey());
        glacierClient = new AmazonGlacierClient(credentials);
        glacierClient.setEndpoint(
        		(getArchiveByIdRequest.getAWSEndpoint() == null || "".equals(getArchiveByIdRequest.getAWSEndpoint())) ? "glacier.eu-west-1.amazonaws.com"
						: getArchiveByIdRequest.getAWSEndpoint());
        JobParameters jobParameters = getJobParameters(getArchiveByIdRequest.getArchiveId(), getArchiveByIdRequest.getRetrieval());

        InitiateJobResult initiateJobResult = getJobResults(getArchiveByIdRequest.getVaultName(), jobParameters);

        String jobId = initiateJobResult.getJobId();

        runDescribeJob(jobId, getArchiveByIdRequest.getVaultName(), credentials,
				(getArchiveByIdRequest.getAWSEndpoint() == null || "".equals(getArchiveByIdRequest.getAWSEndpoint())) ? "glacier.eu-west-1.amazonaws.com"
						: getArchiveByIdRequest.getAWSEndpoint());
    	
    	return jobId;
    }
    
    private void runDescribeJob(String jobId, String vaultName, AWSStaticCredentialsProvider credentials, String endpoint){
    	
    	Thread t = new Thread(() -> {
			AmazonGlacierClient glacierClient = new AmazonGlacierClient(credentials);
			glacierClient.setEndpoint(endpoint);
			DescribeJobRequest request = new DescribeJobRequest(vaultName, jobId);
			boolean flag = false;
			while(!flag){
				DescribeJobResult result = glacierClient.describeJob(request);
				if(!result.getStatusCode().equals("Failed")){
					if(result.isCompleted()){
						flag = true;
						try {
								downloadFile(jobId, vaultName, glacierClient);
						} catch (IOException e) {
							logger.error("Exception :" +e.getMessage() + "with cause :" + e.getCause());
						}
					} else {
						logger.info("Job yet not completed sleeping for one minute");
						try {
							Thread.sleep(60000);
						} catch (InterruptedException e) {
							logger.error(e.getMessage() + "with cause :" + e.getCause());
						}
					}
				} else {
					flag = true;
					logger.error("Job with job id : {} has failed", jobId);
				}
			}
		});

		t.start();
    	
    }
    
    private File downloadFile(String jobId, String vaultName, AmazonGlacierClient glacierClient) throws IOException{
    	GetJobOutputRequest jobOutputRequest = new GetJobOutputRequest()
    	        .withJobId(jobId)
    	        .withVaultName(vaultName);
    	GetJobOutputResult jobOutputResult = glacierClient.getJobOutput(jobOutputRequest);
    	File file = new File(fileStorageService.getFileLocationPath()+"\\"+jobId);
    	if(file.createNewFile()){
    		logger.info("file.txt File Created in Project root directory");
    	}else {
    		logger.info("File file.txt already exists in project root directory");
    	}
    	InputStream intput = jobOutputResult.getBody();
    	
	    OutputStream outStream = new FileOutputStream(file);
	 
	    byte[] buffer = new byte[8 * 1024];
	    int bytesRead;
	    while ((bytesRead = intput.read(buffer)) != -1) {
	        outStream.write(buffer, 0, bytesRead);
	    }
	    IOUtils.closeQuietly(intput);
	    IOUtils.closeQuietly(outStream);
	    
	    return file;
    }
    
    private AWSStaticCredentialsProvider getAWSCredentials(String access_key, String secret_key) throws AWSCredentialsException {
    	if(access_key == null || access_key.equals("")){
    		throw new AWSCredentialsException("AWS credentials not present");
    	}
    	if(secret_key == null || secret_key.equals("")){
			throw new AWSCredentialsException("AWS credentials not present");
    	}
    	BasicAWSCredentials awsCreds = new BasicAWSCredentials(access_key, secret_key);
    	return new AWSStaticCredentialsProvider(awsCreds);
    }
    
    private JobParameters getJobParameters(String archiveId, String retrieval){
    	return new JobParameters()
        	    .withArchiveId(archiveId)
        	    .withDescription("archive retrieval")
        	    .withType("archive-retrieval")
        	    .withTier((retrieval==null || retrieval.equals("")) ? "Standard" : retrieval);
    }
    
    private InitiateJobResult getJobResults(String vaultName, JobParameters jobParameters){
    	return glacierClient.initiateJob(new InitiateJobRequest()
        	    .withJobParameters(jobParameters)
        	    .withVaultName(vaultName));
    }

}
