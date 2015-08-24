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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        private Poster mPoster;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            Log.e(LOG_TAG, "onCreateView ");

            // The detail Activity called via intent.  Inspect the intent for poster data.
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra("poster")) {
                mPoster = (Poster)intent.getSerializableExtra("poster");
                ((TextView) rootView.findViewById(R.id.detail_title)).setText(mPoster.getTitle());
                ((TextView) rootView.findViewById(R.id.detail_synopsis)).setText(mPoster.getOverview());
                ((TextView) rootView.findViewById(R.id.detail_vote)).setText(mPoster.getRating());
                ((TextView) rootView.findViewById(R.id.detail_date)).setText(mPoster.getDate());
                Picasso.with(getActivity()).load(mPoster.getValue()).into((ImageView) rootView.findViewById(R.id.detail_poster));
                ((TextView) rootView.findViewById(R.id.detail_title)).setText(mPoster.getTitle());

            }

            return rootView;
        }
    }
}
