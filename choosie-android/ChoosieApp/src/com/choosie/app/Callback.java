package com.choosie.app;

public abstract class Callback<T> {
	abstract void onOperationFinished(T param);
}
