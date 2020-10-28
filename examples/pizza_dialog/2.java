class DialogEdit{
    public void dialogEdit(final int position)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(SupplyAddProducts.this);
        final View view = getLayoutInflater()
                .inflate(R.layout.supply_add_product_dialog_second_step, null);
        builder.setView(view);
        builder.setTitle("Изменить продукт поставки");
        final TextView textViewName = (TextView) view.findViewById(R.id.textViewName);
        final EditText editTextAmount = (EditText) view.findViewById(R.id.editTextAmount);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.datePicker);
        final TextView textViewMeasurementUnit = (TextView) view.findViewById(R.id.textViewMeasurementUnit);
        String date = arrayList.get(position).getExpireDate();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            calendar.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        textViewName.setText(arrayList.get(position).getName());
        editTextAmount.setText(arrayList.get(position).getAmount());
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
                    Calendar cal = Calendar.getInstance();
                    cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    object.put("expiry", format.format(cal.getTime()));
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
