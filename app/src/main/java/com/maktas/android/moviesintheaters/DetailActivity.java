package com.maktas.android.moviesintheaters;

/**
 * Created by Mehmet Aktas on 27.11.2017.
 */

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.squareup.picasso.Picasso;

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

public class DetailActivity extends AppCompatActivity {

    private static AdView mAdView;
    private static String movieInfo;
    private static String moviePoster;
    private static String movieTitle;
    private static String movieVideo;
    private static float movieRating;
    private static String movieId;
    private static String userRating;
    private static String movieBudget;
    private static String movieRevenue;
    private static String movieGenres;
    private static String lang;
    private static View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }

        movieId = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        movieVideo = getIntent().getStringExtra("EXTRA_VIDEO");
        updateMovie();

        final String ADMOB_APP_ID = "ca-app-pub-7013341411729923~4926568483";
        MobileAds.initialize(getApplicationContext(), ADMOB_APP_ID);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search2).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchableActivity.class)));
        searchView.setIconifiedByDefault(true); // Iconify the widget; do not expand it by default

        searchView.clearFocus();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, DetailSettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        if (id == R.id.action_about) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                    DetailActivity.this);
            // Setting Dialog Title
            alertDialog.setTitle("About Movies in Theaters");

            // Setting Dialog Message
            alertDialog.setMessage("This application is designed and realized by Mehmet Aktaş.\n" +
                    "This product uses the TMDb API but is not endorsed or certified by TMDb. ");

            // Setting Icon to Dialog
            alertDialog.setIcon(R.drawable.powered_by);

            AlertDialog alertDialogMain = alertDialog.create();

            // Setting OK Button
            alertDialogMain.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Write your code here to execute after dialog closed
                    Toast.makeText(getApplicationContext(), "Thanks for downloading!", Toast.LENGTH_SHORT).show();
                }
            });

            // Showing Alert Message
            alertDialogMain.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateMovie(){

        lang = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getString(R.string.pref_lang_key), getString(R.string.pref_lang_default));
        FetchMovieTask movieTask = new FetchMovieTask();

        movieTask.execute();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            rootView = inflater.inflate(R.layout.detail_view, container, false);

            mAdView = (AdView) rootView.findViewById(R.id.adViewDetail);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);

            return rootView;
        }

    /*    private Intent createShareForecastIntent(){

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_STRING_HASHTAG);
            return shareIntent;
        }
        */

    }

    public class FetchMovieTask extends AsyncTask<String ,Void, String> {

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String releaseDate = "Release Date: ";
            String rating = "User Rating: ";
            String budget = "Budget: $";
            String revenue = "Revenue: $";
            String genre = "Genres: ";
            String baseUrl = "http://api.themoviedb.org/3/movie/" + movieId;
            //sort = "vote_average.desc";
            //.appendQueryParameter("sort_by", sort)
            String api_key = "6bbba6c4a0e1173b9d16a3c4e913cfd7";

            Uri uri = Uri.parse(baseUrl).buildUpon()
                    .appendQueryParameter("language", lang)
                    .appendQueryParameter("api_key",api_key).build();


            if(lang.contentEquals("tr-TR")){
                releaseDate = "Vizyon Tarihi: ";
                rating = "İzleyici Puanı: ";
                budget = "Film Bütçesi: $";
                revenue = "Gelir: $";
                genre = "Tür: ";
            }

            String moviesList = getJSONString(uri);

            try {
                JSONObject popularMovies =new JSONObject(moviesList);
                JSONArray genres = popularMovies.getJSONArray("genres");
                int count = genres.length();

                movieGenres = new String();

                for(int i=0; i<count; i++){
                    movieGenres += genres.getJSONObject(i).getString("name") + ",";
                }

                if(movieGenres != null) {
                    movieGenres = movieGenres.substring(0, movieGenres.length() - 1); }
                    movieBudget = popularMovies.getString("budget");
                    movieRevenue = popularMovies.getString("revenue");
                    movieTitle = popularMovies.getString("title");
                    userRating = popularMovies.getString("vote_average");
                    moviePoster = popularMovies.getString("poster_path");

                    movieInfo = popularMovies.getString("overview") + "\n\n"
                            + genre + movieGenres + "\n\n"
                            + releaseDate + popularMovies.getString("release_date");
                if(!movieBudget.contentEquals("0")) {
                    movieInfo += "\n\n" + budget + movieBudget + "\n\n"
                            + revenue + movieRevenue;
                }

                    movieInfo +=  "\n\n" + rating + userRating;

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

            return movieInfo;

        }

        public void onPostExecute(String info){

            TextView titleText = (TextView) rootView.findViewById(R.id.original_title);
            titleText.setText(movieTitle);
            TextView overviewText = (TextView) rootView.findViewById(R.id.overview);
            overviewText.setText(info);
            TextView playSymbol = (TextView) rootView.findViewById(R.id.playIcon);

            movieRating = Float.valueOf(userRating);

            RatingBar ratingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);
            ratingBar.setNumStars(10);
            ratingBar.setStepSize(0.1f);
            ratingBar.setRating(movieRating);

            ImageView poster = (ImageView) rootView.findViewById(R.id.poster);

            String url = "http://image.tmdb.org/t/p/w500" + moviePoster;
            Picasso.with(rootView.getContext()).load(url.toString()).into(poster);

            if(movieVideo != null) {
                playSymbol.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://www.youtube.com/watch?v=" + movieVideo)));
                    }
                });

                playSymbol.setVisibility(View.VISIBLE);
            }

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


            String jsonString = buffer.toString();

            return jsonString;

        }


    }

}

