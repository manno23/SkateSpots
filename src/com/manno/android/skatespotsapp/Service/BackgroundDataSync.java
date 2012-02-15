package com.manno.android.skatespotsapp.Service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import com.manno.android.skatespotsapp.SkateSpotSession;
import com.manno.android.skatespotsapp.Storage.MyDB;
import com.manno.android.skatespotsapp.Utils.Spot;
import com.manno.android.skatespotsapp.Utils.SpotPicture;
import com.manno.android.skatespotsapp.ViewSpot.ViewSpotActivity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class BackgroundDataSync extends IntentService {

	//Background Tasks
    public static final byte SPOT_REFRESH = 0x1;
    public static final byte UPLOAD_PICTURE = 0x2;
    public static final byte UPLOAD_SPOT = 0x3;
    public static final byte RATE_SPOT = 0x4;
    public static final byte GET_SPOT_PHOTO_COMMENTS = 0x5;
    public static final byte USERS_NAME_SERVER_REFRESH = 0x6;
	public static final byte SPOT_PHOTOS_DOWNLOAD = 0x7;
	public static final byte DELETE_SPOT = 0x8;
	public static final byte DELETE_PICTURE = 0x9;
	public static final byte UPLOAD_COMMENT = 0x10;
	public static final byte DELETE_COMMENT = 0x11;
    public static final byte REFRESH_NEWS_ITEMS = 0x12;

    public static final int STATUS_RUNNING = 0x1;
    public static final int STATUS_ERROR = 0x2;
    public static final int STATUS_FINISHED = 0x3;
    public static final int STATUS_DBINUSE = 0x4;
    public static final int SPOT_UPLOAD_ERROR = 0x5;
    public static final int SPOT_UPLOAD_FINISHED = 0x6;

    

    static final String SERVER_ADDRESS = "http://www.therealskatespot.com/";
    private HttpClient httpClient;
    private long uid;
    
    private static final String TAG = "BackgroundDataSync";


    public BackgroundDataSync() {
		super("Tasks_Service");
	}

	@Override public void onCreate() {
		super.onCreate();
		SkateSpotSession session = (SkateSpotSession)getApplicationContext();
		httpClient = session.getHttpClient();
		//uid = session.getUid();
	}

	@Override protected void onHandleIntent(Intent intent) {

		Bundle task = intent.getExtras();
		int bgService =	task.getByte("action");
		ResultReceiver helper = task.getParcelable("callback");
		
		switch(bgService) {
			case SPOT_REFRESH:
				spotRefresh(helper);
				break;
//			case UPLOAD_PICTURE:
//				pictureUpload(task.getString("path"),
//							  task.getInt("SpotID"),
//							  task.getLong("pictureID"),
//							  helper);
//				break;
//			case RATE_SPOT:
//				rateSpot(task.getInt("user_rating"),
//						 (Spot)task.getParcelable("spot"),
//						 helper);
//				break;
			case UPLOAD_SPOT:
				uploadSpot(task.getString("name"),
						   task.getString("description"),
						   task.getDouble("longitude"), task.getDouble("latitude"),
						   task.getInt("share"),
						   helper);
				break;
//			case UPLOAD_COMMENT:
//				uploadComment(task.getLong("pictureID"),
//							  task.getString("comment"),
//							  task.getString("creationDate"),
//							  helper);
//				break;
//			case DELETE_COMMENT:
//				deleteComment(task.getLong("commentID"),
//							  task.getLong("pictureID"),
//							  helper);
			case SPOT_PHOTOS_DOWNLOAD:
				retrieveSpotPictures(task.getInt("sid"),
									 helper);
				break;
//			case USERS_NAME_SERVER_REFRESH:
//				refreshUsersNameOnServer(task.getLong("uid"), task.getString("name"));
//				break;
//			case DELETE_SPOT:
//				userDeleteSpot(task.getInt("spotID"),
//							   helper);
//				break;
//			case DELETE_PICTURE:
//				userDeletePicture(task.getLong("pictureID"),
//								  helper);
//				break;
//			case GET_SPOT_PHOTO_COMMENTS:
//				retrievePictureComments(task.getLong("pictureID"),
//										helper);
//				break;*/
            case REFRESH_NEWS_ITEMS:
                retrieveNewsItems(helper);
			default:
				break;
		}
	}

    private void spotRefresh(ResultReceiver helper) {

        helper.send(STATUS_RUNNING, Bundle.EMPTY);
        try {

            String friendIDs = "1";
/*            Facebook fb = ((SkateSpotSession)getApplicationContext()).getFacebook();

            if(((SkateSpotSession)getApplicationContext()).getFacebook().isSessionValid()) {
                String friendList = fb.request("me/friends");
                if(friendList == null)
                    throw new NullPointerException();
                JSONObject json = new JSONObject(friendList);
                JSONArray friends = json.getJSONArray("data");
                for(int i = 0; i<friends.length();i++) {
                    friendIDs = friendIDs + "," + friends.getJSONObject(i).getLong("id");
                }
            }*/
            MultipartEntity reqEntity = new MultipartEntity();
            reqEntity.addPart("userID", new StringBody(String.valueOf(uid)));
            reqEntity.addPart("friends", new StringBody(friendIDs));
            //Prepare HttpPost to request spotlist
            String postURL = SERVER_ADDRESS+"get.php";
            HttpPost post = new HttpPost(postURL);
            post.addHeader("Accept-Encoding","compress, gzip");
            post.setEntity(reqEntity);
            HttpResponse response = httpClient.execute(post);
            if(response.getStatusLine().getStatusCode() != 200) {
                throw new Exception();
            }

            //Decode response
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream is = entity.getContent();
                GZIPInputStream gzipIn = new GZIPInputStream(is);
                BufferedReader reader = new BufferedReader(new InputStreamReader(gzipIn));
                StringBuilder output = new StringBuilder();
                String s = null;
                while ((s = reader.readLine()) != null) {
                    output.append(s+ "\n");
                    Log.d(TAG, s);
                }

                MyDB db = new MyDB(this);
                JSONObject json = new JSONObject(output.toString());
                JSONArray ratings = json.getJSONArray("ratings");
                JSONArray spots = json.getJSONArray("spots");
                helper.send(STATUS_DBINUSE, Bundle.EMPTY);
                db.open();
                db.clear();
                db.close();

                db.open();
                JSONObject rating = null;
                for(int i = 0; i<ratings.length();i++) {
                    rating = ratings.getJSONObject(i);
                    Log.d(TAG, rating.getInt("SID") + " " + rating.getString("RATING"));
                    db.insertRating(rating.getInt("SID"),rating.getString("RATING"));
                }

                JSONObject spot = null;
                Log.d(TAG, "Spot array length is: " + spots.length());
                for(int i = 0; i<spots.length();i++) {
                    spot = spots.getJSONObject(i);
                    db.insertSpot(new Spot(spot.getInt("SID"), spot.getDouble("LATITUDE"), spot.getDouble("LONGITUDE"),
                            spot.getString("NAME"),	spot.getString("ADDRESS"),
                            (float)spot.getDouble("RATING"), spot.getLong("UID"), spot.getInt("SHARE"), spot.getInt("VOTE_TALLY"), spot.getInt("NO_OF_RATINGS"), spot.getString("DESCRIPTION")));
                    Log.d(TAG, spot.getString("NAME"));
                }
                db.close();
                helper.send(BackgroundDataSync.STATUS_FINISHED, Bundle.EMPTY);
            }



        } catch (Exception e) {
            Log.d(TAG, e.toString());
            helper.send(BackgroundDataSync.STATUS_ERROR, Bundle.EMPTY);
            e.printStackTrace();
        }
    }

