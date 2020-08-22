package com.chopikus.manager_app;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class Edit_cafe extends AppCompatActivity {

    class RequestTask extends AsyncTask<String, Void, String> {
        ProgressDialog dialog;

        ArrayList<SupplyModel> data = new ArrayList<SupplyModel>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(Edit_cafe.this);
            dialog.setTitle("Загрузка...");
            dialog.setMessage("Отправка запроса. Пожалуйста, подождите...");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(strings[0]).build();
            try {
                client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dialog.dismiss();
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cafe);
        String cafe_name = getIntent().getStringExtra("cafe_name");
        final int cafe_id = getIntent().getIntExtra("cafe_id", 0);
        String cafe_address = getIntent().getStringExtra("cafe_address");
        final EditText editText = (EditText) findViewById(R.id.editText);
        final EditText editText2 = (EditText) findViewById(R.id.editText2);
        editText.setText(cafe_name);
        editText2.setText(cafe_address);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject object = new JSONObject();
                    object.put("id", cafe_id);
                    object.put("name", editText.getText().toString());
                    object.put("address", editText2.getText().toString());

                    RequestTask task = new RequestTask();

                    task.execute("http://app.bizzarepizza.xyz/mng/cafe/edit?login=test_manager&token=3&data="+ URLEncoder.encode(object.toString(), "UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
