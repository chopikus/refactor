package com.chopikus.bizzarepizzaoperator;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class OperatorAdapter extends RecyclerView.Adapter<OperatorAdapter.MyViewHolder> {

    private ArrayList<OrderModel> dataSet;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewId;
        TextView textViewName, textViewPhone, textViewAddress;
        ImageView imageView;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            this.textViewAddress = (TextView) itemView.findViewById(R.id.textViewAddress);
            this.textViewPhone = (TextView) itemView.findViewById(R.id.textViewPhone);
            this.textViewId = (TextView) itemView.findViewById(R.id.textViewId);
            this.imageView = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }

    public OperatorAdapter(ArrayList<OrderModel> data) {
        this.dataSet = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cards_layout, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        TextView textViewName = holder.textViewName;
        TextView textViewAddress = holder.textViewAddress;
        TextView textViewPhone = holder.textViewPhone;
        TextView textViewId = holder.textViewId;
        ImageView imageView = holder.imageView;
        textViewName.setText(dataSet.get(listPosition).getName());
        textViewAddress.setText(dataSet.get(listPosition).getAddress());
        textViewPhone.setText(dataSet.get(listPosition).getPhone_number());
        textViewId.setText(dataSet.get(listPosition).getId_()+"");
        if (dataSet.get(listPosition).status.equals("maybe"))
        {
            imageView.setImageResource(R.drawable.uncertain);
        }
        if (dataSet.get(listPosition).status.equals("completed"))
        {
            imageView.setImageResource(R.drawable.processing);
        }

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}