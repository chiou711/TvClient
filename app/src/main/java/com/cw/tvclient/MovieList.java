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

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public final class MovieList {
	public static final String MOVIE_CATEGORY[] = {
			"Category Zero",
			"Category One",
			"Category Two",
			"Category Three",
			"Category Four",
			"Category Five",
	};

	static List<Movie> list;
	private static long count = 0;

	public static List<Movie> getList() {
		if (list == null) {
			list = setupMovies();
		}
		return list;
	}

	public static List<Movie> setupMovies() {

//		prepareList();

		list = new ArrayList<>();

		String title[] = Title;
//		String title[] = {
//				"Zeitgeist 2010_ Year in Review",
//				"Google Demo Slam_ 20ft Search",
//				"Introducing Gmail Blue",
//				"Introducing Google Fiber to the Pole",
//				"Introducing Google Nose"
//		};

		String description = "Fusce id nisi turpis. Praesent viverra bibendum semper. "
				+ "Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est "
				+ "quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit "
				+ "amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit "
				+ "facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id "
				+ "lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.";

		String studio[] = new String[Id.length];
		for(int i=0;i< Id.length;i++)
			studio[i] = String.valueOf(i);

//		String studio[] = {
//				"Studio Zero", "Studio One", "Studio Two", "Studio Three", "Studio Four"
//		};

		String videoUrl[] = Uri;
//		String videoUrl[] = {
//				"http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review.mp4",
//				"http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search.mp4",
//				"http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue.mp4",
//				"http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole.mp4",
//				"http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose.mp4"
//		};

		String bgImageUrl[] = new String[Id.length];
		for(int i=0;i< Id.length;i++)
			bgImageUrl[i] =  "http://img.youtube.com/vi/"+getYoutubeId(Uri[i])+"/0.jpg";
//		String bgImageUrl[] = {
//				"http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review/bg.jpg",
//				"http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/bg.jpg",
//				"http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue/bg.jpg",
//				"http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/bg.jpg",
//				"http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose/bg.jpg",
//		};

		String cardImageUrl[] = new String[Id.length];
		for(int i=0;i< Id.length;i++)
			cardImageUrl[i] =  "http://img.youtube.com/vi/"+getYoutubeId(Uri[i])+"/0.jpg";

//		String cardImageUrl[] = {
//				"http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review/card.jpg",
//				"http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/card.jpg",
//				"http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue/card.jpg",
//				"http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/card.jpg",
//				"http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose/card.jpg"
//		};

		System.out.println("MovieList / title.length = " + title.length);
		System.out.println("MovieList / studio.length = " + studio.length);
		System.out.println("MovieList / videoUrl.length = " + videoUrl.length);
		System.out.println("MovieList / cardImageUrl.length = " + cardImageUrl.length);
		System.out.println("MovieList / bgImageUrl.length = " + bgImageUrl.length);
		for (int index = 0; index < title.length; ++index) {
			list.add(
					buildMovieInfo(
							title[index],
							description,
							studio[index],
							videoUrl[index],
							cardImageUrl[index],
							bgImageUrl[index]));
		}

		return list;
	}

	private static Movie buildMovieInfo(
			String title,
			String description,
			String studio,
			String videoUrl,
			String cardImageUrl,
			String backgroundImageUrl) {
		Movie movie = new Movie();
		movie.setId(count++);
		movie.setTitle(title);
		movie.setDescription(description);
		movie.setStudio(studio);
		movie.setCardImageUrl(cardImageUrl);
		movie.setBackgroundImageUrl(backgroundImageUrl);
		movie.setVideoUrl(videoUrl);
		return movie;
	}


	///
	static int [] Id;
	static String [] Uri;
	static String [] Title;
	static boolean isDataReady;

	static void prepareList()
	{
		MyTask task = new MyTask();
		task.execute();
	}

	private static class MyTask extends AsyncTask<Void,Void,Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			System.out.println("MainActivity / MyTask /_doInBackground");

			String strResult = "";
			isDataReady = false;

			// HTTPS POST
			String project = "Project";
			String urlStr =  "https://" + project + ".ddns.net:8443/"+ project +"Web/viewNote/viewNote_json.jsp";
			//refer https://stackoverflow.com/questions/16504527/how-to-do-an-https-post-from-android
			try {
				URL url = new URL(urlStr);
				trustEveryone();
				HttpsURLConnection urlConnection = ((HttpsURLConnection)url.openConnection());

				// set Timeout and method
				urlConnection.setReadTimeout(7000);
				urlConnection.setConnectTimeout(7000);
				urlConnection.setRequestMethod("POST");
				urlConnection.setDoInput(true);

				// Add any data you wish to post here
				urlConnection.connect();
				InputStream in = urlConnection.getInputStream();

				if(in != null) {
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					String inputLine;

					while ((inputLine = br.readLine()) != null) {
						System.out.println("MainActivity / inputLine = " + inputLine);
						strResult += inputLine;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("MainActivity / result final = " + strResult);

			// JSON array
			try {
				JSONArray jsonArray = new JSONArray(strResult);

				//Set Array size
				Id = new int[jsonArray.length()];
				Uri = new String[jsonArray.length()];
				Title = new String[jsonArray.length()];

				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = (JSONObject) jsonArray.get(i);
					Id[i] = jsonObject.getInt("note_id");
					Uri[i] = jsonObject.getString("note_link_uri");
					Title[i] = jsonObject.getString("note_title");
				}

				isDataReady = true;
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
//			isDataReady = true;
		}
	}

	private static void trustEveryone() {
		try {
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}});

			SSLContext context = SSLContext.getInstance("TLS");

			context.init(null, new X509TrustManager[]
							{
									new X509TrustManager()
									{
										public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
										{}

										public void checkServerTrusted(X509Certificate[] chain,String authType) throws CertificateException
										{}

										public X509Certificate[] getAcceptedIssuers() {
											return new X509Certificate[0];
										}
									}
							},
					new SecureRandom()
			);

			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

		} catch (Exception e) { // should never happen
			e.printStackTrace();
		}
	}

	// Get YouTube Id
	public static String getYoutubeId(String url) {

		String videoId = "";

		if (url != null && url.trim().length() > 0 && url.startsWith("http")) {
			String expression = "^.*((youtu.be\\/)|(v\\/)|(\\/u\\/w\\/)|(embed\\/)|(watch\\?))\\??(v=)?([^#\\&\\?]*).*";
			CharSequence input = url;
			Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String groupIndex1 = matcher.group(8);
				if (groupIndex1 != null && groupIndex1.length() == 11)
					videoId = groupIndex1;
			}
		}
		return videoId;
	}
	///

}