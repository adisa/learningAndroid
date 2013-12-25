package com.acme.yamba;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import winterwell.jtwitter.OAuthSignpostClient;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
//import android.view.Menu;

public class StatusActivity1 extends Activity{
		
		private static final String TAG = "StatusActivity";
		
		private static final String OAUTH_KEY = "ntUNY2o4mUGazQFPbFgLQ";
		private static final String OAUTH_SECRET = "Y5O9Br9BTYxtUsv8owiW00znlyWrFMciH1HybQE4PGs";
		
		private static final String OAUTH_CALLBACK_SCHEME = "x-marakana-oauth-twitter";
		private static final String OAUTH_CALLBACK_URL = "";
		
		
		private static final String TWITTER_USER = "somardk@gmail.com";
		private static final String TOKEN = "1956878546-SxxwR4FwXi0JMXJ7ZgYRJX6pQWj1tkThxDo9MF9";
		private static final String TOKEN_SECRET = "6pkuTg6bPBVToNsOw6JWb28jnS39D9GI68u2MIquJqXEn";
		
		EditText editText;
		Button updateButton;
		Twitter twitter;
		
		private OAuthSignpostClient oauthClient;
		private OAuthConsumer mConsumer;
		private OAuthProvider mProvider;
		SharedPreferences prefs;

		
		//@SuppressWarnings("deprecation")
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.status);
		//	Toast.makeText(StatusActivity1.this, "onCreate", Toast.LENGTH_LONG).show();
			
			editText= (EditText) findViewById(R.id.editText);
			updateButton =( Button) findViewById(R.id.buttonUPdate);
			
			mConsumer = new CommonsHttpOAuthConsumer(OAUTH_KEY, OAUTH_SECRET);
		    mProvider = new DefaultOAuthProvider(
		        "https://api.twitter.com/oauth/request_token",
		        "https://api.twitter.com/oauth/access_token",
		        "https://api.twitter.com/oauth/authorize");
		    mConsumer.setTokenWithSecret(TOKEN, TOKEN_SECRET);
			
			oauthClient = new OAuthSignpostClient(
												OAUTH_KEY, 
												OAUTH_SECRET,
												TOKEN,
												TOKEN_SECRET);
			
			twitter = new Twitter(TWITTER_USER, oauthClient);

		}
		
		/* Callback once we are done with the authorization of this app with Twitter. */
		@Override
		public void onNewIntent(Intent intent){
			super.onNewIntent(intent);
			Log.d(TAG, "intent: " + intent);
			
			Uri uri = intent.getData();
			if (uri != null){
				Log.d(TAG, "callback: "+ uri.getPath());
				
				String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
				Log.d(TAG, "verifier: " + verifier);
				
				new RetrieveAccessTokenTask().execute(verifier);
			}
		}
		
		public void onClickAuthorize(View view){
			new OAuthAuthorizeTask().execute();
		}
		
		public void onClickTweet(View view){
			if (twitter == null){
				Toast.makeText(this, "Authenticate first", Toast.LENGTH_LONG).show();
				return;
			}
			
			new PostStatusTask().execute(editText.getText().toString());
		}
		
		public void onClickGetStatus(View view){
			if (twitter == null){
				Toast.makeText(this, "Authenticate first", Toast.LENGTH_LONG).show();
				return;
			}
			
			new GetStatusTask().execute();
		}

		/* Responsible for starting the Twitter authorization */
		class OAuthAuthorizeTask extends AsyncTask<Void, Void, String>{

			@Override
			protected String doInBackground(Void... params) {
				String authUrl;
			    String message = null;
			    
			    try{
			    	authUrl = mProvider.retrieveRequestToken(mConsumer, OAUTH_CALLBACK_URL);
			    	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
			    	startActivity(intent);
			    }catch (OAuthMessageSignerException e){
			    	message = "OAuthMessageSignerException";
			        e.printStackTrace();
			    }catch (OAuthNotAuthorizedException e) {
			        message = "OAuthNotAuthorizedException";
			        e.printStackTrace();
			    } catch (OAuthExpectationFailedException e) {
			        message = "OAuthExpectationFailedException";
			        e.printStackTrace();
			    } catch (OAuthCommunicationException e) {
			        message = "OAuthCommunicationException";
			        e.printStackTrace();
			    }
				return message;
			}
			
			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				if(result != null){
					Toast.makeText(StatusActivity1.this, result, Toast.LENGTH_LONG).show();
				}
			}
			
		}
		
		/* Responsible for retrieving access tokens from twitter */
		class RetrieveAccessTokenTask extends AsyncTask<String, Void, String>{

			@Override
			protected String doInBackground(String... params) {
				String message = null;
			    String verifier = params[0];
				try {
					Log.d(TAG, "mConsumer: " + mConsumer);
			        Log.d(TAG, "mProvider: " + mProvider);
			        
			        mProvider.retrieveAccessToken(mConsumer, verifier);
			        String token = mConsumer.getToken();
			        String tokenSecret = mConsumer.getTokenSecret();
			        mConsumer.setTokenWithSecret(token, tokenSecret);
			        
			        Log.d(TAG, String.format("verifier: %s, token: %s, tokenSecret: %s",
			                verifier, token, tokenSecret));
			        //make a Twitter object
			        oauthClient = new OAuthSignpostClient(OAUTH_KEY, OAUTH_SECRET, token, tokenSecret);
			        twitter = new Twitter("somardk", oauthClient);
			        
			        Log.d(TAG, "token: "+token);
				} catch (OAuthMessageSignerException e) {
			        message = "OAuthMessageSignerException";
			        e.printStackTrace();
			      } catch (OAuthNotAuthorizedException e) {
			        message = "OAuthNotAuthorizedException";
			        e.printStackTrace();
			      } catch (OAuthExpectationFailedException e) {
			        message = "OAuthExpectationFailedException";
			        e.printStackTrace();
			      } catch (OAuthCommunicationException e) {
			        message = "OAuthCommunicationException";
			        e.printStackTrace();
			      }
			    return message;
			}
			
			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				if(result != null){
					Toast.makeText(StatusActivity1.this, result, Toast.LENGTH_LONG).show();
				}
			}
			
		}
		
		/* Responsible for getting Twitter status */
		class GetStatusTask extends AsyncTask<Void,Void,String> {

			@Override
			protected String doInBackground(Void... params) {
				return twitter.getStatus().text;
			}
			
			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				if(result != null){
					Toast.makeText(StatusActivity1.this, result, Toast.LENGTH_LONG).show();
				}
			}
			
		}
		
		/* Responsible for posting new status to Twitter */
		class PostStatusTask extends AsyncTask<String, Void, String> {

			@Override
			protected String doInBackground(String... params) {
				try {
					twitter.setStatus(params[0]);
					return "Successfully posted: " + params[0];
				} catch (TwitterException e) {
					e.printStackTrace();
					return "Error connecteing to server.";
				}
			}
			
			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				if(result != null){
					Toast.makeText(StatusActivity1.this, result, Toast.LENGTH_LONG).show();
				}
			}
			
		}
		

}
