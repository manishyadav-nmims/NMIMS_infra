package com.nmims.app.Helpers;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class CommonMethods extends HurlStack {

    public void OkHttpUrlStack (Context context){

    }
    //region(Fields)
    private static ProgressDialog progressDialog;
    private static Context context;
    public static  String str;
    //endregion

    public static void showDialog(Context ctx) {
        try {
            context = ctx;
            closeDialog();
            progressDialog = new ProgressDialog(ctx);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable
                    (android.graphics.Color.TRANSPARENT));
            progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                return myTrustedAnchors;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                try {
                    certs[0].checkValidity();
                } catch (Exception e) {
                    try {
                        throw new CertificateException("Certificate not valid or trusted.");
                    } catch (CertificateException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                try {
                    certs[0].checkValidity();
                } catch (Exception e) {
                    try {
                        throw new CertificateException("Certificate not valid or trusted.");
                    } catch (CertificateException ex) {
                        ex.printStackTrace();
                    }
                }

            }
        } };
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                //Log.d("hostname",hostname);
                if (!"https://portal.svkm.ac.in1".equalsIgnoreCase(hostname)) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        //----------------------------------------

       /* try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                            return myTrustedAnchors;
                        }
                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession arg1) {
                    if (!hostname.equalsIgnoreCase("ec2-3-7-84-108.ap-south-1.compute.amazonaws.com")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        } catch (Exception e) {
        }*/
  //-------------------------------------------------------------------------------------------------------//
      /*  try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType){
                    try {
                        certs[0].checkValidity();
                    } catch (Exception e) {
                        try {
                            throw new CertificateException("Certificate not valid or trusted.");
                        } catch (CertificateException ex) {
                            ex.printStackTrace();
                        }
                    }
                    }
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    try {
                        certs[0].checkValidity();
                    } catch (Exception e) {
                        try {
                            throw new CertificateException("Certificate not valid or trusted.");
                        } catch (CertificateException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession arg1) {
                    if (!hostname.equalsIgnoreCase("ec2-3-7-84-108.ap-south-1.compute.amazonaws.com")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        } catch (Exception ignored) {
            ignored.getMessage();
        }*/
    }
    public static void closeDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing()
                    && context != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void toPrettyFormat(String jsonString , int type)
    {
        try {
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(jsonString).getAsJsonObject();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(json);
            switch (type){
                case 0:
                    Log.e("Response:%n %s", prettyJson);
                    break;
                case 1:
                    Log.e("Request:%n %s", prettyJson);
                    break;
                default:
                    Log.e("Data:%n %s", prettyJson);
                    break;
            }
        } catch (JsonSyntaxException e) {
            Log.e("Data:%n %s", jsonString);
            e.printStackTrace();
        }
    }
    public static void generalAlert(Context context, String msg) {
        AlertDialog.Builder builder1;
        builder1 = new AlertDialog.Builder(context);
        builder1.setMessage(msg);
        builder1.setTitle("Alert");
        builder1.setCancelable(true);
        builder1.setNeutralButton("OK",
                (dialog, id) -> dialog.cancel());
        AlertDialog alert11 = builder1.create();
        alert11.show();

    }

    public static void requestQueueMultipart(Context context, VolleyMultipartRequest request) {
        RequestQueue requestQueue1 = Volley.newRequestQueue(context);
        int socketTimeout = 960*1000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        requestQueue1.add(request);
    }

    public static void requestQueue(Context context, StringRequest request) {
        RequestQueue requestQueue1 = Volley.newRequestQueue(context);
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        requestQueue1.add(request);
    }

    public static long timeInMills(String myDate) {
//        String myDate = "2014/10/29 18:10:45";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = sdf.parse(myDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();


    }

    public static Calendar dateToString(String strDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
    //endregion
}
