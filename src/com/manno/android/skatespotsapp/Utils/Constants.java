package com.manno.android.skatespotsapp.Utils;

public class Constants {
	
	//Database Constants
	public static final String DATABASE_NAME="datastorage";
	
	public static final String SPOT_TABLE_NAME="spot";
	public static final String LONGITUDE="long";
	public static final String LATITUDE="lat";
	public static final String SPOT_NAME="name";
	public static final String ADDRESS="addr";
	public static final String DATE_ENTERED="creationdate";
	public static final String SPOT_ID="sid";
	public static final String USER_ID ="uid";
	public static final String SHARING = "share";      // 0 for public  /  1 for friends only
	public static final String NUM_PICS="numpics";
	public static final String RATING="rating";
	public static final String VOTE_TALLY = "tally";
	public static final String NUM_RATINGS = "num_ratings";
	public static final String DESCRIPTION = "description";
	public static final String PICTURE_ID = "picture_id";
	public static final String CREATION_DATE = "creation_date";
	public static final String PICTURE_CAPTION = "picture_caption";
	
		//Spot Images
		public static final String RATED = "rated";                         //user_ratings column
		public static final String STATUS = "status";						//server updating status
		public static final String USER_RATING_TABLE_NAME = "users_ratings";
		public static final String SPOT_IMAGE_URI_TABLE_NAME = "spot_pictures";
		
		//Image Comments
		public static final String IMAGE_COMMENTS_TABLE_NAME = "image_comments";
		public static final String COMMENT_ID = "comment_id";                         //user_ratings column
		public static final String COMMENT = "comment";						//server updating status
		public static final String CREATOR_NAME = "creator_name";
		public static final String CREATOR_ID = "creator_id";
		
	//Rating Enumerations
	public static final int RATING_ENUM_UNRATED = 1;
	public static final int RATING_ENUM_LIKE = 2;
	public static final int RATING_ENUM_DISLIKE = 3;
	//Server update status
	public static final int RATING_UPDATING_SERVER = 1;
	public static final int RATING_CONFIRMED_UPDATED = 2;


    public static final String NEWS_ITEM_TABLE_NAME = "news_items" ;
    public static final String NEWS_ITEM_ID = "id";
    public static final String NEWS_ITEM_CLIENT_ID = "client_id";
    public static final String NEWS_ITEM_CREATION_DATE = "date";
    public static final String NEWS_ITEM_LINK_URL= "link_url";
    public static final String NEWS_ITEM_PICTURE_URL = "picture_url";
}
