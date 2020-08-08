package com.chopikus.manager_app;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.nikhilpanju.recyclerviewenhanced.RecyclerTouchListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private RecyclerTouchListener onTouchListener;
    private RecyclerView recyclerView, recyclerView2, recyclerView3, recyclerView4, recyclerView5;
    //1 - cafe, 2 - suppliers, 3 - supplies, 4 - products, 5 - statistics
    private BottomNavigationView navigation;
    private MenuItem item_add;
    private TextView textViewDate;
    private TextView textViewCafe;

    private Boolean started;
    private RecyclerView.OnItemTouchListener onItemTouchListener;

    public void startCircle() {
        recyclerView.setVisibility(View.GONE);
        recyclerView2.setVisibility(View.GONE);
        recyclerView3.setVisibility(View.GONE);
        recyclerView4.setVisibility(View.GONE);
        recyclerView5.setVisibility(View.GONE);
        ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar);
        bar.setVisibility(View.VISIBLE);

    }

    public void endCircle() {
        ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar);
        bar.setVisibility(View.GONE);
    }


    public void loadDishes() {

        recyclerView5.setHasFixedSize(true);

        final ArrayList<DataModel> data = new ArrayList<DataModel>();
        final Context context = this;
        class MyTask extends AsyncTask<Void, Void, Void> {
            ProgressDialog dialog;
            private Response response;
            private String result;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                startCircle();
            }

            @Override
            protected Void doInBackground(Void... params) {
                OkHttpClient okHttpClient = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("http://app.bizzarepizza.xyz/mng/dish/list?login=test_manager&token=3")
                        .build();
                try {
                    JSONObject object = new JSONObject(okHttpClient.newCall(request).execute().body().string());
                    JSONArray array = object.getJSONArray("data");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object1 = array.getJSONObject(i);
                        DataModel model = new DataModel(object1.getString("name"), object1.getDouble("price "), object1.getInt("id"), object1.getString("photo"), object1.getInt("amount"), object1.getString("measurement_unit"), object1.getString("cooking_time"), object1.getJSONArray("ingredients").toString());
                        model.setDescription(object1.getString("description"));
                        model.setCategory(object1.getString("category_name"));
                        data.add(model);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                endCircle();
                CustomAdapter adapter = new CustomAdapter(data);
                recyclerView5.setVisibility(View.VISIBLE);
                recyclerView5.setAdapter(adapter);
                recyclerView5.removeOnItemTouchListener(onTouchListener);
                onTouchListener = new RecyclerTouchListener(MainActivity.this, recyclerView5);
                onTouchListener.setSwipeOptionViews(R.id.edit)
                        .setSwipeable(R.id.rowFG, R.id.rowBG, new RecyclerTouchListener.OnSwipeOptionsClickListener() {
                            @Override
                            public void onSwipeOptionClicked(int viewID, int position) {
                                if (viewID == R.id.edit) {
                                    Intent intent = new Intent(MainActivity.this, EditDishActivity.class);
                                    intent.putExtra("id", data.get(position).getId_() + "");
                                    intent.putExtra("name", data.get(position).getName());
                                    intent.putExtra("desc", data.get(position).getDescription());
                                    intent.putExtra("category", data.get(position).getCategory());
                                    intent.putExtra("price", data.get(position).getPrice() + "");
                                    intent.putExtra("amount", data.get(position).getAmount() + "");
                                    intent.putExtra("cooking_time", data.get(position).getMinutes() + "");
                                    intent.putExtra("productsJSON", data.get(position).getProductsJSON());
                                    startActivity(intent);
                                }
                            }
                        });

                recyclerView5.addOnItemTouchListener(onTouchListener);
            }

        }
        new MyTask().execute();

    }

    class SimpleRequestTask extends AsyncTask<String, Void, String> {
        ProgressDialog dialog;

        ArrayList<SupplyModel> data = new ArrayList<SupplyModel>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setTitle("Загрузка...");
            dialog.setMessage("Отправка запроса. Пожалуйста, подождите...");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(strings[0])
                    .build();
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
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FloatingActionButton button = (FloatingActionButton) findViewById(R.id.floatingActionButton);
            button.show();
            textViewDate.setText("Bizzare Pizza");
            textViewCafe.setText("");
            textViewDate.setOnClickListener(null);
            textViewCafe.setOnClickListener(null);
            recyclerView.setVisibility(View.GONE);
            recyclerView2.setVisibility(View.GONE);
            recyclerView3.setVisibility(View.GONE);
            recyclerView4.setVisibility(View.GONE);
            recyclerView5.setVisibility(View.GONE);

            switch (item.getItemId()) {

                case R.id.navigation_cafe:
                    recyclerView.setVisibility(View.VISIBLE);
                    //item_add.setVisible(true);
                    loadCafe();
                    return true;
                case R.id.navigation_suppliers:
                    recyclerView2.setVisibility(View.VISIBLE);
                    //item_add.setVisible(true);
                    loadShippers();
                    return true;
                case R.id.navigation_supplies:
                    recyclerView3.setVisibility(View.VISIBLE);
                    textViewDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            changeDate();
                        }
                    });
                    textViewCafe.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            changeCafe();
                        }
                    });
                    textViewCafe.setVisibility(View.VISIBLE);
                    textViewDate.setVisibility(View.VISIBLE);
                    loadSupplies();
                    return true;
                case R.id.navigation_supplies_products:
                    recyclerView4.setVisibility(View.VISIBLE);
                    //item_add.setVisible(false);
                    loadProducts();
                    return true;
                case R.id.navigation_menu:
                    recyclerView5.setVisibility(View.VISIBLE);
                    loadDishes();
                    return true;
            }
            return false;
        }
    };

    public void loadSupplies() {
        class SuppliesGetRequestTask extends AsyncTask<String, Void, String> {
            ArrayList<SupplyModel> data = new ArrayList<SupplyModel>();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                startCircle();
            }

            @Override
            protected String doInBackground(String... strings) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                OkHttpClient client = new OkHttpClient();
                try {
                    Request request = new Request.Builder().url(strings[0]).build();
                    String responseString = client.newCall(request).execute().body().string();
                    JSONObject object = new JSONObject(responseString);
                    JSONArray array = object.getJSONArray("data");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        data.add(new SupplyModel(obj.getString("shipper_name"), obj.getString("shipper_id"), obj.getString("cafe_name"), obj.getString("cafe_id"), obj.getString("number"), obj.getJSONArray("supplies")));
                    }
                    return responseString;
                } catch (Exception ignored) {
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                endCircle();
                SharedPreferences preferences = getSharedPreferences("dates_cafes", Context.MODE_PRIVATE);
                textViewDate.setText(preferences.getString("friendly_date", "Все даты"));
                textViewCafe.setText(preferences.getString("friendly_cafe", "Все кафе"));
                SupplyAdapter adapter = new SupplyAdapter(MainActivity.this, data);
                recyclerView3.setVisibility(View.VISIBLE);
                recyclerView3.setAdapter(adapter);
                recyclerView3.removeOnItemTouchListener(onItemTouchListener);
                onItemTouchListener = new RecyclerItemClickListener(MainActivity.this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(MainActivity.this, ProductsListActivity.class);
                        intent.putExtra("suppliesJSON", data.get(position).supplies.toString());
                        intent.putExtra("supplierId", data.get(position).getShipperId());
                        intent.putExtra("cafeId", data.get(position).getCafeId());
                        intent.putExtra("supplierName", data.get(position).getShipperName());
                        startActivity(intent);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                });
                recyclerView3.addOnItemTouchListener(onItemTouchListener);
            }
        }
        SuppliesGetRequestTask task = new SuppliesGetRequestTask();
        String link = "http://app.bizzarepizza.xyz/mng/invoice/list?login=test_manager&token=3";
        SharedPreferences preferences = getSharedPreferences("dates_cafes", Context.MODE_PRIVATE);
        if (preferences.getInt("cafe_id", -1) != -1) {
            link += "&cafe_id=" + preferences.getInt("cafe_id", -1);
        }
        if (!preferences.getString("start_date", "all").equals("all")) {
            link += "&date_start=" + preferences.getString("start_date", "");
            link += "&date_finish=" + preferences.getString("end_date", "");
        }
        task.execute(link);

    }

    public void loadShippers() {
        class SuppliesGetRequestTask extends AsyncTask<String, Void, String> {

            ArrayList<SupplierModel> data = new ArrayList<SupplierModel>();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                startCircle();
            }

            @Override
            protected String doInBackground(String... strings) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(strings[0]).build();
                try {
                    return client.newCall(request).execute().body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                recyclerView2.invalidate();
                endCircle();
                try {
                    JSONObject reader = new JSONObject(result);
                    JSONArray array = reader.getJSONArray("data");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        String phoneNumber = "+380967188217", contractLink = "";
                        try {
                            phoneNumber = object.getString("phone");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            contractLink = object.getString("contract_file");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        data.add(new SupplierModel(object.getString("name"), object.getString("contract_number"), phoneNumber, i + "", contractLink));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                recyclerView2.setVisibility(View.VISIBLE);
                SupplierAdapter adapter = new SupplierAdapter(MainActivity.this, data);
                recyclerView2.setAdapter(adapter);
                onTouchListener = new RecyclerTouchListener(MainActivity.this, recyclerView2);

                onTouchListener.setSwipeOptionViews(R.id.make_phone_call)
                        .setSwipeable(R.id.rowFG, R.id.rowBG, new RecyclerTouchListener.OnSwipeOptionsClickListener() {
                            @Override
                            public void onSwipeOptionClicked(int viewID, int position) {
                                if (viewID == R.id.make_phone_call) {
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:" + data.get(position).phoneNumber));
                                    startActivity(intent);
                                }
                            }
                        });
                onTouchListener.setIndependentViews(R.id.rowFG).setClickable(new RecyclerTouchListener.OnRowClickListener() {
                    @Override
                    public void onRowClicked(final int position) {

                    }

                    @Override
                    public void onIndependentViewClicked(int independentViewID, final int position) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Скачать договор?");
                        builder.setMessage("Скачать договор с поставщиком? Это может занимать память на устройстве.");
                        builder.setPositiveButton("Скачать", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String contract_link = data.get(position).contractLink;
                                String url = contract_link;
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                                request.setDescription("Скачивание договора...");
                                request.setTitle("Скачивание...");
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    request.allowScanningByMediaScanner();
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                }
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "contract.pdf");

                                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                manager.enqueue(request);
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
                });

                recyclerView2.addOnItemTouchListener(onTouchListener);

            }
        }

        SuppliesGetRequestTask task = new SuppliesGetRequestTask();
        task.execute("http://app.bizzarepizza.xyz/mng/shipper/list?login=test_manager&token=3");
    }

    public void changeDate() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите даты");
        final View view = getLayoutInflater()
                .inflate(R.layout.dates_dialog, null);
        builder.setView(view);
        final DatePicker pickerStart = view.findViewById(R.id.datePickerStart);
        final DatePicker pickerEnd = view.findViewById(R.id.datePickerEnd);
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        final SharedPreferences sharedPreferences = getSharedPreferences("dates_cafes", Context.MODE_PRIVATE);
        Calendar calendarStart = Calendar.getInstance();
        Calendar calendarEnd = Calendar.getInstance();
        try {
            calendarStart.setTime(format.parse(sharedPreferences.getString("start_date", "")));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            calendarEnd.setTime(format.parse(sharedPreferences.getString("end_date", "")));
        } catch (ParseException e) {
            e.printStackTrace();
        }


        pickerStart.updateDate(calendarStart.get(Calendar.YEAR), calendarStart.get(Calendar.MONTH), calendarStart.get(Calendar.DAY_OF_MONTH));
        pickerEnd.updateDate(calendarEnd.get(Calendar.YEAR), calendarEnd.get(Calendar.MONTH), calendarEnd.get(Calendar.DAY_OF_MONTH));
        builder.setNeutralButton("Все даты", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("start_date", "all");
                editor.putString("end_date", "all");
                editor.putString("friendly_date", "Все даты");
                editor.apply();
                loadSupplies();
            }
        });
        builder.setPositiveButton("Выбрать", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = sharedPreferences.edit();

                int dayStart = pickerStart.getDayOfMonth();
                int monthStart = pickerStart.getMonth();
                int yearStart = pickerStart.getYear();
                int dayEnd = pickerEnd.getDayOfMonth();
                int monthEnd = pickerEnd.getMonth();
                int yearEnd = pickerEnd.getYear();
                Calendar calendarStart = Calendar.getInstance();
                calendarStart.set(yearStart, monthStart, dayStart);
                Calendar calendarEnd = Calendar.getInstance();
                calendarEnd.set(yearEnd, monthEnd, dayEnd);

                editor.putString("start_date", format.format(calendarStart.getTime()));
                editor.putString("end_date", format.format(calendarEnd.getTime()));
                editor.putString("friendly_date", format.format(calendarStart.getTime()) + "\n" + format.format(calendarEnd.getTime()));
                editor.apply();
                loadSupplies();
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

    public void changeCafe() {
        class RequestTask extends AsyncTask<String, Void, String> {
            ProgressDialog dialog;
            ArrayList<String> cafesArr = new ArrayList<>();
            ArrayList<Integer> ids = new ArrayList<>();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setTitle("Загрузка...");
                dialog.setMessage("Отправка запроса. Пожалуйста, подождите...");
                dialog.show();
            }

            @Override
            protected String doInBackground(String... strings) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(strings[0]).build();
                    String responseString = client.newCall(request).execute().body().string();
                    JSONObject reader = new JSONObject(responseString);
                    JSONArray array = reader.getJSONArray("data");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        cafesArr.add(object.getString("name"));
                        ids.add(object.getInt("id"));
                    }
                    return responseString;
                } catch (Exception ignored){}

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                dialog.dismiss();
                final int[] checked = {0};
                final SharedPreferences preferences = getSharedPreferences("dates_cafes", Context.MODE_PRIVATE);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Выберите кафе")
                        .setCancelable(false)
                        .setSingleChoiceItems(cafesArr.toArray(new String[cafesArr.size()]), 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checked[0] = which;
                            }
                        });
                builder.setCancelable(true);
                builder.setNeutralButton("Выбрать все кафе", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("friendly_cafe", "Все кафе");
                        editor.putInt("cafe_id", -1);
                        editor.apply();
                        loadSupplies();
                    }
                });
                builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("Выбрать", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("friendly_cafe", cafesArr.get(checked[0]));
                        editor.putInt("cafe_id", ids.get(checked[0]));
                        editor.apply();
                        loadSupplies();
                    }
                });
                builder.show();
            }
        }
        new RequestTask().execute("http://app.bizzarepizza.xyz/mng/cafe/list?login=test_manager&token=3");

    }

    public void loadProducts() {
        class ProductsGetRequestTask extends AsyncTask<String, Void, String> {

            ArrayList<FoodStuffModel> data = new ArrayList<>();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                startCircle();
            }

            @Override
            protected String doInBackground(String... strings) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(strings[0]).build();
                try {
                    return client.newCall(request).execute().body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                try {
                    JSONObject object = new JSONObject(result);
                    JSONArray array = object.getJSONArray("data");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object1 = array.getJSONObject(i);
                        data.add(new FoodStuffModel(object1.getString("id"), object1.getString("name"), object1.getString("code"), object1.getString("measurement_unit"), object1.getString("category")));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                FoodStuffAdapter adapter = new FoodStuffAdapter(MainActivity.this, data);
                endCircle();
                recyclerView4.setVisibility(View.VISIBLE);
                recyclerView4.setAdapter(adapter);
                recyclerView4.removeOnItemTouchListener(onTouchListener);
                onTouchListener = new RecyclerTouchListener(MainActivity.this, recyclerView4);
                onTouchListener.setSwipeOptionViews(R.id.edit)
                        .setSwipeable(R.id.rowFG, R.id.rowBG, new RecyclerTouchListener.OnSwipeOptionsClickListener() {
                            @Override
                            public void onSwipeOptionClicked(int viewID, int position) {
                                if (viewID == R.id.edit) {
                                    editFoodStuffDialog(data.get(position).getName(), data.get(position).getCode(), data.get(position).getMeasurement(), data.get(position).getId(), data.get(position).getCategoryName());
                                }
                            }
                        });
                recyclerView4.addOnItemTouchListener(onTouchListener);

            }
        }
        ProductsGetRequestTask task = new ProductsGetRequestTask();
        SharedPreferences preferences = getSharedPreferences("dates_cafes", Context.MODE_PRIVATE);
        String link = "http://app.bizzarepizza.xyz/mng/foodstuff/list?login=test_manager&token=3";
        task.execute(link);

    }

    public void loadCafe() {

        class CafeGetRequestTask extends AsyncTask<String, Void, String> {
            ArrayList<CafeModel> data = new ArrayList<CafeModel>();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                startCircle();
            }

            @Override
            protected String doInBackground(String... strings) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(strings[0]).build();
                try {
                    return client.newCall(request).execute().body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                endCircle();
                try {
                    JSONObject reader = new JSONObject(result);

                    Bitmap icon = BitmapFactory.decodeResource(getResources(),
                            R.drawable.restaurant);
                    data.clear();
                    JSONArray array = reader.getJSONArray("data");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        data.add(new CafeModel(object.getString("name"), object.getString("address"), object.getInt("id"), icon));
                    }
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.removeOnItemTouchListener(onTouchListener);
                    onTouchListener = new RecyclerTouchListener(MainActivity.this, recyclerView);
                    CafeAdapter adapter = new CafeAdapter(MainActivity.this, data);
                    recyclerView.setAdapter(adapter);
                    onTouchListener.setSwipeOptionViews(R.id.edit)
                            .setSwipeable(R.id.rowFG, R.id.rowBG, new RecyclerTouchListener.OnSwipeOptionsClickListener() {
                                @Override
                                public void onSwipeOptionClicked(int viewID, int position) {
                                    if (viewID == R.id.edit) {
                                        Intent intent = new Intent(MainActivity.this, Edit_cafe.class);
                                        intent.putExtra("cafe_id", data.get(position).id_);
                                        intent.putExtra("cafe_name", data.get(position).name);
                                        intent.putExtra("cafe_address", data.get(position).address);
                                        startActivity(intent);
                                    }
                                }
                            });

                    recyclerView.addOnItemTouchListener(onTouchListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        CafeGetRequestTask task = new CafeGetRequestTask();
        task.execute("http://app.bizzarepizza.xyz/mng/cafe/list?login=test_manager&token=3");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);

    }

    public void addButtonClick(View view) {
        int selectedItemId = navigation.getSelectedItemId();
        if (selectedItemId == R.id.navigation_cafe) {
            Intent intent = new Intent(MainActivity.this, Add_cafe.class);
            startActivity(intent);
        } else if (selectedItemId == R.id.navigation_suppliers) {
            Intent intent = new Intent(MainActivity.this, Add_supplier.class);
            startActivity(intent);
        } else if (selectedItemId == R.id.navigation_supplies) {
            Intent intent = new Intent(MainActivity.this, Add_supply.class);
            startActivity(intent);
        } else if (selectedItemId == R.id.navigation_supplies_products) {
            addFoodStuffDialog();
        } else if (selectedItemId == R.id.navigation_menu) {
            Intent intent = new Intent(MainActivity.this, AddDishActivity.class);
            startActivity(intent);
        }
    }

    void editFoodStuffDialog(final String name, final String code, String measurement, final String id, String foodstuffCategory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменить продукт");
        View dialogView = getLayoutInflater().inflate(R.layout.add_foodstuff_dialog, null);
        builder.setView(dialogView);
        final EditText editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
        editTextName.setText(name);
        final EditText editTextCode = (EditText) dialogView.findViewById(R.id.editTextCode);
        editTextCode.setText(code);
        final Spinner spinnerMeasurement = (Spinner) dialogView.findViewById(R.id.spinnerMeasurement);
        final String[] measurements = {"г", "л", "шт", "упак"};
        final String[] foodstuffcategories = {"Овощи", "Фрукты", "Грибы", "Зелень", "Зерновые", "Молочные продукты", "Мука, мучные изделия", "Сыр", "Мясо и мясная продукция", "Напитки", "Орехи", "Остальное"};
        ArrayAdapter<String> adapterCategories = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, foodstuffcategories);
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner spinner6 = dialogView.findViewById(R.id.spinner6);
        spinner6.setAdapter(adapterCategories);
        int chosen = 0;
        for (int i = 0; i < measurements.length; i++) {
            if (measurements[i].equals(measurement)) {
                chosen = i;
                break;
            }
        }
        int chosen2 = 0;
        for (int i = 0; i < foodstuffcategories.length; i++) {

            if (foodstuffcategories[i].equals(foodstuffCategory)) {
                chosen2 = i;
                break;
            }
        }
        Log.d("LOGLOGLOG", "" + chosen2);
        Log.d("LOGLOGLOG", foodstuffCategory);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, measurements);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMeasurement.setAdapter(adapter);
        spinnerMeasurement.setSelection(chosen);
        spinner6.setSelection(chosen2);
        builder.setPositiveButton("Готово", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                JSONObject object = new JSONObject();
                try {
                    object.put("id", id);
                    object.put("code", editTextCode.getText().toString());
                    object.put("name", editTextName.getText().toString());
                    object.put("measurement_unit", spinnerMeasurement.getSelectedItem());
                    object.put("category_name", spinner6.getSelectedItem());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new SimpleRequestTask().execute("http://app.bizzarepizza.xyz/mng/foodstuff/edit?login=test_manager&token=3&data=" + object.toString());
                loadProducts();

            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();

    }

    void addFoodStuffDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить новый продукт");
        View dialogView = getLayoutInflater().inflate(R.layout.add_foodstuff_dialog, null);
        builder.setView(dialogView);
        final EditText editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
        final EditText editTextCode = (EditText) dialogView.findViewById(R.id.editTextCode);
        final Spinner spinnerMeasurement = (Spinner) dialogView.findViewById(R.id.spinnerMeasurement);
        final String[] measurements = {"г", "л", "шт", "упак"};
        final String[] foodstuffcategories = {"Овощи", "Фрукты", "Грибы", "Зелень", "Зерновые", "Молочные продукты", "Мука, мучные изделия", "Сыр", "Мясо и мясная продукция", "Напитки", "Орехи", "Остальное"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, measurements);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMeasurement.setAdapter(adapter);
        ArrayAdapter<String> adapterCategories = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, foodstuffcategories);
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner spinner6 = dialogView.findViewById(R.id.spinner6);
        spinner6.setAdapter(adapterCategories);
        builder.setPositiveButton("Готово", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String data = "%7B%22description%22:1,%22code%22:%22" + editTextCode.getText().toString() + "%22,%22name%22:%22" + editTextName.getText().toString() + "%22,%22measurement_unit%22:%22" + spinnerMeasurement.getSelectedItem().toString() + "%22, %22category_name%22:%22" + spinner6.getSelectedItem() + "%22%7D";
                new SimpleRequestTask().execute("http://app.bizzarepizza.xyz/mng/foodstuff/add?login=test_manager&token=3&data=" + data);
                loadProducts();
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.custom_action_bar, null);
        textViewDate = (TextView) mCustomView.findViewById(R.id.textViewDate);
        textViewCafe = (TextView) mCustomView.findViewById(R.id.textViewCafe);
        textViewDate.setText("Bizzare Pizza");
        textViewCafe.setText("");
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView2 = (RecyclerView) findViewById(R.id.my_recycler_view_2);
        recyclerView3 = (RecyclerView) findViewById(R.id.my_recycler_view_3);
        recyclerView4 = (RecyclerView) findViewById(R.id.my_recycler_view_4);
        recyclerView5 = (RecyclerView) findViewById(R.id.my_recycler_view_5);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2.setItemAnimator(new DefaultItemAnimator());
        recyclerView3.setLayoutManager(new LinearLayoutManager(this));
        recyclerView3.setItemAnimator(new DefaultItemAnimator());
        recyclerView4.setLayoutManager(new LinearLayoutManager(this));
        recyclerView4.setItemAnimator(new DefaultItemAnimator());
        recyclerView5.setLayoutManager(new LinearLayoutManager(this));
        recyclerView5.setItemAnimator(new DefaultItemAnimator());

    }

    protected void onResume() {
        super.onResume();
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(navigation.getSelectedItemId());
    }
}
