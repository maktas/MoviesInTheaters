package com.maktas.android.moviesintheaters;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by Mehmet Aktas on 26.11.2017.
 */

    /**
     * A placeholder fragment containing a simple view.
     */
public class MoviesFragment extends Fragment {

        private AdView mAdView;
        //ArrayAdapter<String> myAdapter;
        ImageAdapter myAdapter;
        String sort;
        String lang;
        String moviePoster[];
        String[] movieId;
        String[] movieVideo;
        String[] userRating;

        public MoviesFragment() {
        }

        public void onCreate(Bundle savedInstanceState){

            super.onCreate(savedInstanceState);
            //setHasOptionsMenu(true);

        }

        public void onStart(){

            super.onStart();
            updateMovieList();

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            final String ADMOB_APP_ID = "ca-app-pub-7013341411729923~4926568483";
            MobileAds.initialize(getContext(), ADMOB_APP_ID);

            mAdView = (AdView) rootView.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);

            //myAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.listview_movie, R.id.movieText, new ArrayList<String>() );
            myAdapter = new ImageAdapter(this.getContext(), R.layout.listview_movie, new ArrayList<String>() );

            GridView gridView = rootView.findViewById(R.id.grid_view);
            gridView.setAdapter(myAdapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    String titleId = movieId[position];
                    String video = movieVideo[position];

                    Intent detailIntent = new Intent(getActivity(),DetailActivity.class);
                    detailIntent.setAction(Intent.ACTION_SEND);
                    detailIntent.putExtra(Intent.EXTRA_TITLE, titleId);
                    if(video != "") {
                        detailIntent.putExtra("EXTRA_VIDEO", video);
                    }

                    startActivity(detailIntent);
                }
            });

            //Picasso.with(rootView.getContext()).load("http://image.tmdb.org/t/p/w185/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg").into(imageView);

            return rootView;
        }


        public void updateMovieList(){

            sort = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));

            lang = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getString(R.string.pref_lang_key), getString(R.string.pref_lang_default));
            FetchMoviesTask moviesTask = new FetchMoviesTask();

            //Log.v("SORT_VALUE INSIDE updateMovieList: ", sort);

            moviesTask.execute();
        }

        public boolean isOnline() {
            Runtime runtime = Runtime.getRuntime();
            try {
                Process ipProcess = runtime.exec("/system/bin/ping -c 3 8.8.8.8");
                int     exitValue = ipProcess.waitFor();
                return (exitValue == 0);
            }
            catch (IOException e)          { e.printStackTrace(); }
            catch (InterruptedException e) { e.printStackTrace(); }

            return false;
        }


        public class FetchMoviesTask extends AsyncTask<String ,Void, String[]>{

            @Override
            protected String[] doInBackground(String... params) {

                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String releaseDate = "Release Date: ";
                String rating = "User Rating: ";
                String baseUrl = "http://api.themoviedb.org/3/movie/" + sort;
                //sort = "vote_average.desc";
                //.appendQueryParameter("sort_by", sort)
                String api_key = "6bbba6c4a0e1173b9d16a3c4e913cfd7";

                Uri uri = Uri.parse(baseUrl).buildUpon()
                        .appendQueryParameter("language", lang)
                        .appendQueryParameter("api_key",api_key).build();


                if(sort.contentEquals("now_playing")){
                    //System.out.println("sort = Now Playing");
                    if(lang.contentEquals("tr-TR")){
                        uri = Uri.parse(uri.toString()).buildUpon().appendQueryParameter("region", "TR").build();
                    }
                    else if(lang.contentEquals("en-US")){
                        uri = Uri.parse(uri.toString()).buildUpon().appendQueryParameter("region", "US").build();
                    }
                }

                if(lang.contentEquals("tr-TR")){
                    releaseDate = "Vizyon Tarihi: ";
                    rating = "İzleyici Puanı: ";
                }

                String moviesList = getJSONString(uri);

                try {
                    JSONObject popularMovies =new JSONObject(moviesList);
                    JSONArray movieArray = popularMovies.getJSONArray("results");
                    int count = movieArray.length();

                    movieId = new String[count];
                    moviePoster = new String[count];
                    movieVideo = new String[count];
                    userRating = new String[count];

                    for(int i=0; i<movieArray.length();i++){
                            movieId[i] = movieArray.getJSONObject(i).getString("id");
                            userRating[i] = movieArray.getJSONObject(i).getString("vote_average");
                            moviePoster[i] = movieArray.getJSONObject(i).getString("poster_path");
                            movieVideo[i] = getMovieVideo(movieId[i]);

                        //Log.v("moviePosterInfo: ", moviePoster[i]);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally{
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e("FetchMovieTask", "Error closing stream", e);
                        }
                    }
                }

                //Log.v("Buffer: ", buffer.toString());

                return moviePoster;

            }

            protected String getMovieVideo(String movieId) {

                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String baseUrl = "http://api.themoviedb.org/3/movie/" + movieId + "/videos";
                //sort = "vote_average.desc";
                //.appendQueryParameter("sort_by", sort)
                String api_key = "6bbba6c4a0e1173b9d16a3c4e913cfd7";

                Uri uri = Uri.parse(baseUrl).buildUpon()
                        .appendQueryParameter("language", lang)
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
                        Looper.prepare();
                        Toast.makeText(getActivity(), "Please check your internet connection", Toast.LENGTH_LONG);
                        e.printStackTrace();
                    }


                    // Read the input stream into a String
                    InputStream inputStream = null;
                    if (urlConnection != null) {
                        try {
                            inputStream = urlConnection.getInputStream();
                        } catch (IOException e) {
                            Looper.prepare();
                            Toast.makeText(getActivity(), "Couldn't connect to the stream", Toast.LENGTH_LONG);
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
            protected void onPostExecute(String[] result) {
                if(result != null){
                    myAdapter.clear();
                    for(String item:result){
                            myAdapter.add(item);
                    }
                }

            }

        }

        public class ImageAdapter extends ArrayAdapter<String> {

            public ImageAdapter(Context context, int resource, ArrayList<String> list) {
                super(context, resource, list);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent){

                View rootView = LayoutInflater.from(getContext()).inflate(R.layout.listview_movie, parent, false);
                ImageView image = (ImageView) convertView;

                if(image == null) {
                    image = rootView.findViewById(R.id.movie_poster);
                }

                    String url = "http://image.tmdb.org/t/p/w342" + moviePoster[position];
                    Picasso.with(parent.getContext()).load(url.toString()).into(image);

                return image;
            }


        }


}

