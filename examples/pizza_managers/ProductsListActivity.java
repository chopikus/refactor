package com.chopikus.manager_app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.nikhilpanju.recyclerviewenhanced.RecyclerTouchListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ProductsListActivity extends AppCompatActivity {
    class RequestTask extends AsyncTask<String, Void, String> {
        ProgressDialog dialog;

        ArrayList<SupplyModel> data = new ArrayList<SupplyModel>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(ProductsListActivity.this);
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
        setContentView(R.layout.activity_products_list);
        String json = getIntent().getStringExtra("suppliesJSON");
        String supplierId = getIntent().getStringExtra("supplierId");
        String cafeId = getIntent().getStringExtra("cafeId");
        try {
            JSONArray jsonArray = new JSONArray(json);
            Toast.makeText(this, jsonArray.length()+"", Toast.LENGTH_SHORT).show();
            final ArrayList<SupplyProductsModel> list = new ArrayList<>();
            for (int i=0; i<jsonArray.length(); i++)
            {
                JSONObject object = jsonArray.getJSONObject(i);
                String amountAndUnit = "";
                try {
                    amountAndUnit = object.getString("amount") + " " + object.getString("measurement_unit");
                }
                catch (JSONException ignored){};

                String expiryDate = "";
                try{
                    expiryDate = object.getString("expiry");
                }
                catch (JSONException ignored){};
                list.add(new SupplyProductsModel(object.getString("id"), supplierId, cafeId, object.getString("code"), object.getString("name"), amountAndUnit, expiryDate));
            }
            final SupplyProductsAdapter adapter = new SupplyProductsAdapter(this, list);
            final RecyclerView recyclerViewProducts = (RecyclerView) findViewById(R.id.recyclerViewProducts);
            recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewProducts.setItemAnimator(new DefaultItemAnimator());
            recyclerViewProducts.setAdapter(adapter);
            RecyclerTouchListener onTouchListener = new RecyclerTouchListener(ProductsListActivity.this, recyclerViewProducts);
            onTouchListener.setSwipeOptionViews( R.id.edit, R.id.setamount)
                    .setSwipeable(R.id.rowFG, R.id.rowBG, new RecyclerTouchListener.OnSwipeOptionsClickListener() {
                        @Override
                        public void onSwipeOptionClicked(int viewID, final int position) {
                            if (viewID==R.id.edit) {
                                String linkPart = "%7B%22id%22:" + list.get(position).getId() + ",%22amount%22:0%7D";
                                new RequestTask().execute("http://app.bizzarepizza.xyz/mng/supply/setamount?login=test_manager&token=3&data=" + linkPart);
                                list.get(position).setAmount("0");
                                SupplyProductsAdapter adapter = new SupplyProductsAdapter(ProductsListActivity.this, list);
                                recyclerViewProducts.setAdapter(adapter);
                            }
                            else
                            {

                                AlertDialog.Builder builder = new AlertDialog.Builder(ProductsListActivity.this);
                                builder.setTitle("Изменить количество продуктов...");
                                final View dialogView = getLayoutInflater().inflate(R.layout.number_picker_dialog, null);
                                builder.setView(dialogView);
                                final NumberPicker picker = dialogView.findViewById(R.id.numberPicker);
                                TextView textView = dialogView.findViewById(R.id.textView8);
                                textView.setText(list.get(position).getMeasurementUnit());
                                picker.setMinValue(0);
                                picker.setMaxValue(100);
                                builder.setPositiveButton("Изменить", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        String linkPart = "%7B%22id%22:" + list.get(position).getId() + ",%22amount%22:"+picker.getValue()+"%7D";
                                        new RequestTask().execute("http://app.bizzarepizza.xyz/mng/supply/setamount?login=test_manager&token=3&data=" + linkPart);
                                        list.get(position).setAmount(""+picker.getValue());
                                        SupplyProductsAdapter adapter = new SupplyProductsAdapter(ProductsListActivity.this, list);
                                        recyclerViewProducts.setAdapter(adapter);
                                    }
                                });
                                builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                                builder.show();

                            }

                        }
                    });
            recyclerViewProducts.addOnItemTouchListener(onTouchListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
