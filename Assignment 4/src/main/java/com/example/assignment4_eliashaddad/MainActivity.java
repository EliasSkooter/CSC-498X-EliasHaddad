package com.example.assignment4_eliashaddad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase db;
    Cursor c;
    List<String> articlesList;
    List<String> articlesString;
    List<JSONObject> articlesJson;
    List<String> articlesHtml;
    int currentIndex;
    ListView listView;
    ArrayAdapter arrayAdapter;
    List<String> dbArticleNames;
    List<String> dbArticlesUrls;

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String data = reader.readLine();

                while (data != null) {
                    result += data;
                    data = reader.readLine();
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
                return "failed";
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            s = s.substring(1);
            List<String> idList = new ArrayList<String>();
            try {
                idList = Arrays.asList(s.split(","));
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<String> newIdList = new ArrayList<>();

            for (int i = 0; i < idList.size(); i++) {
                if (idList.get(i).equals("29251357") || idList.get(i).equals("29252974") || idList.get(i).equals("29251343") || idList.get(i).equals("29254063") || idList.get(i).equals("29253277") || idList.get(i).equals("29251798") || idList.get(i).equals("29254236") || idList.get(i).equals("29255283") || idList.get(i).equals("29266094")) {
                    System.out.println("Found id with no url!!! " + idList.get(i));
                } else {
                    newIdList.add(idList.get(i));
                }
            }

            if (newIdList.size() > 20) {
                for (int i = 0; i < 20; i++) {
                    articlesList.add("https://hacker-news.firebaseio.com/v0/item/" + newIdList.get(i) + ".json?print=pretty");
                }
            } else {
                for (int i = 0; i < newIdList.size(); i++) {
                    articlesList.add("https://hacker-news.firebaseio.com/v0/item/" + newIdList.get(i) + ".json?print=pretty");
                }
            }

            Log.i("resulttttt ====>", articlesList.toString());

            DownloadArticlesTask firstTask = new DownloadArticlesTask();
            try {
                firstTask.execute(articlesList.get(0)).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class DownloadArticlesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String data = reader.readLine();

                while (data != null) {
                    result += data;
                    data = reader.readLine();
                }
                reader.close();

            } catch (Exception e) {
                e.printStackTrace();
                return "failed";
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (currentIndex < articlesList.size()) {
                DownloadArticlesTask newTask = new DownloadArticlesTask();
                try {
                    String result = newTask.execute(articlesList.get(currentIndex)).get();
                    currentIndex++;
                    articlesString.add(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    currentIndex++;
                    System.out.println("FAILED TO RETRIEVE ARTICLE AT INDEX " + currentIndex);
                }
            } else {
                for (int i = 0; i < articlesString.size(); i++) {
                    try {
                        JSONObject json = new JSONObject(articlesString.get(i));
                        articlesJson.add(json);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                currentIndex = 0;
                DownloadHtmlTask htmlTask = new DownloadHtmlTask();
                try {
                    htmlTask.execute(articlesJson.get(0).getString("url"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class DownloadHtmlTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String data = reader.readLine();

                while (data != null) {
                    result += data;
                    data = reader.readLine();
                }
                reader.close();

            } catch (Exception e) {
                e.printStackTrace();
                return "failed";
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (currentIndex < articlesJson.size()) {
                DownloadHtmlTask newTask = new DownloadHtmlTask();
                try {
                    String currentUrl = articlesJson.get(currentIndex).getString("url");
                    System.out.println("currentUrl ===> " + currentUrl + " at index: " + currentIndex);
                    String result = newTask.execute(currentUrl).get();
                    currentIndex++;
                    articlesHtml.add(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("FAILED TO RETRIEVE ARTICLE AT INDEX " + currentIndex);
                    System.out.println("FAILED TO RETRIEVE ARTICLE  " + articlesJson.get(currentIndex));
                    currentIndex++;
                }
            } else {
                for (int i = 0; i < articlesHtml.size(); i++) {
                    try {
                        db.execSQL("INSERT INTO articles (id_article, name, content) VALUES (" + articlesJson.get(i).getString("id") + ",'" + articlesJson.get(i).getString("title").replace("'", "char(39)") + "','" + articlesJson.get(i).getString("url").replace("'", "char(39)") + "')");
                        System.out.println("saved article: " + i + "!");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void OpenArticlePage(String title, String url) {
        Intent intent = new Intent(this, MainActivity2.class);
        intent.putExtra("title", title);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = this;
        articlesList = new ArrayList<>();
        articlesString = new ArrayList<>();
        articlesJson = new ArrayList<>();
        articlesHtml = new ArrayList<>();
        dbArticleNames = new ArrayList<>();
        dbArticlesUrls = new ArrayList<>();
        currentIndex = 0;
        listView = (ListView) findViewById(R.id.listView);
        db = this.openOrCreateDatabase("assignment4db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS articles (id_article INT, name VARCHAR(255), content LONGTEXT)");

        try {
            c = db.rawQuery("Select * from articles", null);
            int nameIndex = c.getColumnIndex("name");
            int contentIndex = c.getColumnIndex("content");
            c.moveToFirst();

            while (c != null) {
                String name = c.getString(nameIndex).replace("char(39)", "'");
                String url = c.getString(contentIndex).replace("char(39)", "'");
                dbArticleNames.add(name);
                dbArticlesUrls.add(url);
                if (!c.moveToNext()) {
                    System.out.println("Done!!!!");
                    break;
                }
            }

            System.out.println("Final articles name list: " + dbArticleNames.toString());
            System.out.println("Final articles url list: " + dbArticlesUrls.toString());

            arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_expandable_list_item_1, dbArticleNames);
            listView.setAdapter(arrayAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    System.out.println("This is item: " + position);
                    OpenArticlePage(dbArticleNames.get(position), dbArticlesUrls.get(position));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


//        DownloadTask task = new DownloadTask();
//        try {
//            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json").get();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}