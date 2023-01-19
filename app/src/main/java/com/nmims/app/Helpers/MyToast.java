package com.nmims.app.Helpers;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.nmims.app.R;

public class MyToast
{
    private Context context;
    private static MyToast instance;

    public MyToast(Context context) {
        this.context = context;
    }

    public synchronized static MyToast getInstance(Context context) {
        if (instance == null) {
            instance = new MyToast(context);
        }
        return instance;
    }


    public void showLongMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void showSmallMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public void showLongCustomToast(String message) {

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) ((Activity) context).findViewById(R.id.customLayoutContainer));
        TextView msgTv = layout.findViewById(R.id.customTextMsg);
        msgTv.setText(message);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 350);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    public void showSmallCustomToast(String message) {

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) ((Activity) context).findViewById(R.id.customLayoutContainer));
        TextView msgTv = layout.findViewById(R.id.customTextMsg);
        msgTv.setText(message);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 350);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
