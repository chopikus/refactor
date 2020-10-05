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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EditDishActivity extends AppCompatActivity {

    private String name,desc,category, price,amount,cooking_time;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dish);
        SharedPreferences preferences = getSharedPreferences("add_product", Context.MODE_PRIVATE);
         id = getIntent().getStringExtra("id");
        name = getIntent().getStringExtra("name");
        desc = getIntent().getStringExtra("desc");
        category = getIntent().getStringExtra("category");
        price = getIntent().getStringExtra("price");
        amount = getIntent().getStringExtra("amount");
        cooking_time = getIntent().getStringExtra("cooking_time");

        String productsJSON = getIntent().getStringExtra("productsJSON");
        preferences.edit().putString("products", productsJSON).apply();

        Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditDishActivity.this, DishAddProducts.class);
                startActivity(intent);
            }
        });
        Button button4 = findViewById(R.id.button4);
        button4.setText("Изменить блюдо");
        load();
    }
    public void load()
    {
        EditText editTextName = findViewById(R.id.editText7);
        EditText editTextDesc = findViewById(R.id.editText10);
        EditText editTextPrice = findViewById(R.id.editText11);
        EditText editTextAmount = findViewById(R.id.editText12);
        EditText editTextTime = findViewById(R.id.editText13);
        editTextName.setText(name);
        editTextDesc.setText(desc);
        editTextPrice.setText(price);
        editTextAmount.setText(amount);
        editTextTime.setText(cooking_time);
        TextView textViewProducts = (TextView) findViewById(R.id.textViewProducts);
        Spinner spinner = findViewById(R.id.spinner5);
        String[] measurements = {"г", "мл"};
        SharedPreferences preferences = getSharedPreferences("add_product", Context.MODE_PRIVATE);
        String productsJSON = preferences.getString("products", "[]");
        JSONArray array = null;
        try {
            array = new JSONArray(productsJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        textViewProducts.setText("Для приготовления блюда необходимо " + array.length()+" продукт(ов).");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, measurements);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        new CategoriesRequestTask().execute("http://app.bizzarepizza.xyz/mng/dishcategory/list?login=test_manager&token=3");
    }
    public void makeRequest(View view)
    {
        Spinner spinner = findViewById(R.id.spinner5);
        Spinner spinner1 = findViewById(R.id.spinner4);
        String[] measurements = {"г", "мл"};
        String measurement = (String) spinner.getSelectedItem();
        EditText editTextName = findViewById(R.id.editText7);
        EditText editTextDesc = findViewById(R.id.editText10);
        EditText editTextPrice = findViewById(R.id.editText11);
        EditText editTextAmount = findViewById(R.id.editText12);
        EditText editTextTime = findViewById(R.id.editText13);
        String name = editTextName.getText().toString();
        String desc = editTextDesc.getText().toString();
        String price = editTextPrice.getText().toString();
        String amount = editTextAmount.getText().toString();
        String minutes = editTextTime.getText().toString();
        JSONObject data = new JSONObject();
        try {
            data.put("id", id);
            data.put("name", name);
            data.put("description", desc);
            data.put("price", price);
            data.put("amount", amount);
            data.put("cooking_time", minutes);
            data.put("measurement_unit", spinner.getSelectedItem());
            data.put("category_name", spinner1.getSelectedItem());
            SharedPreferences preferences = getSharedPreferences("add_product", Context.MODE_PRIVATE);
            String productsJSON = preferences.getString("products", "[]");
            JSONArray array = new JSONArray(productsJSON);
            data.put("ingredients", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("REQUESTREQUEST", "http://app.bizzarepizza.xyz/mng/dish/edit?login=test_manager&token=3&data="+data.toString());
        new MakeRequestTask().execute("http://app.bizzarepizza.xyz/mng/dish/edit?login=test_manager&token=3&data="+data.toString());
    }
    class MakeRequestTask extends AsyncTask<String, Void, Void>
    {
        ProgressDialog dialog = new ProgressDialog(EditDishActivity.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Отправка запроса. Загрузка...");
        }

        @Override
        protected Void doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(strings[0])
                    .build();

            try {
                Response response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            finish();
        }
    }
    class CategoriesRequestTask extends AsyncTask<String, Void, Void>
    {
        ProgressDialog dialog = new ProgressDialog(EditDishActivity.this);
        ArrayList<String> list = new ArrayList<>();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Отправка запроса. Загрузка...");
        }

        @Override
        protected Void doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(strings[0])
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String responseString = response.body().string();
                JSONArray array = new JSONObject(responseString).getJSONArray("data");
                for (int i=0; i<array.length(); i++)
                    list.add(array.getJSONObject(i).getString("name"));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Spinner categories = findViewById(R.id.spinner4);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditDishActivity.this, android.R.layout.simple_spinner_item, list);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categories.setAdapter(adapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }
}
