package com.cw.tvclient;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Toast;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * splash screen
 */
public class SplashScreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		// show splash screen
		Thread timerThread = new Thread(){
			public void run(){
				try{
					sleep(1000);
				}catch(InterruptedException e){
					e.printStackTrace();
				}finally{
					// check server connection
					CheckHttpsConnection checkConnTask = new CheckHttpsConnection();
					checkConnTask.execute();
					while(!checkConnTask.checkIsReady)
						SystemClock.sleep(1000);

					if(checkConnTask.connIsOK) {
						// launch
						Intent intent = new Intent(SplashScreen.this,MainActivity.class);
						startActivity(intent);
					} else {
						// exit
						runOnUiThread(new Runnable() {
							public void run() {
								// exit
								Toast.makeText(SplashScreen.this,"Network connection failed.",Toast.LENGTH_LONG).show();
							}
						});
						finish();
					}
				}
			}
		};
		timerThread.start();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}

	class CheckHttpsConnection extends AsyncTask<Void,Integer,Void>
	{
		int code = -1;
		boolean checkIsReady;
		boolean connIsOK;

		@Override
		protected Void doInBackground(Void... voids) {
			checkIsReady = false;
			connIsOK = false;

			// HTTPS POST
			String project = "LiteNote";
			String urlStr =  "https://" + project + ".ddns.net:8443/"+ project +"Web/client/viewNote_json.jsp";

			try {
				URL url = new URL(urlStr);
				MovieList.trustEveryone();
				HttpsURLConnection connection = ((HttpsURLConnection) url.openConnection());
				connection.connect();
				code = connection.getResponseCode();
				System.out.println("SplashScreen / _doInBackground / code = " + code);
				if (code == 200) {
					// reachable
					checkIsReady = true;
					connIsOK = true;
				} else if(code == 404) {
					checkIsReady = true;
					connIsOK = false;
				}
				connection.disconnect();
			}catch (Exception e)
			{
				// connection refused
				checkIsReady = true;
				connIsOK = false;
				e.printStackTrace();
			}
			return null;
		}
	}

}