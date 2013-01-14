package com.choozie.app;

public abstract class Callback<Pre, Progress, Result> {

	public void onPre(Pre param) {
	}

	public void onFinish(Result param) {
	}

	public void onProgress(Progress param) {
	}
}
