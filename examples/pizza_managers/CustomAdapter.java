package com.chopikus.manager_app;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private ArrayList<DataModel> dataSet;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        TextView textViewVersion;
        ImageView imageViewIcon;
        TextView textViewId;
        TextView textViewMinutes;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            this.textViewVersion = (TextView) itemView.findViewById(R.id.textViewVersion);
            this.imageViewIcon = (ImageView) itemView.findViewById(R.id.imageView);
            this.textViewId = (TextView) itemView.findViewById(R.id.textViewId);
            this.textViewMinutes = (TextView) itemView.findViewById(R.id.textViewMinutes);
        }
    }

    public CustomAdapter(ArrayList<DataModel> data) {
        this.dataSet = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dishes_layout, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        TextView textViewName = holder.textViewName;
        TextView textViewVersion = holder.textViewVersion;
        ImageView imageView = holder.imageViewIcon;
        TextView textViewId = holder.textViewId;
        TextView textViewMinutes = holder.textViewMinutes;
        textViewName.setText(dataSet.get(listPosition).getName());
        textViewVersion.setText(dataSet.get(listPosition).getPrice().toString()+" грн/"+dataSet.get(listPosition).getAmount()+" "+dataSet.get(listPosition).getUnit());
        Picasso.get().load(dataSet.get(listPosition).getImageUrl()).into(imageView);
        textViewId.setText(String.valueOf(dataSet.get(listPosition).getId_()));
        textViewMinutes.setText(dataSet.get(listPosition).minutes);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}