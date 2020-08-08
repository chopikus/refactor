package com.chopikus.manager_app;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CafeAdapter extends RecyclerView.Adapter<CafeAdapter.MyViewHolder> {

    private ArrayList<CafeModel> dataSet;
    LayoutInflater inflater;

    public CafeAdapter(Context context, List<CafeModel> list) {
        inflater = LayoutInflater.from(context);
        dataSet = new ArrayList<>(list);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.cards_layout, parent, false);
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


        TextView textViewName,textViewVersion,textViewId;
        ImageView imageView;
        public MyViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewVersion = itemView.findViewById(R.id.textViewVersion);
            imageView = itemView.findViewById(R.id.imageView);
            textViewId = itemView.findViewById(R.id.textViewId);
        }

        public void bindData(CafeModel rowModel) {
            textViewName.setText(rowModel.getName());
            textViewVersion.setText(rowModel.getAddress());
            imageView.setImageBitmap(rowModel.getImage());
            textViewId.setText(String.valueOf(rowModel.id_));
        }
    }
}
