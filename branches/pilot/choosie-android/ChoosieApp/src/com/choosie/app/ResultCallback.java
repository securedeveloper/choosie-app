package com.choosie.app;


public abstract class ResultCallback<Result, Parameter> {
	abstract Result getData(Parameter param,
			Callback<Void, Object, Void> progressCallback);
}