//	private void deleteComment(long commentID, long pictureID, ResultReceiver helper) {
//
//		HttpGet get = new HttpGet(
//				SERVER_ADDRESS + "deleteComment.php?commentID="+commentID );
//		Log.d(TAG, get.getURI().toString());
//
//		HttpResponse response = null;
//		try {
//			response = httpClient.execute(get);
//			int responseCode = response.getStatusLine().getStatusCode();
//			InputStream in = response.getEntity().getContent();
//			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//			StringBuilder responseString = new StringBuilder();
//			String line = null;
//			while((line = reader.readLine()) != null) {
//				responseString.append(line);
//			}
//			Log.d(TAG, responseString.toString());
//
//			Bundle bundle = new Bundle();
//			bundle.putLong("pictureID", pictureID);
//			if(responseCode == 200) {
//				MyDB db = new MyDB(this);
//				if(db.deleteComment(commentID)) {
//					Log.d(TAG, "delete succesful");
//				} else
//					Log.d(TAG, "delete unsuccesful");
//				helper.send(PictureActivity.DELETE_COMMENT_SUCCESSFUL, bundle);
//			}
//			else helper.send(PictureActivity.DELETE_COMMENT_UNSUCCESSFUL, Bundle.EMPTY);
//		} catch (Exception e) {
//			helper.send(PictureActivity.DELETE_COMMENT_UNSUCCESSFUL, Bundle.EMPTY);
//			e.printStackTrace();
//		}
//	}
//
//	private void pictureUpload(String path, int spotID, long pictureID, ResultReceiver helper) {
//
//		*//*
//		 *TODO  -refactor filecache
//		 *		-design on responsibilities and collaborators
//		 *		 including ImageLoader, FileCache, LRUCache, BackgroundSync
//		 * 		-Terrible,  terrible design at the moment
//		 *//*
//
//        Log.d(TAG, "Image path: " + path);
//        File imageFile = new File(path);
//		HttpClient httpClient = new DefaultHttpClient();
//		HttpPost postRequest = new HttpPost(SERVER_ADDRESS+"imageupload.php");
//		MultipartEntity reqEntity = new MultipartEntity();
//
//		//Load Bitmap if it exists
//
//		if((imageFile != null)&&(imageFile.exists())) {
//			//retrieve jpg file from storage and create a byte array
//			try {
//				//Decode stream in manageable chunks
//				BitmapFactory.Options options = new BitmapFactory.Options();
//				options.inSampleSize = 4;
//				options.inTempStorage = new byte[1024];
//				options.inPurgeable = true;
//				InputStream fileInStream = new BufferedInputStream(new FileInputStream(imageFile), 1024*2);
//				Bitmap bm = BitmapFactory.decodeStream(fileInStream, null, options);
//				SoftReference<Bitmap> reference = new SoftReference<Bitmap>(bm);
//				ByteArrayOutputStream bos = new ByteArrayOutputStream();
//				reference.get().compress(CompressFormat.JPEG, 75, bos);
//				byte[] data = bos.toByteArray();
//				ByteArrayBody bab = new ByteArrayBody(data, String.valueOf(pictureID));
//				reqEntity.addPart("sid", new StringBody(String.valueOf(spotID)));
//				reqEntity.addPart("uid", new StringBody(String.valueOf(uid)));
//				reqEntity.addPart("creationDate", new StringBody(Utils.getCurrentUTCTime()));
//				reqEntity.addPart("image", bab);
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			}
//		} else {
//			helper.send(ViewSpotActivity.PHOTO_UPLOAD_NOT_SUCCESSFUL, Bundle.EMPTY);
//			Log.d(TAG, "Path: " + imageFile.getPath() + " does not exist");
//		}
//
//		postRequest.setEntity(reqEntity);
//		HttpResponse response;
//		try {
//			response = httpClient.execute(postRequest);
//			int responseStatus = response.getStatusLine().getStatusCode();
//			if(responseStatus == 200) {
//				helper.send(ViewSpotActivity.PHOTO_UPLOAD_SUCCESSFUL, Bundle.EMPTY);
//				BufferedReader reader = new BufferedReader(new InputStreamReader(
//					response.getEntity().getContent(), "UTF-8"));
//				String sResponse;
//				while ((sResponse = reader.readLine()) != null) {
//					Log.d(TAG, sResponse);
//				}
//			} else {
//				uploadFailAlert(helper);
//			}
//		} catch (ClientProtocolException e) {
//			uploadFailAlert(helper);
//			e.printStackTrace();
//		} catch (IOException e) {
//			uploadFailAlert(helper);
//			e.printStackTrace();
//		}
//
//	}
//
//	private void rateSpot(int userRating, Spot spot, ResultReceiver helper) {
//		int sid = spot.getSid();
//		Log.d(TAG, "Background Data sync recieved with" + " " + userRating);
//		try {
//			String rateSpotURL = SERVER_ADDRESS+"rate.php";
//			HttpPost post = new HttpPost(rateSpotURL);
//
//			//add values to the body
//			MultipartEntity reqEntity = new MultipartEntity();
//			reqEntity.addPart("spotID", new StringBody(String.valueOf(sid)));
//			String userID = String.valueOf(uid);
//			reqEntity.addPart("userID", new StringBody(userID));
//			String rating = "";
//			switch(userRating) {
//				case Constants.RATING_ENUM_LIKE:
//					Log.d(TAG, "Rating set as good");
//					rating = "GOOD";
//					break;
//				case Constants.RATING_ENUM_DISLIKE:
//					Log.d(TAG, "Rating set as shit");
//					rating = "SHIT";
//					break;
//			}
//			reqEntity.addPart("user_rating", new StringBody(rating));
//			post.setEntity(reqEntity);
//
//			//send off
//			HttpResponse response = httpClient.execute(post);
//			BufferedReader reader = new BufferedReader(new InputStreamReader(
//					response.getEntity().getContent()));
//			String sResponse = "";
//			while ((sResponse = reader.readLine()) != null) {
//				Log.d(TAG, sResponse);
//			}
//			MyDB db = new MyDB(this);
//			db.open();
//			switch(response.getStatusLine().getStatusCode()) {
//				case 201:
//					db.setRating(spot, userRating);
//					helper.send(ViewSpotActivity.RATING_SUCCESSFUL, Bundle.EMPTY);
//					break;
//				case 400:
//					helper.send(ViewSpotActivity.RATING_NOT_SUCCESSFUL, Bundle.EMPTY);
//					break;
//			}
//			db.close();
//
//		} catch (Exception e) {
//			Log.d(TAG, e.getMessage());
//			e.printStackTrace();
//		}
//
//	}
//
	private void uploadSpot(String spotName, String description, double longitude, double latitude, int share, ResultReceiver helper) {

		helper.send(BackgroundDataSync.STATUS_RUNNING, Bundle.EMPTY);
		HttpPost postRequest = new HttpPost(SERVER_ADDRESS + "add.php");
		MultipartEntity reqEntity = new MultipartEntity();

		try {
			reqEntity.addPart("name", new StringBody(spotName));
			reqEntity.addPart("desc", new StringBody(description));
			reqEntity.addPart("long", new StringBody(String.valueOf(longitude)));
			reqEntity.addPart("lat", new StringBody(String.valueOf(latitude)));
			reqEntity.addPart("share", new StringBody(String.valueOf(share)));
			reqEntity.addPart("uid", new StringBody(String.valueOf(uid)));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		try {

			postRequest.setEntity(reqEntity);
			HttpResponse response = httpClient.execute(postRequest);

			int responseStatus = response.getStatusLine().getStatusCode();
			Log.d(TAG, "Response Code: " + responseStatus);
			/* Outcomes:
			 *1. Spot is uploaded successfully   MSG 200[SpotID]
			 *		- insert new spot into db
			 *      - callback successful -> SpotMap
			 *2. Spot exists to me as a public    TODO //Handle this use case later
			 *3. Server Error  MSG 500 [whatever]
			 *		- Toast notification that server error occurred
			 *4. I/O or SQLite Exception thrown  TODO
			 *		- Try 1 more time, then
			 *		- Toast notification connection failure in case of I/O Exception
			*/

			if(responseStatus == 200) {

				// Read the response
				BufferedReader responseBodyReader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
				StringBuilder output = new StringBuilder();
				String sResponse;
				while ((sResponse = responseBodyReader.readLine()) != null) {
					output.append(sResponse);
				}
				Log.d(TAG, "Response body: " + output.toString());
				JSONObject json = new JSONObject(output.toString());
				int sid = json.getInt("sid");
				Log.d(TAG, "JSON gives me an sid value of: " + sid);

				// Insert newly updated spot into our db
				MyDB db = new MyDB(this);
				db.open();
				long insert = db.insertSpot(new Spot(sid, latitude, longitude, spotName, "", 2.0f, uid, share, 0, 0, description));
				db.close();
				Log.d(TAG, "DB insert returned: " + insert);

				// Callback with success
				helper.send(BackgroundDataSync.SPOT_UPLOAD_FINISHED, Bundle.EMPTY);
			} else {

				// Callback with failure
				helper.send(BackgroundDataSync.SPOT_UPLOAD_ERROR, Bundle.EMPTY);
			}
		} catch(Exception e) {
			helper.send(BackgroundDataSync.SPOT_UPLOAD_ERROR, Bundle.EMPTY);
		}
	}
    
    private void retrieveNewsItems(ResultReceiver helper) {

        //Create GET request
        HttpGet get = new HttpGet(SERVER_ADDRESS+"getNews.php");
        try {
            HttpResponse response = null;
            response = httpClient.execute(get);
            int responseStatus = response.getStatusLine().getStatusCode();
            Log.d(TAG, "Response Code: " + responseStatus);
            if(responseStatus == 200) {
                // Read the response
                BufferedReader responseBodyReader = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent(), "UTF-8"));
                StringBuilder output = new StringBuilder();
                String sResponse;
                while ((sResponse = responseBodyReader.readLine()) != null) {
                    output.append(sResponse);
                }
                Log.d(TAG, "Response body: " + output.toString());
                JSONArray newsItemArray = new JSONArray(output.toString());
                for(int i = 0; i < newsItemArray.length(); i++) {
                    JSONObject json = newsItemArray.getJSONObject(i);
                    // Insert news into our db
                    MyDB db = new MyDB(this);
                    db.insertNewsItem(json.getInt("news_item_id"),
                                      json.getString("creation_date"),
                                      json.getInt("client_id"),
                                      json.getString("link_url"),
                                      json.getString("image_url"));
                }
                helper.send(BackgroundDataSync.SPOT_UPLOAD_FINISHED, Bundle.EMPTY);
            } else {
                helper.send(BackgroundDataSync.SPOT_UPLOAD_ERROR, Bundle.EMPTY);
            }
        } catch (IOException e) {
            helper.send(BackgroundDataSync.SPOT_UPLOAD_ERROR, Bundle.EMPTY);
            e.printStackTrace();
        } catch (JSONException e) {
            helper.send(BackgroundDataSync.SPOT_UPLOAD_ERROR, Bundle.EMPTY);
            e.printStackTrace();
        }
    }

	private void retrieveSpotPictures(int spotID, ResultReceiver helper) {

		HttpGet requesturls = new HttpGet(
				SERVER_ADDRESS + "getimage.php?spotID="+spotID );

		HttpResponse response = null;
		try {
			response = httpClient.execute(requesturls);
			InputStream in = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder json = new StringBuilder();
			String line = null;
			while((line = reader.readLine()) != null) {
				json.append(line);
			}
			Log.d(TAG, json.toString());

			JSONArray spotPictureJSONArray = new JSONArray(json.toString());;
			for(int i = 0; i < spotPictureJSONArray.length(); i++) {
				JSONObject pictureInfo = spotPictureJSONArray.getJSONObject(i);
				MyDB db = new MyDB(this);
				db.storePicture(new SpotPicture(
						pictureInfo.getLong("PictureID"),
						pictureInfo.getLong("UID"),
						pictureInfo.getInt("SID"),
						pictureInfo.getString("Date"),
						pictureInfo.getString("Caption") ));
			}
			helper.send(ViewSpotActivity.PHOTO_INFORMATION_DOWNLOAD_SUCCESFUL, Bundle.EMPTY);
		} catch (Exception e) {
			Log.d(TAG, "Photo info download unsuccessful");
			e.printStackTrace();
		}

	}
}
//
//	private void uploadComment(long pictureID, String commentText, String creationDate, ResultReceiver helper) {
//
//		//Establish datetime in an sql format
//
//
//		Log.d(TAG, "upload comments");
//		try {
//			String uploadCommentURL = SERVER_ADDRESS+"uploadComment.php";
//			HttpPost post = new HttpPost(uploadCommentURL);
//
//			//add values to the body
//			MultipartEntity reqEntity = new MultipartEntity();
//			reqEntity.addPart("pictureID", new StringBody(String.valueOf(pictureID)));
//			reqEntity.addPart("uid", new StringBody(String.valueOf(uid)));
//			reqEntity.addPart("comment", new StringBody(commentText));
//			post.setEntity(reqEntity);
//
//			//send off
//			HttpResponse response = httpClient.execute(post);
//
//
//
//			switch(response.getStatusLine().getStatusCode()) {
//				case 200:
//
//					BufferedReader reader = new BufferedReader(new InputStreamReader(
//							response.getEntity().getContent()));
//					String sResponse = "";
//					sResponse = reader.readLine();
//					Log.d(TAG, sResponse);
//
//					//retrieve the assigned commentID
//					JSONObject json = new JSONObject(sResponse);
//					int commentID = json.getInt("commentID");
//					Log.d(TAG, "commentID is " + commentID);
//
//					//Load the comment into the database
//					String name = ((SkateSpotSession)getApplication()).getFbname();
//					Comment comment = new Comment(commentID, commentText, name, uid, pictureID, creationDate);
//					MyDB db = new MyDB(this);
//					db.storeComment(comment);
//					Bundle bundle = new Bundle();
//					bundle.putLong("pictureID", pictureID);
//					helper.send(PictureActivity.COMMENT_UPLOAD_SUCCESSFUL, bundle);
//					break;
//				case 400:
//					helper.send(PictureActivity.COMMENT_UPLOAD_UNSUCCESSFUL, Bundle.EMPTY);
//					break;
//			}
//
//
//		} catch (Exception e) {
//			helper.send(PictureActivity.COMMENT_UPLOAD_UNSUCCESSFUL, Bundle.EMPTY);
//			e.printStackTrace();
//		}
//
//	}
//
//	private void refreshUsersNameOnServer(long uid, String name) {
//
//		Log.d(TAG, "refereshUsersName called with uid:" + uid + " and name:" + name);
//		HttpPost postRequest = new HttpPost(SERVER_ADDRESS + "user_refresh.php");
//		MultipartEntity reqEntity = new MultipartEntity();
//
//		try {
//			reqEntity.addPart("name", new StringBody(name));
//			reqEntity.addPart("uid", new StringBody(String.valueOf(uid)));
//		} catch (UnsupportedEncodingException e1) {
//			e1.printStackTrace();
//		}
//		try {
//
//			postRequest.setEntity(reqEntity);
//			HttpResponse response = httpClient.execute(postRequest);
//
//			int responseStatus = response.getStatusLine().getStatusCode();
//			Log.d(TAG, "Response Code: " + responseStatus);
//		} catch (IOException e1) {
//				Log.d(TAG, e1.toString());
//				e1.printStackTrace();
//		}
//	}
//
//
//	private void userDeleteSpot(int spotID, ResultReceiver receiver) {
//
//		HttpGet requesturls = new HttpGet(
//				SERVER_ADDRESS + "deletespot.php?spotID="+spotID );
//
//		HttpResponse response = null;
//		try {
//			response = httpClient.execute(requesturls);
//			int responseCode = response.getStatusLine().getStatusCode();
//			InputStream in = response.getEntity().getContent();
//			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//			StringBuilder json = new StringBuilder();
//			String line = null;
//			while((line = reader.readLine()) != null) {
//				json.append(line);
//			}
//			Log.d(TAG, json.toString());
//			if(responseCode == 200) receiver.send(ViewSpotActivity.DELETE_SPOT_SUCCESFUL, Bundle.EMPTY);
//			else receiver.send(ViewSpotActivity.DELETE_SPOT_UNSUCCESFUL, Bundle.EMPTY);
//		} catch (Exception e) {
//			receiver.send(ViewSpotActivity.DELETE_SPOT_UNSUCCESFUL, Bundle.EMPTY);
//			e.printStackTrace();
//		}
//	}
//
//	private void userDeletePicture(long pictureID, ResultReceiver receiver) {
//
//		HttpGet requesturls = new HttpGet(
//				SERVER_ADDRESS + "deletepicture.php?PictureID="+pictureID );
//
//		HttpResponse response = null;
//		try {
//			response = httpClient.execute(requesturls);
//			int responseCode = response.getStatusLine().getStatusCode();
//			InputStream in = response.getEntity().getContent();
//			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//			StringBuilder responseString = new StringBuilder();
//			String line = null;
//			while((line = reader.readLine()) != null) {
//				responseString.append(line);
//			}
//			Log.d(TAG, responseString.toString());
//
//			Bundle bundle = new Bundle();
//			bundle.putLong("pictureID", pictureID);
//			if(responseCode == 200) receiver.send(PictureActivity.DELETE_PICTURE_SUCCESFUL, bundle);
//			else receiver.send(PictureActivity.DELETE_PICTURE_UNSUCCESFUL, Bundle.EMPTY);
//		} catch (Exception e) {
//			receiver.send(PictureActivity.DELETE_PICTURE_UNSUCCESFUL, Bundle.EMPTY);
//			e.printStackTrace();
//		}
//	}
//
//	private void retrievePictureComments(long pictureID, ResultReceiver receiver) {
//
//		Log.d(TAG, "Picture ID: " + pictureID);
//
//		HttpPost postRequest = new HttpPost(SERVER_ADDRESS + "retrievePictureComments.php");
//		MultipartEntity reqEntity = new MultipartEntity();
//		try {
//			reqEntity.addPart("pictureID", new StringBody(String.valueOf(pictureID)));
//		} catch (UnsupportedEncodingException e1) {
//			e1.printStackTrace();
//		}
//		try {
//			postRequest.setEntity(reqEntity);
//			HttpResponse response = httpClient.execute(postRequest);
//
//			//decode comments into an array of comment objects
//			//and put into local database
//
//			int responseStatus = response.getStatusLine().getStatusCode();
//			Log.d(TAG, "Response Code: " + responseStatus);
//
//			if(responseStatus == 200) {
//
//				// Read the response
//				BufferedReader responseBodyReader = new BufferedReader(new InputStreamReader(
//					response.getEntity().getContent(), "UTF-8"));
//				StringBuilder output = new StringBuilder();
//				String sResponse;
//				while ((sResponse = responseBodyReader.readLine()) != null) {
//					output.append(sResponse);
//				}
//				Log.d(TAG, "Comments Response body: " + output.toString());
//
//				//Read JSON to Comments and load these into the database
//				ArrayList<Comment> comments = new ArrayList<Comment>();
//				JSONArray jsonCommentsArray = new JSONArray(output.toString());
//				for(int i = 0; i < jsonCommentsArray.length(); i++) {
//					JSONObject jsonComment = jsonCommentsArray.getJSONObject(i);
//					Comment comment = new Comment(jsonComment.getInt("CommentID"),
//													jsonComment.getString("COMMENT"),
//													jsonComment.getString("FB_NAME"),
//													jsonComment.getLong("UID"),
//													jsonComment.getLong("PictureID"),
//													jsonComment.getString("DATE_CREATED"));
//					comments.add(comment);
//				}
//				MyDB db = new MyDB(this);
//				db.storeComments(comments);
//
//				// Callback with success
//				Bundle bundle = new Bundle();
//				bundle.putLong("pictureID", pictureID);
//				receiver.send(PictureActivity.IMAGE_COMMENTS_DOWNLOADED, bundle);
//
//			} else {
//				// Callback with failure
//				receiver.send(PictureActivity.IMAGE_COMMENTS_ERROR, Bundle.EMPTY);
//			}
//		} catch (IOException e1) {
//				e1.printStackTrace();
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void uploadFailAlert(ResultReceiver helper) {
//		helper.send(ViewSpotActivity.PHOTO_UPLOAD_NOT_SUCCESSFUL, Bundle.EMPTY);
//		AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Network Error").setMessage("Image failed to upload").create();
//		dialog.setIcon(R.drawable.icon);
//		dialog.setButton(DialogInterface.BUTTON1, "Retry", new OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				Toast toast = Toast.makeText(BackgroundDataSync.this, "Retry selected", Toast.LENGTH_LONG);
//				toast.show();
//			}
//		});
//		dialog.setButton(DialogInterface.BUTTON2, "Cancel", new OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				Toast toast = Toast.makeText(BackgroundDataSync.this, "Cancel selected", Toast.LENGTH_LONG);
//				toast.show();
//			}
//		});
//		dialog.show();
//	}*/

