class DialogEdit
{
    public void dialogEdit(final int position)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(DishAddProducts.this);
        final View view = getLayoutInflater()
                .inflate(R.layout.supply_add_product_dialog_second_step, null);
        builder.setView(view);
        builder.setTitle("Изменить продукт поставки");
        final TextView textViewName = (TextView) view.findViewById(R.id.textViewName);
        final EditText editTextAmount = (EditText) view.findViewById(R.id.editTextAmount);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.datePicker);
        final TextView textViewMeasurementUnit = (TextView) view.findViewById(R.id.textViewMeasurementUnit);
        datePicker.setVisibility(View.GONE);
        textViewName.setText(arrayList.get(position).getName());
        editTextAmount.setText(arrayList.get(position).getAmount()+"");
        textViewMeasurementUnit.setText(arrayList.get(position).getMeasurementUnit());
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("Готово", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    SharedPreferences preferences = getSharedPreferences("add_product", Context.MODE_PRIVATE);
                    String productsJSON = preferences.getString("products", "[]");
                    JSONArray array = new JSONArray(productsJSON);
                    JSONObject object = array.getJSONObject(position);
                    object.put("amount", editTextAmount.getText().toString());
                    preferences.edit().putString("products", array.toString()).apply();
                    loadList();
                }
                catch (Exception e)
                {

                }
            }
        });
        builder.show();
    }
}
