package com.dj.glacierCRUD.payload;

public class ArchiveRetrieveResponseTwo {
	
	private String status;
	private String message;
	private boolean error;
	private String jobId;
	private String errorMessage;
	
	public ArchiveRetrieveResponseTwo(String status, String message,  boolean error, String jobId, String errorMessage){
		this.status = status;
		this.message = message;
		this.error = error;
		this.jobId = jobId;
		this.errorMessage = errorMessage;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public boolean isError() {
		return error;
	}
	public void setError(boolean error) {
		this.error = error;
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
