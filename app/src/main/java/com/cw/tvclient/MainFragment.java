/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.cw.tvclient;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.youtube.player.YouTubeIntents;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.net.ssl.HttpsURLConnection;

import static com.cw.tvclient.MovieList.getYoutubeId;

public class MainFragment extends BrowseFragment {
	private static final String TAG = "MainFragment";

	private static final int BACKGROUND_UPDATE_DELAY = 300;
	private static final int GRID_ITEM_WIDTH = 200;
	private static final int GRID_ITEM_HEIGHT = 200;
//	private static final int NUM_ROWS = 6;
//	private static final int NUM_COLS = 15;

	private final Handler mHandler = new Handler();
	private Drawable mDefaultBackground;
	private DisplayMetrics mMetrics;
	private Timer mBackgroundTimer;
	private String mBackgroundUri;
	private BackgroundManager mBackgroundManager;
	int rowsCount;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onActivityCreated(savedInstanceState);

		prepareBackgroundManager();

		setupUIElements();

		// check connection
		CheckHttpsConnection check = new CheckHttpsConnection();
		check.execute();
		while(!check.checkIsReady)
		{
			try {
				Thread.sleep(1000);
				System.out.println("waiting");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// get pages
		rowsCount = getRowsCount();

		loadRows();
		setupEventListeners();
	}



	class CheckHttpsConnection extends AsyncTask<Void,Void,Void>
	{
		int code = -1;
		boolean checkIsReady;
		@Override
		protected Void doInBackground(Void... voids) {
			checkIsReady = false;
			// HTTPS POST
			String project = "LiteNote";
			String urlStr =  "https://" + project + ".ddns.net:8443/"+ project +"Web/client/viewNote_json.jsp";

			try {
				URL url = new URL(urlStr);
				MovieList.trustEveryone();
				HttpsURLConnection connection = ((HttpsURLConnection) url.openConnection());
				connection.connect();
				code = connection.getResponseCode();
				if (code == 200) {
					// reachable
					checkIsReady = true;
				} else {
					Toast.makeText(getActivity(),"Network connection failed.",Toast.LENGTH_SHORT).show();
				}
				connection.disconnect();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (null != mBackgroundTimer) {
			Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
			mBackgroundTimer.cancel();
		}
	}

	int getRowsCount()
	{
		GetRowsTask task = new GetRowsTask();
		task.execute();
		while (!task.isGetReady)
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return task.count;
	}


	private static class GetRowsTask extends AsyncTask<Void,Void,Void> {
		boolean isGetReady;
		int count;
		GetRowsTask(){ }

		@Override
		protected Void doInBackground(Void... voids) {
			System.out.println("MainFragment / GetRowsTask /_doInBackground");
			isGetReady = false;
			String strResult = "";

			// HTTPS POST
			String project = "LiteNote";
			String urlStr =  "https://" + project + ".ddns.net:8443/"+ project +"Web/client/viewTotalPages.jsp";

			try {
				URL url = new URL(urlStr);
				MovieList.trustEveryone();
				HttpsURLConnection urlConnection = ((HttpsURLConnection)url.openConnection());

				// set Timeout and method
				urlConnection.setReadTimeout(7000);
				urlConnection.setConnectTimeout(7000);
				urlConnection.setRequestMethod("POST");
				urlConnection.setDoInput(true);
				urlConnection.setDoOutput( true );
				urlConnection.setInstanceFollowRedirects( false );
				urlConnection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
				urlConnection.setRequestProperty( "charset", "utf-8");
				urlConnection.setUseCaches( false );
				try( DataOutputStream wr = new DataOutputStream( urlConnection.getOutputStream())) {
					wr.close();
					wr.flush();
				}

				// Add any data you wish to post here
				urlConnection.connect();
				InputStream in = urlConnection.getInputStream();

				if(in != null) {
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					String inputLine;

					while ((inputLine = br.readLine()) != null) {
						System.out.println("MainFragment / GetRowsTask / inputLine = " + inputLine);
						strResult += inputLine;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("MainFragment / GetRowsTask / result final = " + strResult);

			// JSON array
			try {
				JSONArray jsonArray = new JSONArray(strResult);
				for (int i = 0; i < jsonArray.length(); i++)
				{
					JSONObject jsonObject = (JSONObject) jsonArray.get(i);
					count = jsonObject.getInt("totalPagesCount");
				}
				isGetReady = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return null;
		}
		@Override
		protected void onPostExecute(Void Result){
			super.onPostExecute(Result);

		}
	}



	private void loadRows() {

		ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
		CardPresenter cardPresenter = new CardPresenter();


		int row;
//		for (row = 0; row < NUM_ROWS; row++) {
		for (row = 0; row < rowsCount; row++) {

			// prepare
			MovieList.isDataReady = false;
			MovieList.prepareList(row+1);//table name starts from 1
			while (!MovieList.isDataReady)
			{
				System.out.println("MainFragment / waiting ...");
				try {
					Thread.sleep(1000);
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			// setup list
			List<Movie> list = MovieList.setupMovies();

			//			if (row != 0) {
//				Collections.shuffle(list);
//			}
			ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
//			for (int j = 0; j < NUM_COLS; j++) {

			for (int col = 0; col < list.size(); col++) {
					listRowAdapter.add(list.get(col));
			}

//			HeaderItem header = new HeaderItem(row, MovieList.MOVIE_CATEGORY[row]);
			HeaderItem header = new HeaderItem(row, "Set "+(row+1));
			rowsAdapter.add(new ListRow(header, listRowAdapter));
		}

		HeaderItem gridHeader = new HeaderItem(row, "PREFERENCES");

		GridItemPresenter mGridPresenter = new GridItemPresenter();
		ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
		gridRowAdapter.add(getResources().getString(R.string.grid_view));
		gridRowAdapter.add(getString(R.string.error_fragment));
		gridRowAdapter.add(getResources().getString(R.string.personal_settings));
		rowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

		setAdapter(rowsAdapter);
	}

	private void prepareBackgroundManager() {

		mBackgroundManager = BackgroundManager.getInstance(getActivity());
		mBackgroundManager.attach(getActivity().getWindow());

		mDefaultBackground = ContextCompat.getDrawable(getContext(), R.drawable.default_background);
		mMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
	}

	private void setupUIElements() {
		// setBadgeDrawable(getActivity().getResources().getDrawable(
		// R.drawable.videos_by_google_banner));
		setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
		// over title
		setHeadersState(HEADERS_ENABLED);
		setHeadersTransitionOnBackEnabled(true);

		// set fastLane (or headers) background color
		setBrandColor(ContextCompat.getColor(getContext(), R.color.fastlane_background));
		// set search icon color
		setSearchAffordanceColor(ContextCompat.getColor(getContext(), R.color.search_opaque));
	}

	private void setupEventListeners() {
		setOnSearchClickedListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				Toast.makeText(getActivity(), "Implement your own in-app search", Toast.LENGTH_LONG)
						.show();
			}
		});

		setOnItemViewClickedListener(new ItemViewClickedListener());
		setOnItemViewSelectedListener(new ItemViewSelectedListener());
	}

	private void updateBackground(String uri) {
		int width = mMetrics.widthPixels;
		int height = mMetrics.heightPixels;
		Glide.with(getActivity())
				.load(uri)
				.centerCrop()
				.error(mDefaultBackground)
				.into(new SimpleTarget<GlideDrawable>(width, height) {
					@Override
					public void onResourceReady(GlideDrawable resource,
					                            GlideAnimation<? super GlideDrawable>
							                            glideAnimation) {
						mBackgroundManager.setDrawable(resource);
					}
				});
		mBackgroundTimer.cancel();
	}

	private void startBackgroundTimer() {
		if (null != mBackgroundTimer) {
			mBackgroundTimer.cancel();
		}
		mBackgroundTimer = new Timer();
		mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
	}

	private final class ItemViewClickedListener implements OnItemViewClickedListener {
		@Override
		public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
		                          RowPresenter.ViewHolder rowViewHolder, Row row) {

			if (item instanceof Movie) {
				Movie movie = (Movie) item;
				Log.d(TAG, "Item: " + item.toString());
//				Intent intent = new Intent(getActivity(), DetailsActivity.class);
//				intent.putExtra(DetailsActivity.MOVIE, movie);
//				Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
//						getActivity(),
//						((ImageCardView) itemViewHolder.view).getMainImageView(),
//						DetailsActivity.SHARED_ELEMENT_NAME)
//						.toBundle();
//				getActivity().startActivity(intent, bundle);

				String idStr = getYoutubeId(movie.getVideoUrl() );
				Intent intent = YouTubeIntents.createPlayVideoIntentWithOptions(getActivity(), idStr, false/*fullscreen*/, true/*finishOnEnd*/);
				startActivity(intent);

			} else if (item instanceof String) {
				if (((String) item).contains(getString(R.string.error_fragment))) {
					Intent intent = new Intent(getActivity(), BrowseErrorActivity.class);
					startActivity(intent);
				} else {
					Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
		@Override
		public void onItemSelected(
				Presenter.ViewHolder itemViewHolder,
				Object item,
				RowPresenter.ViewHolder rowViewHolder,
				Row row) {
			if (item instanceof Movie) {
				mBackgroundUri = ((Movie) item).getBackgroundImageUrl();
				startBackgroundTimer();
			}
		}
	}

	private class UpdateBackgroundTask extends TimerTask {

		@Override
		public void run() {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					updateBackground(mBackgroundUri);
				}
			});
		}
	}

	private class GridItemPresenter extends Presenter {
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent) {
			TextView view = new TextView(parent.getContext());
			view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
			view.setFocusable(true);
			view.setFocusableInTouchMode(true);
			view.setBackgroundColor(
					ContextCompat.getColor(getContext(), R.color.default_background));
			view.setTextColor(Color.WHITE);
			view.setGravity(Gravity.CENTER);
			return new ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(ViewHolder viewHolder, Object item) {
			((TextView) viewHolder.view).setText((String) item);
		}

		@Override
		public void onUnbindViewHolder(ViewHolder viewHolder) {
		}
	}

}