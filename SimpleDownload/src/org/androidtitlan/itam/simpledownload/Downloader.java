package org.androidtitlan.itam.simpledownload;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class Downloader extends IntentService {
public static final String EXTRA_MESSENGER="org.androidtitlan.itam.simpledownload.EXTRA_MESSENGER";
private HttpClient client=null;

public Downloader() {
	super("Downloader");
}

@Override
public void onCreate() {
	super.onCreate();
	client=new DefaultHttpClient();
}

@Override
public void onDestroy() {
	super.onDestroy();
	client.getConnectionManager().shutdown();
}

@Override	
public void onHandleIntent(Intent i) {
	HttpGet getMethod=new HttpGet(i.getData().toString());
	int result=Activity.RESULT_CANCELED;
	
	try {
		ResponseHandler<byte[]> responseHandler=new ByteArrayResponseHandler();
		byte[] responseBody=client.execute(getMethod, responseHandler);
		File output=new File(Environment.getExternalStorageDirectory(),
												i.getData().getLastPathSegment());
		
		if (output.exists()) {
			output.delete();
		}
		
		FileOutputStream fos=new FileOutputStream(output.getPath());
		
		fos.write(responseBody);
		fos.close();
		result=Activity.RESULT_OK;
	}
	catch (IOException e2) {
		Log.e(getClass().getName(), "Exception in download", e2);
	}
	
	Bundle extras=i.getExtras();

	if (extras!=null) {
		Messenger messenger=(Messenger)extras.get(EXTRA_MESSENGER);
		Message msg=Message.obtain();
		
		msg.arg1=result;
		
		try {
			messenger.send(msg);
		}
		catch (android.os.RemoteException e1) {
			Log.w(getClass().getName(), "Exception sending message", e1);
		}
	}
}
}