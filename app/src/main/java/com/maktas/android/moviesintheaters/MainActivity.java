package com.maktas.android.moviesintheaters;

import com.android.vending.billing.*;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;

import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    IInAppBillingService mService;
    ServiceConnection mServiceConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MoviesFragment())
                    .commit();
        }

        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name,
                                           IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
            }
        };

        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        //final String ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713";
        //MobileAds.initialize(this, "ADMOB_APP_ID");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search1).getActionView();

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
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        if (id == R.id.action_about) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                    MainActivity.this);
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

}
