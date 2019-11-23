package fmt.febuweather;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import fmt.febuweather.helper.BasicFunctions;
import fmt.febuweather.helper.Menu;


public class ContactUs extends AppCompatActivity {


    String cu_email, cu_name, cu_subject, cu_message;

    EditText CU_NAME, CU_EMAIL, CU_SUBJECT, CU_MESSAGE;

    Button B_SEND_MESSAGE;

    ImageButton MENU_BUTTON, B_NAME_CANCEL, B_EMAIL_CANCEL, B_SUBJECT_CANCEL, B_MESSAGE_CANCEL;

    private BasicFunctions basicFunctions;

    private Menu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        menu = new Menu(ContactUs.this);

        MENU_BUTTON = findViewById(R.id.cu_menu);

        MENU_BUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.LeftDrawer.toggleLeftDrawer();

            }
        });

        basicFunctions = new BasicFunctions(ContactUs.this);

        CU_NAME = findViewById(R.id.cu_name);
        CU_EMAIL = findViewById(R.id.cu_email);
        CU_SUBJECT = findViewById(R.id.cu_subject);
        CU_MESSAGE = findViewById(R.id.cu_message);

        AdView mAdView = findViewById(R.id.cu_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        B_NAME_CANCEL = findViewById(R.id.cu_name_cancel);
        B_EMAIL_CANCEL = findViewById(R.id.cu_email_cancel);
        B_SUBJECT_CANCEL = findViewById(R.id.cu_subject_cancel);
        B_MESSAGE_CANCEL = findViewById(R.id.cu_message_cancel);

        B_SEND_MESSAGE = findViewById(R.id.cu_send_message);

        B_NAME_CANCEL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CU_NAME.setText("");

            }
        });

        B_EMAIL_CANCEL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CU_EMAIL.setText("");

            }
        });

        B_SUBJECT_CANCEL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CU_SUBJECT.setText("");

            }
        });

        B_MESSAGE_CANCEL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CU_MESSAGE.setText("");

            }
        });

        B_SEND_MESSAGE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cu_name = CU_NAME.getText().toString();

                cu_email = CU_EMAIL.getText().toString();

                cu_subject = CU_SUBJECT.getText().toString();

                cu_message = CU_MESSAGE.getText().toString();

                if (TextUtils.isEmpty(cu_name)) {
                    CU_NAME.setError("Type in your Name !");

                } else if (TextUtils.isEmpty(cu_email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(cu_email).matches()) {
                    CU_EMAIL.setError("Invalid Email-ID !");

                } else if (TextUtils.isEmpty(cu_subject)) {
                    CU_SUBJECT.setError("Type in your Subject !");

                } else if (TextUtils.isEmpty(cu_message)) {
                    CU_MESSAGE.setError("Type in your Message !");

                } else {

                    if(basicFunctions.isConnectingToInternet())
                       sendEmail(cu_name, cu_email, cu_subject, cu_message);

                    else {

                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){

                                    case DialogInterface.BUTTON_POSITIVE:

                                        if(basicFunctions.isConnectingToInternet())

                                            sendEmail(cu_name, cu_email, cu_subject, cu_message);

                                        else
                                            Toast.makeText(ContactUs.this,
                                                    "No Internet Connection. Try again later !",
                                                    Toast.LENGTH_LONG).show();

                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:

                                        Toast.makeText(ContactUs.this,
                                                "No Internet Connection. Try again later !",
                                                Toast.LENGTH_LONG).show();

                                        dialog.dismiss();

                                        break;

                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(ContactUs.this);
                        builder.setMessage("No Internet Connection. Try again ?")
                                .setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();

                    }

                }
            }
        });

    }


    private void sendEmail(String temp_name, String temp_email, String temp_subject, String temp_message) {

        String subject, message, message1, message2;

        subject = temp_name + " : " + temp_subject;

        message1 = "Application : FebWeather" + "\n\nName : " + temp_name + "\n\nEmail-Id : " + temp_email;
        message2 = "\n\nSubject : " + temp_subject + "\n\nMessage : " + temp_message;

        message = message1 + message2;

        SendingEmailBackgroundTask emailBackgroundTask = new SendingEmailBackgroundTask();

        emailBackgroundTask.execute(temp_email, subject, message);

    }


    @SuppressLint("StaticFieldLeak")
    private class SendingEmailBackgroundTask extends AsyncTask<String, Void, String> {

        private ProgressDialog cu_loading;

        @Override
        public void onPreExecute() {

            super.onPreExecute();
            cu_loading = new ProgressDialog(ContactUs.this);
            cu_loading.setMessage("Sending Email ... ");
            cu_loading.setIndeterminate(false);
            cu_loading.setCancelable(true);
            cu_loading.show();

        }

        @Override
        protected String doInBackground(String... params) {

            String email = params[0];
            String subject = params[1];
            String message = params[2];

            try {

                URL url = new URL(BasicFunctions.SEND_EMAIL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                OutputStream OS = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(OS, "UTF-8"));

                String data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8")
                        + "&" + URLEncoder.encode("subject", "UTF-8") + "=" + URLEncoder.encode(subject, "UTF-8")
                        + "&" + URLEncoder.encode("message", "UTF-8") + "=" + URLEncoder.encode(message, "UTF-8");

                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                OS.close();
                InputStream IS = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(IS, "iso-8859-1"));

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }

                bufferedReader.close();
                httpURLConnection.disconnect();
                IS.close();

                return response.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {

            switch (result) {

                case "Thank You for your Message, kindly wait until our Team responds to it !":

                    Toast.makeText(ContactUs.this, result, Toast.LENGTH_LONG).show();

                    CU_NAME.setText("");
                    CU_EMAIL.setText("");
                    CU_SUBJECT.setText("");
                    CU_MESSAGE.setText("");

                    cu_loading.dismiss();

                    break;

                case "Sending Email Failed !":

                    Toast.makeText(ContactUs.this, result, Toast.LENGTH_LONG).show();
                    cu_loading.dismiss();

                    break;

                default:

                    Toast.makeText(ContactUs.this, result, Toast.LENGTH_LONG).show();
                    cu_loading.dismiss();

                    break;

            }
        }
    }
}