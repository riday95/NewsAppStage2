package com.example.android.newsapp;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import com.example.android.newsapp.databinding.ActivityMainBinding;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>> {
    //DataBinding
    ActivityMainBinding binding;
    private NewsAdapter mAdapter;

    /**
     * Constant value for the news loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    public static final int LOADER_ID = 1;
    public static final String URL = "http://content.guardianapis.com/search?";

    private static final String ORDER = "order-by";
    private static final String DATE = "newest";
    private static final String PAGE = "page-size";
    private static final String PAGES = "20";
    private static final String SECTION = "section";
    private static final String TAGS = "show-tags";
    private static final String AUTHOR = "contributor";

    private static final String API_KEY = "api-key";
    private static final String KEY = "455abfcd-a621-4438-836d-91edbdf3101c";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();


        if (networkInfo != null && networkInfo.isConnected()) {

            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            /** Initialize the loader. Pass in the int ID constant defined above and pass in null for
             *the bundle. Pass in this activity for the LoaderCallbacks parameter (which is
             * valid because this activity implements the LoaderCallbacks interface). */

            loaderManager.initLoader(LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            binding.progressbarIdView.setVisibility(View.GONE);
            binding.emptyView.setVisibility(View.VISIBLE);
            binding.emptyView.setText(R.string.no_internet_connection);
        }

        mAdapter = new NewsAdapter(this, new ArrayList<News>());
        binding.listView.setAdapter(mAdapter);

        binding.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Find the current news that was clicked on
                News currentNews = mAdapter.getItem(position);
                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri newsUri = Uri.parse(currentNews.getUrl());
                // Create a new intent to view the news URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);
                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });
    }

    @Override
    // onCreateLoader instantiates and returns a new Loader for the given ID
    public Loader<List<News>> onCreateLoader(int id, Bundle args) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String searchCategory = sharedPrefs.getString(getString(R.string.chosen_option), getString(R.string.defaultOption_val));

        String[] categorySearch = searchCategory.split(" ");
        String catInCapitals = categorySearch[0];
        String category = catInCapitals.toLowerCase();

        // Parse breaks apart the URI string that's passed into its parameter
        Uri uri = Uri.parse(URL);
        // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
        Uri.Builder uriBuilder = uri.buildUpon();

        if (!category.equals("all")) {
            uriBuilder.appendQueryParameter(SECTION, category);
        }

        // Append query parameter and its value.
        uriBuilder.appendQueryParameter(API_KEY, KEY);
        uriBuilder.appendQueryParameter(ORDER, DATE);
        uriBuilder.appendQueryParameter(PAGE, PAGES);
        uriBuilder.appendQueryParameter(TAGS, AUTHOR);

        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {
        // Hide loading indicator because the data has been loaded
        binding.progressbarIdView.setVisibility(View.GONE);
        // Set empty state text to display "No news available found, please come back later and try again!"
        binding.emptyView.setText(R.string.no_news);
        // Clear the adapter of previous news data
        mAdapter.clear();
        // If there is a valid list of {@link News}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (news != null && !news.isEmpty()) {
            mAdapter.addAll(news);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    @Override
    // This method initialize the contents of the Activity's options menu.
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the Options Menu we specified in XML
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
