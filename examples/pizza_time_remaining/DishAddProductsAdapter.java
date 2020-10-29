package com.chopikus.manager_app;


import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DishAddProductsAdapter extends RecyclerView.Adapter<DishAddProductsAdapter.MyViewHolder> {

    private ArrayList<DishProductsModel> dataSet;
    LayoutInflater inflater;

    public DishAddProductsAdapter(Context context, List<DishProductsModel> list) {
        inflater = LayoutInflater.from(context);
        dataSet = new ArrayList<>(list);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.dish_add_products_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bindData(dataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
    public int getTimeRemaining(String date1)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar sDate = Calendar.getInstance();
        try {
            sDate.setTime(sdf.parse(date1));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Get the represented date in milliseconds
        long milis1 = sDate.getTimeInMillis();
        long milis2 = System.currentTimeMillis();

        // Calculate difference in millisends
        long diff = milis1 - milis2;

        return (int)(diff / (24 * 60 * 60 * 1000));
    }
    class MyViewHolder extends RecyclerView.ViewHolder {


        private final TextView textViewAmount;
        private final TextView textViewName;

        public MyViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
        }

        public void bindData(DishProductsModel rowModel) {
            textViewName.setText(rowModel.getName());
            textViewAmount.setText(rowModel.getAmount()+" "+rowModel.getMeasurementUnit());
        }
    }
}
