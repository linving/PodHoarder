package com.podhoarder.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.DBHelper";

	static final String dbName = "podHoarderDB";

	static final String feedTable = "FEEDS";
	static final String colFeedId = "feedId";
	static final String colFeedTitle = "title";
	static final String colFeedAuthor = "author";
	static final String colFeedDescription = "description";
	static final String colFeedLink = "link";
	static final String colFeedCategory = "category";
	static final String colFeedImage = "imageURL";

	static final String episodeTable = "EPISODES";
	static final String colEpisodeId = "episodeId";
	static final String colEpisodeTitle = "title";
	static final String colEpisodeLink = "link";
	static final String colEpisodeLocalLink = "localLink";
	static final String colEpisodePubDate = "pubDate";
	static final String colEpisodeDescription = "description";
	static final String colEpisodeElapsedTime = "elapsedTime";
	static final String colEpisodeTotalTime = "totalTime";
	static final String colParentFeedId = "feedId";
	
	static final String playlistTable = "PLAYLIST";
	static final String colId = "id";
	
	public DBHelper(Context context)
	{
		super(context, dbName, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		//Create FEEDS table
		db.execSQL("CREATE TABLE " + feedTable + " (" 
					+ colFeedId + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ colFeedTitle + " TEXT NOT NULL, " 
					+ colFeedAuthor + " TEXT NOT NULL, "
					+ colFeedDescription + " TEXT NOT NULL, "
					+ colFeedLink + " TEXT NOT NULL UNIQUE, "
					+ colFeedCategory + " TEXT NOT NULL, "
					+ colFeedImage + " TEXT NOT NULL "
			    	+	")");
			  
		//Create EPISODES table
		db.execSQL("CREATE TABLE " + episodeTable + " (" 
				+ colEpisodeId + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ colEpisodeTitle + " TEXT NOT NULL, " 
				+ colEpisodeLink + " TEXT NOT NULL UNIQUE, "
				+ colEpisodeLocalLink + " TEXT NOT NULL, "
				+ colEpisodePubDate + " TEXT NOT NULL, "
				+ colEpisodeDescription + " TEXT NOT NULL, "
				+ colEpisodeElapsedTime + " INTEGER NOT NULL, "
				+ colEpisodeTotalTime + " INTEGER NOT NULL, "
				+ colParentFeedId + " INTEGER NOT NULL "
		    	+	")");
		
		//Create PLAYLIST table
		db.execSQL("CREATE TABLE " + playlistTable + " (" 
				+ colId + " INTEGER PRIMARY KEY, "
				+ colEpisodeId + " INTEGER NOT NULL " 
		    	+	")");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE IF EXISTS "+feedTable);
		db.execSQL("DROP TABLE IF EXISTS "+episodeTable);
		db.execSQL("DROP TABLE IF EXISTS "+playlistTable);
		
		onCreate(db);
	}

}
