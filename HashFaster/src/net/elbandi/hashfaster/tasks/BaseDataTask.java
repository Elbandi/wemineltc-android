package net.elbandi.hashfaster.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import net.elbandi.hashfaster.ApiKeyException;
import net.elbandi.hashfaster.interfaces.RefreshListener;
import net.elbandi.hashfaster.managers.PoolManager;
import net.elbandi.hashfaster.managers.PrefManager;
import net.elbandi.hashfaster.utils.StringUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public abstract class BaseDataTask extends AsyncTask<String, Void, JSONObject> {
	private static final String BASEURL = "%s/index.php?page=api&action=%s&api_key=%s";

	Context mContext;
	RefreshListener mListener;
	String mKey;

	public BaseDataTask(Context context, RefreshListener listener, String key) {
		mContext = context;
		mListener = listener;
		mKey = key;
	}

	protected JSONObject DoRequest(String action) {
		InputStream is = null;
		String JSONString = "";
		JSONObject result = new JSONObject();

		try {
			String mURL = String.format(BASEURL, PoolManager.getPoolUrl(mKey), action, PrefManager.getAPIKey(mContext, mKey));
			Log.d("HASHFASTER", "DoRequest: url is + " + mURL);

			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is
			// established.
			// The default value is zero, that means the timeout is not used.
			HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
			// Set the default socket timeout (SO_TIMEOUT)
			// in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(httpParameters, 15000);

			HttpGet httpPost = new HttpGet(mURL);
			DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

		} catch (UnsupportedEncodingException e) {
			setError("Invalid response format");
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			setError("Error: No connection, check your internet!");
			e.printStackTrace();
		} catch (IOException e) {
			setError("Error: No connection, check your internet!");
			e.printStackTrace();
		}

		if (is != null) {
			try {
				JSONString = StringUtils.readAll(is, "UTF-8");
				Log.d("HASHFASTER", "DoRequest: JSONString is:\n" + JSONString);
				if ("Access denied".equals(JSONString)) {
					throw new ApiKeyException();
				}
				if (JSONString != null) {
					while (JSONString.length() > 0 && JSONString.charAt(0) != '{')
						JSONString = JSONString.substring(1);
				}
				result = new JSONObject(JSONString);
			} catch (ApiKeyException e) {
				setError("Error: Invalid API Key!");
			} catch (Exception e) {
				setError("Invalid response format");
			}
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
