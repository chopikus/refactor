package com.chopikus.manager_app;


import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SupplyAddProductsAdapter extends RecyclerView.Adapter<SupplyAddProductsAdapter.MyViewHolder> {

    private ArrayList<SupplyProductsModel> dataSet;
    LayoutInflater inflater;

    public SupplyAddProductsAdapter(Context context, List<SupplyProductsModel> list) {
        inflater = LayoutInflater.from(context);
        dataSet = new ArrayList<>(list);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.supply_add_products_layout, parent, false);
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


        TextView textViewName,textViewId, textViewCount, textViewDate, textViewSupplier;
        LinearLayout rowFG;
        public MyViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewId = itemView.findViewById(R.id.textViewId);
            textViewCount = itemView.findViewById(R.id.textViewCount);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewSupplier = itemView.findViewById(R.id.textViewSupplier);
            rowFG = itemView.findViewById(R.id.rowFG);
        }

        public void bindData(SupplyProductsModel rowModel) {
            textViewName.setText(rowModel.name);
            textViewCount.setText(rowModel.getAmount()+" "+rowModel.getMeasurementUnit());
            textViewDate.setText("Срок годности: \n"+rowModel.getExpireDate());
            textViewId.setText(String.valueOf(rowModel.getId()));
            rowFG.setBackgroundColor(Color.WHITE);
        }
    }
}
