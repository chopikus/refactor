package com.chopikus.manager_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Add_supply extends AppCompatActivity {
    public ArrayList<String> cafes, suppliers;
    public ArrayList<Integer> cafe_ids;
    public ArrayList<Integer> suppliers_ids;
    class RequestTask extends AsyncTask<String, Void, String> {
        ProgressDialog dialog;

        ArrayList<SupplyModel> data = new ArrayList<SupplyModel>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(Add_supply.this);
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

    class GetRequestTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;

        ArrayList<SupplyModel> data = new ArrayList<SupplyModel>();


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(Add_supply.this);
            dialog.setTitle("Загрузка...");
            dialog.setMessage("Получение запросов. Пожалуйста, подождите...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String[] strings = {"http://app.bizzarepizza.xyz/mng/cafe/list?login=test_manager&token=3", "http://app.bizzarepizza.xyz/mng/shipper/list?login=test_manager&token=3"};
            cafes.clear();
            suppliers.clear();
            cafe_ids.clear();
            suppliers_ids.clear();
            OkHttpClient client = new OkHttpClient();
            for (int i=0; i<strings.length; i++) {
                String responseString = null;
                try {
                    Request request = new Request.Builder().url(strings[i]).build();
                    responseString = client.newCall(request).execute().body().string();
                    if (i == 0) {
                        try {
                            JSONObject object = new JSONObject(responseString);
                            JSONArray jsonArray = object.getJSONArray("data");
                            for (int j = 0; j < jsonArray.length(); j++) {
                                cafes.add(jsonArray.getJSONObject(j).getString("name"));
                                cafe_ids.add(jsonArray.getJSONObject(j).getInt("id"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (i == 1) {
                        try {
                            JSONObject object = new JSONObject(responseString);
                            JSONArray jsonArray = object.getJSONArray("data");
                            for (int j = 0; j < jsonArray.length(); j++) {
                                suppliers.add(jsonArray.getJSONObject(j).getString("name"));
                                suppliers_ids.add(jsonArray.getJSONObject(j).getInt("id"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception ignored){};
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void Void1) {
            super.onPostExecute(Void1);
            dialog.dismiss();
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            Spinner spinner3 = (Spinner) findViewById(R.id.spinner3);
            ArrayAdapter adapter2 = new ArrayAdapter<String>(Add_supply.this, android.R.layout.simple_spinner_item, cafes.toArray(new String[cafes.size()]));
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(Add_supply.this, android.R.layout.simple_spinner_item, suppliers.toArray(new String[suppliers.size()]));
            adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner2.setAdapter(adapter2);
            spinner3.setAdapter(adapter3);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_supply);
        SharedPreferences preferences = getSharedPreferences("add_product", Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
        cafes = new ArrayList<>();
        cafe_ids = new ArrayList<>();
        suppliers = new ArrayList<>();
        suppliers_ids = new ArrayList<>();
        GetRequestTask task = new GetRequestTask();
        task.execute();
    }
    public void openActivity(View view)
    {
        Intent intent = new Intent(this, SupplyAddProducts.class);
        startActivity(intent);
    }
    public void loadSupplies()
    {
        try {
            TextView textViewSupplies = (TextView) findViewById(R.id.textViewSupplies);
            SharedPreferences preferences = getSharedPreferences("add_product", Context.MODE_PRIVATE);
            String productsJSON = preferences.getString("products", "[]");
            JSONArray array = new JSONArray(productsJSON);
            textViewSupplies.setText("Количество продуктов поставки: " + array.length()+" шт.");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void makeRequest(View view)
    {
        String data="";
        JSONObject object = new JSONObject();
        EditText editTextNumber = (EditText) findViewById(R.id.editTextNumber);

        SharedPreferences preferences = getSharedPreferences("add_product", Context.MODE_PRIVATE);
        String productsJSON = preferences.getString("products", "[]");
        Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
        Spinner spinner3 = (Spinner) findViewById(R.id.spinner3);
        try {
            object.put("number", editTextNumber.getText().toString());
            object.put("cafe_id", cafe_ids.get(spinner2.getSelectedItemPosition()));
            object.put("shipper_id", suppliers_ids.get(spinner3.getSelectedItemPosition()));
            JSONArray array = new JSONArray(productsJSON);
            object.put("supplies", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
        data = URLEncoder.encode(object.toString(), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.i("LINKLINK", "http://app.bizzarepizza.xyz/mng/invoice/add?login=test_manager&token=3&data="+data);
        new RequestTask().execute("http://app.bizzarepizza.xyz/mng/invoice/add?login=test_manager&token=3&data="+data);

    }
    @Override
    protected void onResume() {
        super.onResume();
        loadSupplies();

    }
}
