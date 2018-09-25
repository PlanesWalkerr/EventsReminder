package com.makhovyk.eventsreminder.utils;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogHelper {

    ProgressDialog progressDialog;
    Context context;

    public ProgressDialogHelper(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
    }

    public void showProgressDialog(String message) {
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        progressDialog.dismiss();
    }
}
