package marubinotto.piggydb.ui;

import marubinotto.piggydb.model.entity.RawEntity;

public class WarSetting {
	
	// piggydbVersion
	
	private String piggydbVersion;

	public String getPiggydbVersion() {
		return piggydbVersion;
	}

	public void setPiggydbVersion(String piggydbVersion) {
		this.piggydbVersion = piggydbVersion;
	}
	
	
	// clientAddressAuthEnabled

	private boolean clientAddressAuthEnabled = false;

	public boolean isClientAddressAuthEnabled() {
		return clientAddressAuthEnabled;
	}

	public void setClientAddressAuthEnabled(boolean clientAddressAuthEnabled) {
		this.clientAddressAuthEnabled = clientAddressAuthEnabled;
	}
	
	
	// userAgentAuthEnabled
	
	private boolean userAgentAuthEnabled = false;

	public boolean isUserAgentAuthEnabled() {
		return userAgentAuthEnabled;
	}

	public void setUserAgentAuthEnabled(boolean userAgentAuthEnabled) {
		this.userAgentAuthEnabled = userAgentAuthEnabled;
	}
	
	
	// feedMaxSize
	
	private int feedMaxSize = 30;

	public int getFeedMaxSize() {
		return this.feedMaxSize;
	}

	public void setFeedMaxSize(int feedMaxSize) {
		this.feedMaxSize = feedMaxSize;
	}
	
	
	// RawEntity.changeableOnlyForCreator
	
	public void setEntityChangeableOnlyForCreator(boolean entityChangeableOnlyForCreator) {
		RawEntity.changeableOnlyForCreator = entityChangeableOnlyForCreator;
	}
	
	
	// defaultFragmentsViewScale
	
	private Integer defaultFragmentsViewScale;

	public Integer getDefaultFragmentsViewScale() {
		return this.defaultFragmentsViewScale != null ? this.defaultFragmentsViewScale : 1000;
	}

	public void setDefaultFragmentsViewScale(Integer defaultFragmentsViewScale) {
		this.defaultFragmentsViewScale = defaultFragmentsViewScale;
	}
	
	
	// allowsOnlyOwnerToUploadFile
	
	private boolean allowsOnlyOwnerToUploadFile = false;

	public boolean isAllowsOnlyOwnerToUploadFile() {
		return this.allowsOnlyOwnerToUploadFile;
	}

	public void setAllowsOnlyOwnerToUploadFile(boolean allowsOnlyOwnerToUploadFile) {
		this.allowsOnlyOwnerToUploadFile = allowsOnlyOwnerToUploadFile;
	}
}
