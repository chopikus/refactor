public class Add_cafe extends AppCompatActivity {

    class RequestTask extends AsyncTask<String, Void, String> {
        ProgressDialog dialog;

        ArrayList<SupplyModel> data = new ArrayList<SupplyModel>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(Add_cafe.this);
            dialog.setTitle("Загрузка...");
            dialog.setMessage("Отправка запроса. Пожалуйста, подождите...");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();
            Request  request = new Request.Builder().url(strings[0]).build();
            try {
               client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dialog.dismiss();
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cafe);
        final EditText editText = (EditText) findViewById(R.id.editText);
        final EditText editText2 = (EditText) findViewById(R.id.editText2);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject object = new JSONObject();
                    object.put("name", editText.getText().toString());
                    object.put("address", editText2.getText().toString());
                    RequestTask task = new RequestTask();
                    task.execute("http://app.bizzarepizza.xyz/mng/cafe/add?login=test_manager&token=3&data=" + URLEncoder.encode(object.toString(), "UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
