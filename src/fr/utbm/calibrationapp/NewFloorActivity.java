package fr.utbm.calibrationapp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import fr.utbm.calibrationapp.model.Floor;
import fr.utbm.calibrationapp.utils.NetworkUtils;

public class NewFloorActivity extends Activity {
	final int ACTIVITY_CHOOSE_FILE = 1;
	private static final String DEBUG_TAG = "NEW_FLOOR";

	private SharedPreferences sp;
	private Bundle bundle;
	private EditText editFloorFile;
	private EditText editFloorName;
	private Button buttonChooseFile;
	private Button buttonAddFloor;
	private Button buttonCancel;
	private Uri imageFile = null;
	private Bitmap bitmap;
	private Integer buildingId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_floor_new);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		bundle = getIntent().getExtras();

		buildingId = bundle.getInt("building_id");

		editFloorFile = (EditText) findViewById(R.id.floorFile);
		editFloorName = (EditText) findViewById(R.id.floorName);

		buttonChooseFile = (Button) findViewById(R.id.floorChooseFile);
		buttonChooseFile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent chooseFile;
				Intent intent;
				chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
				chooseFile.setType("image/*");
				intent = Intent.createChooser(chooseFile, "Choose a file");
				startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
			}
		});

		buttonAddFloor = (Button) findViewById(R.id.floorAddNew);
		buttonAddFloor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (imageFile != null) {
					new ImageUploadTask().execute(new Floor(0, editFloorName.getText().toString(), 0));
				}
			}
		});

		buttonCancel = (Button) findViewById(R.id.floorAddCancel);
		buttonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent returnIntent = new Intent();
				setResult(RESULT_CANCELED, returnIntent);
				finish();
			}
		});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent upIntent = NavUtils.getParentActivityIntent(this);
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities(bundle);
			} else {
				NavUtils.navigateUpTo(this, upIntent);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTIVITY_CHOOSE_FILE: {
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				imageFile = Uri.parse(getRealPathFromUri(NewFloorActivity.this, uri));
				editFloorFile.setText(imageFile.getPath());
			}
		}
		}
	}

	public static String getRealPathFromUri(Activity activity, Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = activity.managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public void sendResultToParentActivity(String newFloor) {
		Intent returnIntent = new Intent();
		returnIntent.putExtra("jsonNewFloor", newFloor);
		returnIntent.putExtra("imageFile", imageFile);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	private class ImageUploadTask extends AsyncTask<Floor, Void, String> {
		private Floor newFloor;

		@Override
		protected String doInBackground(Floor... params) {
			try {
				newFloor = params[0];
				HttpClient httpClient = new DefaultHttpClient();
				HttpContext localContext = new BasicHttpContext();
				
				Drawable d = Drawable.createFromPath(imageFile.getPath());
				
				Log.d("IMAGE_UPLOAD", "http://" + sp.getString("serverAddress", "192.168.1.1") + ":" + sp.getString("serverPort", "80") + "/server/buildings/" + buildingId + "/addMap?name=" + newFloor.getName().replace(" ", "%20") + "&pxWidth=" + d.getIntrinsicWidth() + "&pxHeight=" + d.getIntrinsicHeight());
				HttpPost httpPost = new HttpPost("http://" + sp.getString("serverAddress", "192.168.1.1") + ":" + sp.getString("serverPort", "80") + "/server/buildings/" + buildingId + "/addMap?name=" + newFloor.getName().replace(" ", "%20") + "&pxWidth=" + d.getIntrinsicWidth() + "&pxHeight=" + d.getIntrinsicHeight());
				MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				File file = new File(imageFile.getPath());
				Log.d("IMAGE_UPLOAD", imageFile.getPath());
				ContentBody cbFile = new FileBody(file, "image/jpg");
				entity.addPart("image", cbFile);
				
				httpPost.setEntity(entity);
				Log.d("IMAGE_UPLOAD", "Executing request...");
				HttpResponse response = httpClient.execute(httpPost, localContext);
				Log.d("IMAGE_UPLOAD", "Request executed !");
				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
				String sResponse = reader.readLine();
				String s;
				while ((s = reader.readLine()) != null) {
					sResponse = sResponse + s;
				}
				Log.d("IMAGE_UPLOAD", sResponse);
				return sResponse;
			} catch (Exception e) {
				Log.e(e.getClass().getName(), e.getMessage(), e);
				return NetworkUtils.UNABLE_TO_CONTACT_SERVER;
			}
		}

		@Override
		protected void onProgressUpdate(Void... unsued) {

		}

		@Override
		protected void onPostExecute(String sResponse) {
			try {
				if (sResponse != null) {
					JSONObject response = new JSONObject(sResponse);
					if (!response.getBoolean("success")) {
						Toast.makeText(getApplicationContext(), response.getString("exception"), Toast.LENGTH_LONG).show();
					} else {
						sendResultToParentActivity(sResponse);
					}
				}
			} catch (Exception e) {
				Log.e(e.getClass().getName(), e.getMessage(), e);
			}
		}
	}

}
