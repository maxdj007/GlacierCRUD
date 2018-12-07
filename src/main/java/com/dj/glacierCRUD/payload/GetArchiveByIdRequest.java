package com.dj.glacierCRUD.payload;

public class GetArchiveByIdRequest {

	private String archiveId;
	private String vaultName;
	private String accesskey;
	private String secretKey;
	private String retrieval;
	private String AWSEndpoint;


	public String getAWSEndpoint() { return AWSEndpoint; }
	public void setAWSEndpoint(String AWSEndpoint) { this.AWSEndpoint = AWSEndpoint; }
	public String getRetrieval() {
		return retrieval;
	}
	public void setRetrieval(String retrieval) {
		this.retrieval = retrieval;
	}
	public String getArchiveId() {
		return archiveId;
	}
	public void setArchiveId(String archiveId) {
		this.archiveId = archiveId;
	}
	public String getVaultName() {
		return vaultName;
	}
	public void setVaultName(String vaultName) {
		this.vaultName = vaultName;
	}
	public String getAccesskey() {
		return accesskey;
	}
	public void setAccesskey(String accesskey) {
		this.accesskey = accesskey;
	}
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	
	
	
}
