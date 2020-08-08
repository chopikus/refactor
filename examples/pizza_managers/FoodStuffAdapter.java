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

public class FoodStuffAdapter extends RecyclerView.Adapter<FoodStuffAdapter.MyViewHolder> {

    private ArrayList<FoodStuffModel> dataSet;
    LayoutInflater inflater;

    public FoodStuffAdapter(Context context, List<FoodStuffModel> list) {
        inflater = LayoutInflater.from(context);
        dataSet = new ArrayList<>(list);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.foodstuff_layout, parent, false);
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
    class MyViewHolder extends RecyclerView.ViewHolder {


        TextView textViewName,textViewCode;
        LinearLayout rowFG;
        public MyViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewCode =  itemView.findViewById(R.id.textViewCode);
        }

        public void bindData(FoodStuffModel rowModel) {
            textViewName.setText(rowModel.getName());
            textViewCode.setText(rowModel.getCode());
        }
    }
}
