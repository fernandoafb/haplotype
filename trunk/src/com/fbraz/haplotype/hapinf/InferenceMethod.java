package com.fbraz.haplotype.hapinf;

import com.fbraz.haplotype.misc.IOHelper;


public abstract class InferenceMethod {
	
	private HapProperties hapProperties;
	protected IOHelper ioHelper;
	
	public InferenceMethod() {
		hapProperties = new HapProperties();
	}

	public InferenceMethod(HapProperties hapProperties) {
		this.hapProperties = hapProperties;
	}
	
	public HapProperties getHapProperties() {
		return hapProperties;
	}

	public void setHapProperties(HapProperties hapProperties) {
		this.hapProperties = hapProperties;
	}	
	
	public IOHelper getIoHelper() {
		return ioHelper;
	}

	public void setIoHelper(IOHelper ioHelper) {
		this.ioHelper = ioHelper;
	}

	public abstract void execute();

}
