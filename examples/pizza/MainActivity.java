package com.chopikus.bizzarepizzaoperator;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(0);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {

            super.setUserVisibleHint(
                    isVisibleToUser);

            // Refresh tab data:

            if (getFragmentManager() != null) {

                getFragmentManager()
                        .beginTransaction()
                        .detach(this)
                        .attach(this)
                        .commit();
            }
        }

        public void printDocument(String name, ArrayList<String> list)
        {
           Printer printer = new Printer(getContext(), name, list);
           printer.print();
        }
        String name="";
        ArrayList<String> list;
        class MakeReqestsTask extends AsyncTask<String, Void, Void>
        {
            String print="";
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(String... strings) {
                print = strings[1];
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
                if (print.equals("print"))
                {
                    printDocument(name, list);
                }
            }
        }
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }
        public void showFirstTabDialog(final int id, String name, ArrayList<String> printList)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Изменить статус заказа?");
            String s="";
            for (int i=0; i<printList.size(); i++)
                s+=printList.get(i)+"\n";
            builder.setMessage("Изменить статус заказа №"+id+"?\nСодержимое заказа:\n"+s);
            this.name = name;
            this.list = printList;
            builder.setNegativeButton("Отменить заказ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new MakeReqestsTask().execute("http://app.bizzarepizza.xyz/opr/maybeorder/delete?login=test_operator&token=3&data=%7B%22id%22:"+id+"%7D", "not_print");

                }
            });
            builder.setNeutralButton("Закрыть", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.setPositiveButton("Потвердить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new MakeReqestsTask().execute("http://app.bizzarepizza.xyz/opr/maybeorder/claim?login=test_operator&token=3&data=%7B%22id%22:"+id+"%7D", "print");
                }
            });
            builder.show();
        }
        public void showSecondTabDialog(final int id)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Изменить статус заказа?");
            builder.setMessage("Изменить статус заказа №"+id+" на приготовленный ?");
            builder.setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.setPositiveButton("Потвердить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new MakeReqestsTask().execute("http://app.bizzarepizza.xyz/opr/order/ready?login=test_operator&token=3&data=%7B%22id%22:"+id+"%7D", "not_print");
                }
            });
            builder.show();
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            final RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);
            final ProgressBar progressBar = rootView.findViewById(R.id.progressBar);
            final SwipeRefreshLayout layout = rootView.findViewById(R.id.refreshLayout);
            class MyTask extends AsyncTask<String, Void, Void> {
                ArrayList<OrderModel> data = new ArrayList<>();
                RecyclerView.Adapter adapter;
                String refreshing="";
                final Context context = getActivity();
                
								@Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    if (refreshing.equals("false"))
                        progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                protected Void doInBackground(String... strings) {
                    try {
                        refreshing = strings[2];
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url(strings[0])
                                .build();
                        Response response = client.newCall(request).execute();
                        JSONObject object = new JSONObject(response.body().string());
                        JSONArray array = object.getJSONArray("data");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object1 = array.getJSONObject(i);
                            String dishes = object1.getString("dishes");
                            ArrayList<String> list = new ArrayList<>(Arrays.asList(dishes.split(" ")));
                            ArrayList<String> printList = new ArrayList<>();
                            for (int j=0; j<list.size(); j++)
                            {
                                String a = list.get(j);
                                Request request1 = new Request.Builder()
                                        .url("http://app.bizzarepizza.xyz/opr/dish/info?login=test_operator&token=3&data=%7B%22id%22:"+a.split(":")[0]+"%7D")
                                        .build();
                                Response response1 = client.newCall(request1).execute();
                                String jsonResult = response1.body().string();
                                printList.add(new JSONObject(jsonResult).getJSONObject("data").getString("name")+" ("+a.split(":")[1]+" шт.)");
                            }
                            data.add(new OrderModel("Заказ №" + object1.getString("number"), object1.getInt("id"), object1.getString("address"), object1.getString("client_phone"), strings[1], printList));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    if (refreshing.equals("false"))
                        progressBar.setVisibility(View.GONE);
                    if (refreshing.equals("true"))
                    {
                        layout.setRefreshing(false);
                    }
                    OperatorAdapter adapter = new OperatorAdapter(data);
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(adapter);
                    RecyclerItemClickListener onItemTouchListener = new RecyclerItemClickListener(context, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            if (getArguments().getInt(ARG_SECTION_NUMBER)==1)
                            {
                                showFirstTabDialog(data.get(position).getId_(), data.get(position).name, data.get(position).getPrintList());
                            }
                            else
                                showSecondTabDialog(data.get(position).getId_());
                        }

                        @Override
                        public void onLongItemClick(View view, int position) {
                            // do whatever
                        }
                    });
                    recyclerView.addOnItemTouchListener(onItemTouchListener);

                }

            }
            layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (getArguments().getInt(ARG_SECTION_NUMBER)==1)
                        new MyTask().execute("http://app.bizzarepizza.xyz/opr/maybeorder/list?login=test_operator&token=3", "maybe", "true");
                    else
                        new MyTask().execute("http://app.bizzarepizza.xyz/opr/order/list?login=test_operator&token=3", "completed", "true");
                }
            });
            if (getArguments().getInt(ARG_SECTION_NUMBER)==1)
                new MyTask().execute("http://app.bizzarepizza.xyz/opr/maybeorder/list?login=test_operator&token=3", "maybe", "false");
            else
                new MyTask().execute("http://app.bizzarepizza.xyz/opr/order/list?login=test_operator&token=3", "completed", "false");
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }
}
