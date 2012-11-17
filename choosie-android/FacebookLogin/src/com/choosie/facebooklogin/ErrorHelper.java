package com.choosie.facebooklogin;

//import val.to.config.Constants;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

public class ErrorHelper {

	public static AlertDialog showErrorAlert(Context context, String errorMessage, String details) {
		AlertDialog.Builder alertbox = new AlertDialog.Builder(context);

		alertbox.setIcon(android.R.drawable.ic_dialog_info);
		if (details != null){
			alertbox.setTitle(errorMessage);
			alertbox.setMessage(details);
		}
		else{
			alertbox.setTitle("Error");
			alertbox.setMessage(errorMessage);	
		}
		
		alertbox.setNeutralButton("Close", null);
		return alertbox.show();
	}

	public static void handleError(Exception ex){
		ex.printStackTrace();
		Log.d("Choosie", ex.getMessage());
	}
}
