/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.gael.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Encapsulates fetching the forecast and displaying it as a {@link ListView} layout.
 */
public class PosterFragment extends Fragment implements AbsListView.OnScrollListener {

    private final String LOG_TAG = PosterFragment.class.getSimpleName();

    private ImageAdapter mPosterAdapter;

    public PosterFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updatePoster();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.e(LOG_TAG, "creating view");
        mPosterAdapter =
                new ImageAdapter(
                        getActivity(), // The current context (this activity)
                        R.layout.grid_item_poster, // The name of the layout ID.
                        R.id.grid_item_poster_imageview,
                        new ArrayList<Poster>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the GridView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_poster);
        gridView.setAdapter(mPosterAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Poster poster = mPosterAdapter.getItem(position);
                Context context = getActivity();
                CharSequence text = poster.getKey();
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra("poster", poster);
                startActivity(intent);
            }
        });

        gridView.setOnScrollListener(this);

        return rootView;
    }

    private void updatePoster() {
        FetchPosterTask fetchPosterTask = new FetchPosterTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // we dont do anything with the param
        String location = prefs.getString(getString(R.string.pref_order_key), getString(R.string.pref_order_popular));
        fetchPosterTask.execute(location);
    }

    @Override
    public void onStart() {
        super.onStart();
        updatePoster();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // We should add more item here but we should probably change the container for container with a list or a smarter choice
    }

    public class FetchPosterTask extends AsyncTask<String, Void, Poster[]> {

        private final String LOG_TAG = FetchPosterTask.class.getSimpleName();
        private int lastPage = 0;

        /**
         * Take the String representing the complete movie set in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         */
        private Poster[] getPosterDataFromJson(String posterJsonStr, int posterPerPage) throws JSONException {
            Poster[] resultPosters = new Poster[posterPerPage];

            // These are the names of the JSON objects that need to be extracted.
            final String POSTER_RESULTS = "results";
            final String POSTER_IMG = "poster_path"; // maybe "backdrop_path";
            final String POSTER_ID = "id";
            final String POSTER_TITLE = "original_title";
            final String POSTER_OVERVIEW = "overview";
            final String POSTER_DATE = "release_date";
            final String POSTER_POPULARITY = "popularity";
            final String POSTER_VOTE_AVG = "vote_average";
            final String POSTER_VOTE_COUNT= "vote_count";



            final String POSTER_IMAGE_PREFIX = "http://image.tmdb.org/t/p/w780"; // w780 could maybe be added as an option

            JSONObject forecastJson = new JSONObject(posterJsonStr);
            JSONArray posterArray = forecastJson.getJSONArray(POSTER_RESULTS);

            for(int i = 0; i < posterArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                JSONObject poster = posterArray.getJSONObject(i);

                resultPosters[i] = new Poster(
                        poster.getString(POSTER_ID),
                        POSTER_IMAGE_PREFIX + poster.getString(POSTER_IMG),
                        poster.getString(POSTER_TITLE),
                        poster.getString(POSTER_OVERVIEW),
                        poster.getString(POSTER_VOTE_AVG),
                        poster.getString(POSTER_DATE)
                );
            }
            return resultPosters;

        }
        @Override
        protected Poster[] doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String posterJsonStr = null;

            int numPosters = 20; // not good, should be handled with the paging

            try {

                final String POSTER_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";

                // http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=6bc8d934671e56b5e960c035bab8fa8b&page=1
                final String SORT_PARAM = "sort_by";
                final String KEY_PARAM = "api_key";
                final String PAGE_PARAM = "page";

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String sort = prefs.getString(
                        getString(R.string.pref_order_key),
                        getString(R.string.pref_order_popular));

                final String default_key = getString(R.string.pref_api_key_default);
                String api_key = prefs.getString(
                        getString(R.string.pref_api_key_key),
                        default_key);

                Uri builtUri = Uri.parse(POSTER_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, sort)
                        .appendQueryParameter(KEY_PARAM, api_key)
                        .appendQueryParameter(PAGE_PARAM, "1")
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                posterJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getPosterDataFromJson(posterJsonStr, numPosters);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(Poster[] result) {
            if (result != null) {
                mPosterAdapter.clear();
                for(Poster poster : result) {
                    mPosterAdapter.add(poster);
                }
            }
        }
    }
}
