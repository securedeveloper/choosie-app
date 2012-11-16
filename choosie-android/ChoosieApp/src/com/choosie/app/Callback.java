package com.choosie.app;

public abstract class Callback<T> {
	abstract void handleOperationFinished(T param);
}
