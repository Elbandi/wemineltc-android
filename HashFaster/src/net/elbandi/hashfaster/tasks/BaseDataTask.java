package net.elbandi.hashfaster.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import net.elbandi.hashfaster.interfaces.RefreshListener;
import net.elbandi.hashfaster.managers.PrefManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public abstract class BaseDataTask extends AsyncTask<String, Void, JSONObject> {
	private static final String BASEURL = "http://ltc.hashfaster.com/index.php?page=api&action=%s&api_key=%s";

	Context mContext;
	RefreshListener mListener;

	public BaseDataTask(Context context, RefreshListener listener) {
		mContext = context;
		mListener = listener;
	}

	protected JSONObject DoRequest(String action) {
		InputStream is = null;
		String JSONString = "";
		JSONObject result = new JSONObject();

		try {
			String mURL = String.format(BASEURL, action, PrefManager.getAPIKey(mContext));
			Log.v("HASHFASTER", "DoRequest: url is + " + mURL);

			HttpGet httpPost = new HttpGet(mURL);
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			setError("Error: No connection, check your internet!");
			e.printStackTrace();
		} catch (IOException e) {
			setError("Error: No connection, check your internet!");
			e.printStackTrace();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			JSONString = sb.toString();
			Log.v("HASHFASTER", "DoRequest: JSONString is:\n" + JSONString);
			result = new JSONObject(JSONString);
		} catch (Exception e) {
			setError("Error: Invalid API Key!");
		}

		Log.v("HASHFASTER", "DoRequest: result:\n" + result.toString());
		return result;
	}

	@Override
	protected void onPostExecute(JSONObject result) {
		if (mListener != null && result != null)
			mListener.onRefresh();
	}

	protected void setError(String error) {

	}

}
