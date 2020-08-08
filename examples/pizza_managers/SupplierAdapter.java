package com.chopikus.manager_app;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SupplierAdapter extends RecyclerView.Adapter<SupplierAdapter.MyViewHolder> {

    private ArrayList<SupplierModel> dataSet;
    LayoutInflater inflater;

    public SupplierAdapter(Context context, List<SupplierModel> list) {
        inflater = LayoutInflater.from(context);
        dataSet = new ArrayList<>(list);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.supplier_layout, parent, false);
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
        TextView textViewName,textViewId,textViewContractNumber, textViewPhone;
        public MyViewHolder(View itemView) {
            super(itemView);
            textViewId = (TextView) itemView.findViewById(R.id.textViewId);
            textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            textViewContractNumber = (TextView) itemView.findViewById(R.id.textViewContractNumber);
            textViewPhone = (TextView) itemView.findViewById(R.id.textViewPhoneNumber);
        }

        public void bindData(SupplierModel rowModel) {
            textViewId.setText(rowModel.getId());
            textViewName.setText(rowModel.getName());
            textViewContractNumber.setText("Номер договора: \n"+rowModel.getContractNumber());
            textViewPhone.setText(rowModel.phoneNumber);
        }
    }
}
