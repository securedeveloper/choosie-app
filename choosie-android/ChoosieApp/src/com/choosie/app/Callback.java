package com.choosie.app;

public abstract class Callback<Progress, Result> {
	abstract void onOperationFinished(Result param);

	void onProgress(Progress param) {

	}
}
