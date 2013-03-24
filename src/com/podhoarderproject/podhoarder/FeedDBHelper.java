package com.podhoarderproject.podhoarder;

/**
 * @author Emil Almrot
 * 2013-03-16
 */
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FeedDBHelper
{
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.FeedDBHelper";
	
	private DBHelper dbHelper;
	private SQLiteDatabase db;
	private EpisodeDBHelper eph;
	private Context ctx;
	private static final String TABLE_NAME = DBHelper.feedTable;
	private final String[] columns = { DBHelper.colFeedId,
			DBHelper.colFeedTitle, DBHelper.colFeedAuthor,
			DBHelper.colFeedDescription, DBHelper.colFeedLink,
			DBHelper.colFeedCategory, DBHelper.colFeedImage };

	public FeedDBHelper(Context ctx)
	{
		this.ctx = ctx;
		this.dbHelper = new DBHelper(this.ctx );
		this.eph = new EpisodeDBHelper(this.ctx);
	}

	/**
	 * 
	 * Retrieves a list of all the Feeds from the SQLite database.
	 * @return A list containing all stored Feeds
	 */
	public List<Feed> getAllFeeds()
	{
		List<Feed> feeds = new ArrayList<Feed>();
		this.db = this.dbHelper.getWritableDatabase();
		Cursor cursor = this.db.query(TABLE_NAME, columns, null, null,
				null, null, null);
		if (cursor.moveToFirst())
		{
			do
			{
				feeds.add(cursorToFeed(cursor));
			} while (cursor.moveToNext());
		}
		this.db.close();
		return feeds;
	}

	/**
	 * 
	 * Retrieves a Feed from the SQLite database.
	 * @param id	Id of the Feed to retrieve.
	 * @return A Feed object.
	 */
	public Feed getFeed(int id)
	{
		Feed feed = new Feed();
		this.db = this.dbHelper.getWritableDatabase();
		Cursor cursor = this.db.query(TABLE_NAME, columns, columns[0] + " = " + id, null, null, null, null);
		cursor.moveToFirst();
		this.db.close();
		feed = cursorToFeed(cursor);
		return feed;
	}
	
	/**
	 * 
	 * Stores a Feed in the database.
	 * @param feed Feed object to insert.
	 * @throws SQLiteConstraintException Throws an SQLiteConstrainException if the Feed added already exists in the database (duplicate entries not allowed)
	 * @return A Feed object with updated Id relative to the table.
	 */
	public Feed insertFeed(Feed feed) throws SQLiteConstraintException
	{
		ContentValues values = new ContentValues();
	    values.put(columns[1], feed.getTitle());
	    values.put(columns[2], feed.getAuthor());
	    values.put(columns[3], feed.getDescription());
	    values.put(columns[4], feed.getLink());
	    values.put(columns[5], feed.getCategory());
	    values.put(columns[6], feed.getFeedImage().getImageURL());
	    
	    this.db = this.dbHelper.getWritableDatabase();
	    try
	    {
	    	long insertId = this.db.insert(TABLE_NAME, null, values);
	    	Cursor cursor = this.db.query(TABLE_NAME, columns, columns[0] + " = " + insertId, null, null, null, null);
		    Log.w(LOG_TAG,"Added Feed with id: " + insertId);
		    cursor.moveToFirst();
		    Feed insertedFeed = cursorToFeed(cursor);
		    this.db.close();
		    this.eph.insertEpisodes(feed.getEpisodes(), insertedFeed.getFeedId());
		    insertedFeed.setEpisodes(this.eph.getAllEpisodes(insertedFeed.getFeedId()));
		    return insertedFeed;
	    }
	    catch(SQLiteConstraintException e) 
	    { 
	    	throw e;
	    }
	    
	    
	}
	
	/**
	 * 
	 * Deletes a Feed from the database.
	 * @param feedId Feed ID of object to delete.
	 * @return True if something was deleted. False otherwise.
	 */
	public boolean deleteFeed(int feedId) 
	{
		boolean retCheck = false;
	    this.db = this.dbHelper.getWritableDatabase();
	    int res = this.db.delete(TABLE_NAME, columns[0] + " = " + feedId, null);
	    this.db.close();
	    this.eph.deleteEpisodes(feedId);
	    if (res == 0)
	    {
	    	Log.w(LOG_TAG,"No Feed deleted");
	    }
	    else
	    {
	    	retCheck = true;
	    	Log.w(LOG_TAG,"Feed deleted with id: " + feedId);
	    }
	    return retCheck;
	  }
	
	/**
	 * Converts a Cursor object to a Feed object.
	 * @param c	Cursor containing the data.
	 * @return	A new Feed object.
	 */
	private Feed cursorToFeed(Cursor c)
	{
		Feed feed = new Feed(	Integer.parseInt(c.getString(0)),
							c.getString(1), 
							c.getString(2), 
							c.getString(3), 
							c.getString(4),
							c.getString(5), 
							c.getString(6), 
							this.eph.getAllEpisodes(Integer.parseInt(c.getString(0))), 
							this.ctx);
		return feed;
	}
}
