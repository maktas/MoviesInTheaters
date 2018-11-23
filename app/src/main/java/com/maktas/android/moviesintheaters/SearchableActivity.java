package com.maktas.android.moviesintheaters;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Mehmet Aktas on 29.11.2017.
 */
public class SearchableActivity extends ListActivity{

    ListView listView;
    ArrayAdapter<String> myAdapter;
    String[] movieId = {};
    String[] movieTitle = {};
    String[] movieVideo = {};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        listView = this.findViewById(android.R.id.list);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            MovieSearchTask movieSearch = new MovieSearchTask();

            movieSearch.execute(query);

            myAdapter = new ArrayAdapter<String>(this,
                    R.layout.search_result, new ArrayList<String>());

            listView.setAdapter(myAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                    String id = movieId[position];
                    String video = movieVideo[position];

                    Intent detailIntent = new Intent(view.getContext(),DetailActivity.class);
                    detailIntent.setAction(Intent.ACTION_SEND);
                    detailIntent.putExtra(Intent.EXTRA_TITLE, id);
                    if(video != ""){
                        detailIntent.putExtra("EXTRA_VIDEO", video);
                    }


                    startActivity(detailIntent);

                }
            });
        }
    }

    public class MovieSearchTask extends AsyncTask<String ,Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {

            String query = params[0];

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String baseUrl = "http://api.themoviedb.org/3/search/movie";
            //sort = "vote_average.desc";
            //.appendQueryParameter("sort_by", sort)
            String api_key = "6bbba6c4a0e1173b9d16a3c4e913cfd7";
            String lang = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getString(R.string.pref_lang_key), getString(R.string.pref_lang_default));

            Uri uri = Uri.parse(baseUrl).buildUpon()
                    .appendQueryParameter("language", lang)
                    .appendQueryParameter("api_key", api_key)
                    .appendQueryParameter("query", query).build();

            URL url = null;
            try {
                url = new URL(uri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            // Create the request to OpenWeatherMap, and open the connection
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
            } catch (IOException e) {
                Looper.prepare();
                Toast.makeText(getApplicationContext(), "Please check your internet connection", Toast.LENGTH_LONG);
                e.printStackTrace();
            }


            // Read the input stream into a String
            InputStream inputStream = null;
            if (urlConnection != null) {
                try {
                    inputStream = urlConnection.getInputStream();
                } catch (IOException e) {
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(), "Couldn't connect to the stream", Toast.LENGTH_LONG);
                    e.printStackTrace();
                }
            }
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }


            String moviesList = buffer.toString();

            try {
                JSONObject popularMovies = new JSONObject(moviesList);
                JSONArray movieArray = popularMovies.getJSONArray("results");
                int count = movieArray.length();

                movieId = new String[count];
                movieTitle = new String[count];
                movieVideo = new String[count];

                for (int i = 0; i < count; i++) {
                    movieId[i] = movieArray.getJSONObject(i).getString("id");
                    movieTitle[i] = movieArray.getJSONObject(i).getString("title");
                    movieVideo[i] = getMovieVideo(movieId[i]);

                    //System.out.println(i +". Film: " + movieTitle[i]);

                    //Log.v("moviePosterInfo: ", moviePoster[i]);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("MovieSearchTask", "Error closing stream", e);
                    }
                }
            }

            return movieTitle;

        }

        protected String getMovieVideo(String movieId) {

            String baseUrl = "http://api.themoviedb.org/3/movie/" + movieId + "/videos";
            //sort = "vote_average.desc";
            //.appendQueryParameter("sort_by", sort)
            String api_key = "6bbba6c4a0e1173b9d16a3c4e913cfd7";

            Uri uri = Uri.parse(baseUrl).buildUpon()
                    //.appendQueryParameter("language", lang)
                    .appendQueryParameter("api_key", api_key).build();


            String movieVideos = getJSONString(uri);
            String videoKey="";

            try {
                JSONObject videoObject =new JSONObject(movieVideos);
                JSONArray videoArray = videoObject.getJSONArray("results");
                if(videoArray.length() != 0) {
                    videoKey = videoArray.getJSONObject(0).getString("key");
                }
            }catch(JSONException e){
                e.printStackTrace();
            }

            return videoKey;

        }

        public String getJSONString(Uri uri){

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            URL url = null;
            try {
                url = new URL(uri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            // Create the request to OpenWeatherMap, and open the connection
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Please check your internet connection", Toast.LENGTH_LONG);
                e.printStackTrace();
            }


            // Read the input stream into a String
            InputStream inputStream = null;
            if (urlConnection != null) {
                try {
                    inputStream = urlConnection.getInputStream();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Couldn't connect to the stream", Toast.LENGTH_LONG);
                    e.printStackTrace();
                }
            }
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }


            String jsonString = buffer.toString();

            return jsonString;

        }


        @Override
        protected void onPostExecute(String[] result){

            if(myAdapter.getCount() != 0){
                myAdapter.clear();
            }
            if(result != null){
                for(int i=0; i<result.length; i++){
                    myAdapter.add(result[i]);
                }
            }

        }

    }

}
