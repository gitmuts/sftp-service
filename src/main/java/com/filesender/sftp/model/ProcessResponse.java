package com.filesender.sftp.model;

/**
 * ProcessResponse
 */
public class ProcessResponse {

    private String serverName;
    private boolean sent;
    private long recordId;
    
	public ProcessResponse(String serverName, boolean sent, long recordId) {
		super();
		this.serverName = serverName;
		this.sent = sent;
		this.recordId = recordId;
	}
	
	
	public long getRecordId() {
		return recordId;
	}


	public void setRecordId(long recordId) {
		this.recordId = recordId;
	}


	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	public boolean isSent() {
		return sent;
	}
	public void setSent(boolean sent) {
		this.sent = sent;
	}

    
    
    
}