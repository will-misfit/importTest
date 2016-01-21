package com.misfit.ble.shine.network;

import com.misfit.ble.encryption.TextEncryption;
import com.misfit.ble.shine.log.LogManager;
import com.misfit.ble.util.Helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HttpsURLConnection;

public class PostTask {

	// FIXME: this task should run on its own background Thread.
	
	public static final int DEFAULT_RESPONSE_CODE = -1;
	public static final int CONNECTION_TIMEOUT = 10000;
	public static final int READ_TIMEOUT = 15000;
	
	/*package*/ int mNumberOfRetriesLeft = 0;
	
	/*package*/ String mURL;
	/*package*/ String mAccessKey;
	/*package*/ File mFile;
	
	public class PostTaskResult {
		public String mFilename;
		public String mResponseMessage;
		public int mResponseCode;
		
		public PostTaskResult() {
			mResponseCode = DEFAULT_RESPONSE_CODE;
		}
		
		public boolean hasSucceeded() {
			return mResponseCode == HttpURLConnection.HTTP_OK;
		}
	}
	
	public PostTask(final String url, final String accessKey, final File file) {
		super();
		mURL = url;
		mAccessKey = accessKey;
		mFile = file;
	}

	public PostTaskResult execute() {
		PostTaskResult result = null;
		do {
			result = postData();
			if (result.hasSucceeded())
				break;
			
			mNumberOfRetriesLeft -= 1;
		}  while (mNumberOfRetriesLeft > 0);
		
		return result;
	}
	
	private PostTaskResult postData() {
		String filename = mFile.getName();
		
		PostTaskResult result = new PostTaskResult();
		result.mFilename = filename;
		
		HttpsURLConnection httpConnection = null;
		try {
			String accessKeyId = mAccessKey;
			
			String content = Helper.readTextFile(mFile);
			String decryptedContent = TextEncryption.decrypt(content);
			if (decryptedContent == null) {
				// NOTE: backward compatible. Old version doesn't encrypt data.
				decryptedContent = content;				
			}
			
			// NOTE: add sessionId to request (if existing). 
			decryptedContent = LogManager.getDefault().addSessionIdToPostRequest(filename, decryptedContent);
			
			byte[] data = decryptedContent.getBytes("UTF-8");
			
			URL obj = new URL(mURL);
			httpConnection = (HttpsURLConnection) obj.openConnection();
			 
			// add request header
			httpConnection.setRequestMethod("POST");
			httpConnection.setRequestProperty("Content-Type", "application/json");  
			httpConnection.setRequestProperty("Content-Encoding", "gzip");
			httpConnection.setRequestProperty("access_key_id", accessKeyId);
			httpConnection.setConnectTimeout(CONNECTION_TIMEOUT);
			httpConnection.setReadTimeout(READ_TIMEOUT);
			
			// send post request
			httpConnection.setDoOutput(true);
			httpConnection.setChunkedStreamingMode(0);
			
			GZIPOutputStream outStream = new GZIPOutputStream(httpConnection.getOutputStream());
			outStream.write(data);
			outStream.flush();
			outStream.close();
			
			result.mResponseCode = httpConnection.getResponseCode();
			
			InputStream inputStream;
			if (result.mResponseCode == HttpURLConnection.HTTP_OK) {
			    inputStream = httpConnection.getInputStream();
			} else {
			    inputStream = httpConnection.getErrorStream();
			}
			result.mResponseMessage = (inputStream != null) ? getStringFromInputStream(inputStream) : null;
			
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			System.out.println("SocketTimeoutException is caught, not print the whole stack trace");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (httpConnection != null) {
				httpConnection.disconnect();
			}
		}
		return result;
	}
	
	private static String getStringFromInputStream(InputStream is) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();
	}
}
