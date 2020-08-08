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
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.nikhilpanju.recyclerviewenhanced.RecyclerTouchListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DishAddProducts extends AppCompatActivity {

    ArrayList<DishProductsModel> arrayList = new ArrayList<>();

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
                //new DishProductsModel("name", "code", amount, unit);
                DishProductsModel model = new DishProductsModel(object.getString("name"), object.getString("code"), object.getInt("amount"), object.getString("measurement_unit"));
                arrayList.add(model);

            }
            DishAddProductsAdapter adapter = new DishAddProductsAdapter(this, arrayList);
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            recyclerView.setAdapter(adapter);
            if (arrayList.size()==0)
            {
                TextView textView7 = (TextView) findViewById(R.id.textView7);
                textView7.setVisibility(View.VISIBLE);
                textView7.setText("Список ингридентов пуст!");
            }
            else
            {
                TextView textView7 = (TextView) findViewById(R.id.textView7);
                textView7.setVisibility(View.GONE);
            }
            RecyclerTouchListener onTouchListener = new RecyclerTouchListener(DishAddProducts.this, recyclerView);

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
    class GetRequestTaskSecondDialog extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;

        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> codes = new ArrayList<>();
        ArrayList<String> measurementUnits = new ArrayList<>();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(DishAddProducts.this);
            dialog.setTitle("Загрузка...");
            dialog.setMessage("Отправка запроса. Пожалуйста, подождите...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            try {
                String url = "http://app.bizzarepizza.xyz/mng/foodstuff/list?login=test_manager&token=3";
                Request request = new Request.Builder().url(url).build();
                String responseString = client.newCall(request).execute().body().string();
                JSONObject object = new JSONObject(responseString);
                JSONArray array = object.getJSONArray("data");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object1 = array.getJSONObject(i);
                    names.add(object1.getString("name"));
                    codes.add(object1.getString("code"));
                    measurementUnits.add(object1.getString("measurement_unit"));
                }
                return null;
            }
            catch (Exception ignored){}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(DishAddProducts.this);
            builder.setTitle("Выберите продукт");
            final View view = getLayoutInflater()
                    .inflate(R.layout.supply_add_dish_dialog_first_step, null);
            builder.setView(view);
            final Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(DishAddProducts.this, android.R.layout.simple_spinner_item, names.toArray(new String[names.size()]));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            // заголовок
            builder.setPositiveButton("Далее", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialogAdd2Step(names.get(spinner.getSelectedItemPosition()), measurementUnits.get(spinner.getSelectedItemPosition()), codes.get(spinner.getSelectedItemPosition()));
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
                //dialogAdd2Step(result[0], result[1], result[2]);
        }
    }

    public void dialogAdd()
    {
        new GetRequestTaskSecondDialog().execute();

    }
    public void dialogAdd2Step(final String name, final String measurement_unit, final String code)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(DishAddProducts.this);
        final View view = getLayoutInflater()
                .inflate(R.layout.supply_add_product_dialog_second_step, null);
        builder.setView(view);
        builder.setTitle("Добавить продукт для приготовления блюда");
        TextView textViewName = (TextView) view.findViewById(R.id.textViewName);
        textViewName.setText(name);
        TextView textView = view.findViewById(R.id.textView);
        textView.setVisibility(View.GONE);
        TextView textViewMeasurementUnit = (TextView) view.findViewById(R.id.textViewMeasurementUnit);
        textViewMeasurementUnit.setText(measurement_unit);
        DatePicker datePicker = (DatePicker) view.findViewById(R.id.datePicker);
        datePicker.setVisibility(View.GONE);
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("Готово", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                EditText editTextAmount = (EditText) view.findViewById(R.id.editTextAmount);

                try {
                    SharedPreferences preferences = getSharedPreferences("add_product", Context.MODE_PRIVATE);
                    String productsJSON = preferences.getString("products", "[]");
                    JSONArray array = new JSONArray(productsJSON);
                    JSONObject object = new JSONObject();
                    object.put("code", code);
                    object.put("amount", editTextAmount.getText().toString());
                    object.put("name", name);
                    object.put("measurement_unit", measurement_unit);
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(DishAddProducts.this);
        final View view = getLayoutInflater()
                .inflate(R.layout.supply_add_product_dialog_second_step, null);
        builder.setView(view);
        builder.setTitle("Изменить продукт поставки");
        final TextView textViewName = (TextView) view.findViewById(R.id.textViewName);
        final EditText editTextAmount = (EditText) view.findViewById(R.id.editTextAmount);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.datePicker);
        final TextView textViewMeasurementUnit = (TextView) view.findViewById(R.id.textViewMeasurementUnit);
        datePicker.setVisibility(View.GONE);
        textViewName.setText(arrayList.get(position).getName());
        editTextAmount.setText(arrayList.get(position).getAmount()+"");
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
        setTitle("Изменение продуктов для приготовления блюда");
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
