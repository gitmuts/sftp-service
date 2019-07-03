package com.filesender.sftp.model;

/**
 * ProcessResponse
 */
public class ProcessResponse {

    private String serverName;
    private boolean sent;
    
    
	public ProcessResponse(String serverName, boolean sent) {
		super();
		this.serverName = serverName;
		this.sent = sent;
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