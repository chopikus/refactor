package com.chopikus.manager_app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nikhilpanju.recyclerviewenhanced.RecyclerTouchListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class SupplyAddProducts extends AppCompatActivity {

    ArrayList<SupplyProductsModel> arrayList = new ArrayList<>();

    public void loadList()
    {
        try {
            SharedPreferences preferences = getSharedPreferences("add_product", Context.MODE_PRIVATE);
            String productsJSON = preferences.getString("products", "[]");
            JSONArray array = new JSONArray(productsJSON);
            arrayList.clear();
            for (int i=0; i<array.length(); i++)
            {
                JSONObject object = array.getJSONObject(i);
                SupplyProductsModel model = new SupplyProductsModel(object.getString("code"), i + "", i + "", "", object.getString("name"), object.getString("amount"), object.getString("expiry"));
                model.setMeasurementUnit(object.getString("measurement_unit"));
                arrayList.add(model);

            }
            SupplyAddProductsAdapter adapter = new SupplyAddProductsAdapter(this, arrayList);
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            recyclerView.setAdapter(adapter);
            RecyclerTouchListener onTouchListener = new RecyclerTouchListener(SupplyAddProducts.this, recyclerView);
            if (arrayList.size()==0)
            {
                TextView textView7 = findViewById(R.id.textView7);
                textView7.setVisibility(View.VISIBLE);
            }
            else
            {
                TextView textView7 = findViewById(R.id.textView7);
                textView7.setVisibility(View.GONE);
            }
            onTouchListener.setSwipeOptionViews(R.id.edit, R.id.delete)
                    .setSwipeable(R.id.rowFG, R.id.rowBG, new RecyclerTouchListener.OnSwipeOptionsClickListener() {
                        @Override
                        public void onSwipeOptionClicked(int viewID, int position) {
                            if (viewID==R.id.edit) {
                                dialogEdit(position);
                            }
                            if (viewID==R.id.delete)
                            {
                                dialogDelete(position);
                            }
                        }
                    });
            recyclerView.addOnItemTouchListener(onTouchListener);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    class GetRequestTaskSecondDialog extends AsyncTask<String, Void, String[]> {
        ProgressDialog dialog;

        ArrayList<SupplyModel> data = new ArrayList<SupplyModel>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(SupplyAddProducts.this);
            dialog.setTitle("Загрузка...");
            dialog.setMessage("Отправка запроса. Пожалуйста, подождите...");
            dialog.show();
        }

        @Override
        protected String[] doInBackground(String... strings) {
            try{
                OkHttpClient client = new OkHttpClient();
                String dataString = URLEncoder.encode("{\"code\":\""+strings[0]+"\"}", "UTF-8");
                String url = "http://app.bizzarepizza.xyz/mng/foodstuff/info?login=test_manager&token=3&data="+dataString;
                Request request = new Request.Builder().url(url).build();
                String responseString = client.newCall(request).execute().body().string();
                JSONObject object = new JSONObject(responseString);
                    String[] result = new String[3];
                    result[0] = object.getJSONObject("data").getString("name");
                    result[1] = object.getJSONObject("data").getString("measurement_unit");
                    result[2] = strings[0];
                    return result;
            }
            catch (Exception ignored){}

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            if (result==null)
            {
                Toast.makeText(SupplyAddProducts.this, "Введен неверный код товара!", Toast.LENGTH_SHORT).show();
            }
            else
                dialogAdd2Step(result[0], result[1], result[2]);
            dialog.dismiss();
        }
    }

    public void dialogAdd()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(SupplyAddProducts.this);
        builder.setTitle("Для добавление введите код продукта");
        final View view = getLayoutInflater()
                .inflate(R.layout.supply_add_product_dialog_first_step, null);
        builder.setView(view);
        builder.setPositiveButton("Далее", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editText = (EditText) view.findViewById(R.id.editText8);
                new GetRequestTaskSecondDialog().execute(editText.getText().toString());
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    public void dialogAdd2Step(final String name, final String measurement_unit, final String code)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(SupplyAddProducts.this);
        final View view = getLayoutInflater()
                .inflate(R.layout.supply_add_product_dialog_second_step, null);
        builder.setView(view);
        builder.setTitle("Добавить продукт поставки");
        TextView textViewName = (TextView) view.findViewById(R.id.textViewName);
        textViewName.setText(name);
        TextView textViewMeasurementUnit = (TextView) view.findViewById(R.id.textViewMeasurementUnit);
        textViewMeasurementUnit.setText(measurement_unit);
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("Готово", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                EditText editTextAmount = (EditText) view.findViewById(R.id.editTextAmount);
                DatePicker datePicker = (DatePicker) view.findViewById(R.id.datePicker);
                try {
                    SharedPreferences preferences = getSharedPreferences("add_product", Context.MODE_PRIVATE);
                    String productsJSON = preferences.getString("products", "[]");
                    JSONArray array = new JSONArray(productsJSON);
                    JSONObject object = new JSONObject();
                    object.put("code", code);
                    object.put("amount", editTextAmount.getText().toString());
                    object.put("name", name);
                    object.put("measurement_unit", measurement_unit);
                    Calendar cal = Calendar.getInstance();
                    cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    object.put("expiry", format.format(cal.getTime()));
                    array.put(object);
                    preferences.edit().putString("products", array.toString()).apply();
                    loadList();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.show();
    }
    public void dialogEdit(final int position)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(SupplyAddProducts.this);
        final View view = getLayoutInflater()
                .inflate(R.layout.supply_add_product_dialog_second_step, null);
        builder.setView(view);
        builder.setTitle("Изменить продукт поставки");
        final TextView textViewName = (TextView) view.findViewById(R.id.textViewName);
        final EditText editTextAmount = (EditText) view.findViewById(R.id.editTextAmount);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.datePicker);
        final TextView textViewMeasurementUnit = (TextView) view.findViewById(R.id.textViewMeasurementUnit);
        String date = arrayList.get(position).getExpireDate();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            calendar.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        textViewName.setText(arrayList.get(position).getName());
        editTextAmount.setText(arrayList.get(position).getAmount());
        textViewMeasurementUnit.setText(arrayList.get(position).getMeasurementUnit());
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("Готово", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    SharedPreferences preferences = getSharedPreferences("add_product", Context.MODE_PRIVATE);
                    String productsJSON = preferences.getString("products", "[]");
                    JSONArray array = new JSONArray(productsJSON);
                    JSONObject object = array.getJSONObject(position);
                    object.put("amount", editTextAmount.getText().toString());
                    Calendar cal = Calendar.getInstance();
                    cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    object.put("expiry", format.format(cal.getTime()));
                    preferences.edit().putString("products", array.toString()).apply();
                    loadList();
                }
                catch (Exception e)
                {

                }
            }
        });
        builder.show();
    }
    public void dialogDelete(final int position)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Вы уверены, что хотите удалить данный продукт?");
        builder.setMessage("После отменить эту операцию будет невозможно.");
        builder.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    SharedPreferences preferences = getSharedPreferences("add_product", Context.MODE_PRIVATE);
                    String productsJSON = preferences.getString("products", "[]");
                    JSONArray array = new JSONArray(productsJSON);
                    array.remove(position);
                    preferences.edit().putString("products", array.toString()).apply();
                    loadList();
                }
                catch (Exception e)
                {}
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supply_add_products);
        setTitle("Изменение продуктов поставки");
        loadList();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.floatingActionButton2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAdd();
            }
        });
    }
}
