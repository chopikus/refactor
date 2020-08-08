package com.chopikus.manager_app;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class Add_supplier extends AppCompatActivity {
    class RequestTask extends AsyncTask<String, Void, String> {
        ProgressDialog dialog;

        ArrayList<SupplyModel> data = new ArrayList<SupplyModel>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(Add_supplier.this);
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
        setContentView(R.layout.activity_add_supplier);
        Button button = (Button) findViewById(R.id.button2);
        final EditText editTextName = (EditText) findViewById(R.id.editText3);
        final EditText editTextContractNumber = (EditText) findViewById(R.id.editText4);
        final EditText editTextPhone = (EditText) findViewById(R.id.editText5);
        final EditText editTextLink = (EditText) findViewById(R.id.editText6);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject object = new JSONObject();
                    object.put("name", editTextName.getText().toString());
                    object.put("phone_number", editTextPhone.getText().toString());
                    object.put("contract_number", editTextContractNumber.getText().toString());
                    object.put("contract_file", editTextLink.getText().toString());
                    RequestTask task = new RequestTask();
                    Toast.makeText(Add_supplier.this, "http://app.bizzarepizza.xyz/mng/shipper/add?login=test_manager&token=3&data="+URLEncoder.encode(object.toString(), "UTF-8"), Toast.LENGTH_SHORT).show();
                    task.execute("http://app.bizzarepizza.xyz/mng/shipper/add?login=test_manager&token=3&data="+URLEncoder.encode(object.toString(), "UTF-8"));

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
