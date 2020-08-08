package com.chopikus.manager_app;


import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SupplyAdapter extends RecyclerView.Adapter<SupplyAdapter.MyViewHolder> {

    private ArrayList<SupplyModel> dataSet;
    Context context;
    LayoutInflater inflater;

    public SupplyAdapter(Context context, List<SupplyModel> list) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        dataSet = new ArrayList<>(list);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.supply_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.bindData(dataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
    class MyViewHolder extends RecyclerView.ViewHolder {

        
        TextView textViewName, textViewContractNumber, textViewSupplier;
        CardView cardView;
        public MyViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewContractNumber = itemView.findViewById(R.id.textViewContractNumber);
            textViewSupplier = itemView.findViewById(R.id.textViewSupplier);
            cardView = itemView.findViewById(R.id.card_view);
        }

        public void bindData(final SupplyModel rowModel) {
            textViewName.setText("Поставка\n"+rowModel.getName());
            textViewSupplier.setText(rowModel.getShipperName());
            textViewContractNumber.setText(rowModel.getCount() + " прод.");
        }
    }
}
