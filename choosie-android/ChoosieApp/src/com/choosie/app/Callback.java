package com.choosie.app;

public abstract class Callback<Pre, Progress, Result> {

	void onPre(Pre param) {
	}

	void onFinish(Result param) {
	}

	void onProgress(Progress param) {
	}
}
