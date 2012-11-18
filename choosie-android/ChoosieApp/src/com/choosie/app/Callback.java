package com.choosie.app;

public abstract class Callback<Progress, Result> {
	void onFinish(Result param) {
	}

	void onProgress(Progress param) {
	}
}
