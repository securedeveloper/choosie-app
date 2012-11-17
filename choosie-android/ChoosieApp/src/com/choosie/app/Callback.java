package com.choosie.app;

public abstract class Callback<Progress, Result> {
	abstract void onOperationFinished(Result param);
	
	public void onProgress(Progress param) {
		
	}
}
