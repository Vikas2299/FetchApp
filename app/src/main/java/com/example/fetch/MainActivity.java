package com.example.fetch;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listview);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        new FetchDataTask().execute("https://fetch-hiring.s3.amazonaws.com/hiring.json");
    }

    private class FetchDataTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected List<String> doInBackground(String... urls) {
            List<String> items = new ArrayList<>();
            TreeMap<Integer, TreeMap<Integer, String>> totalItems = new TreeMap<>();

            try {
                // Establish URL connection && read
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }
                JSONArray jsonArray = new JSONArray(response.toString());

                for (int i = 0; i < jsonArray.length(); i++) {
                    //Set id, name, && listid
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int id = jsonObject.optInt("id");
                    String name = jsonObject.optString("name");
                    int listId = jsonObject.optInt("listId");

                    //Check to make sure there is no null or empty strings for name
                    if (name != "null" && !name.trim().isEmpty()) {
                        if (!totalItems.containsKey(listId)) {
                            //Add items that satisfy criteria
                            totalItems.put(listId, new TreeMap<>());
                        }
                        //Add id && name alongside list Ids
                        totalItems.get(listId).put(id, name);
                    }
                }

                List<Integer> keys = new ArrayList<>(totalItems.keySet());
                for (int key : keys) {
                    TreeMap<Integer, String> itemMap = totalItems.get(key);
                    for (String item : itemMap.values()) {
                        //Add colon to display the list in a readable format
                        items.add(key + ": " + item);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return items;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);
            adapter.clear();
            adapter.addAll(result);
        }
    }
}