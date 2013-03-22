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
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EpisodeDBHelper
{
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.EpisodeDBHelper";
	
	private DBHelper dbHelper;
	private SQLiteDatabase db;
	private static final String TABLE_NAME = DBHelper.episodeTable;
	private final String[] columns = { DBHelper.colEpisodeId,
			DBHelper.colEpisodeTitle, DBHelper.colEpisodeLink,
			DBHelper.colEpisodePubDate, DBHelper.colEpisodeDescription,
			DBHelper.colEpisodepercentListened, DBHelper.colParentFeedId };

	public EpisodeDBHelper(Context ctx)
	{
		this.dbHelper = new DBHelper(ctx);
	}

	/**
	 * 
	 * Retrieves a list of all the Episodes from the SQLite database.
	 * @return A list containing all stored Episodes
	 */
	public List<Episode> getAllEpisodes()
	{
		List<Episode> episodes = new ArrayList<Episode>();
		this.db = this.dbHelper.getWritableDatabase();
		Cursor cursor = this.db.query(TABLE_NAME, columns, null, null,
				null, null, null);
		this.db.close();
		if (cursor.moveToFirst())
		{
			do
			{
				episodes.add(cursorToEpisode(cursor));
			} while (cursor.moveToNext());
		}
		return episodes;
	}
	
	/**
	 * 
	 * Retrieves a list of all Episodes in a certain Feed from the SQLite database.
	 * @param feedId	Id of the Feed to get all Episodes from.
	 * @return 			A list containing all Episodes of the given Feed.
	 */
	public List<Episode> getAllEpisodes(int feedId)
	{
		List<Episode> episodes = new ArrayList<Episode>();
		this.db = this.dbHelper.getWritableDatabase();
		Cursor cursor = this.db.query(TABLE_NAME, columns,  columns[6] + "=" + feedId, null, null, null, null);
		
		if (cursor.moveToFirst())
		{
			do
			{
				episodes.add(cursorToEpisode(cursor));
			} while (cursor.moveToNext());
		}
		this.db.close();
		return episodes;
	}

	/**
	 * 
	 * Retrieves an Episode from the SQLite database.
	 * @param id	Id of the Episode to retrieve.
	 * @return An Episode object.
	 */
	public Episode getFeed(int id)
	{
		Episode ep = new Episode();
		this.db = this.dbHelper.getWritableDatabase();
		Cursor cursor = this.db.query(TABLE_NAME, columns, columns[0] + " = " + id, null, null, null, null);
		this.db.close();
		cursor.moveToFirst();
		ep = cursorToEpisode(cursor);
		return ep;
	}
	
	/**
	 * 
	 * Stores an Episode in the database.
	 * @param ep Episode object to insert.
	 * @return An Episode object with updated Id relative to the table.
	 */
	public Episode insertEpisode(Episode ep, int feedId)
	{
		
		ContentValues values = new ContentValues();
	    values.put(columns[1], ep.getTitle());
	    values.put(columns[2], ep.getLink());
	    values.put(columns[3], ep.getPubDate());
	    values.put(columns[4], ep.getDescription());
	    values.put(columns[5], ep.getPercentListened());
	    values.put(columns[6], feedId);
	    
	    this.db = this.dbHelper.getWritableDatabase();
	    long insertId = this.db.insert(TABLE_NAME, null, values);
	    Cursor cursor = this.db.query(TABLE_NAME, columns, columns[0] + " = " + insertId, null, null, null, null);
	    Log.w(LOG_TAG,"Added Episode with id: " + insertId);
	    cursor.moveToFirst();
	    Episode insertedEpisode = cursorToEpisode(cursor);
	    this.db.close();
	    return insertedEpisode;
	}
	
	/**
	 * 
	 * Stores a collection of Episode objects in the database.
	 * @param eps Episode objects to insert.
	 * @return A List<Episode> object with updated Id's relative to the table.
	 */
	public List<Episode> insertEpisodes(List<Episode> eps, int feedId)
	{
		List<Episode> episodes = new ArrayList<Episode>();
		for (int i=0; i<eps.size(); i++)
		{
			ContentValues values = new ContentValues();
		    values.put(columns[1], eps.get(i).getTitle());
		    values.put(columns[2], eps.get(i).getLink());
		    values.put(columns[3], eps.get(i).getPubDate());
		    values.put(columns[4], eps.get(i).getDescription());
		    values.put(columns[5], eps.get(i).getPercentListened());
		    values.put(columns[6], feedId);
		    
		    this.db = this.dbHelper.getWritableDatabase();
		    long insertId = this.db.insert(TABLE_NAME, null, values);
		    Cursor cursor = this.db.query(TABLE_NAME, columns, columns[0] + " = " + insertId, null, null, null, null);
		    Log.w(LOG_TAG,"Added Episode with id: " + insertId);
		    cursor.moveToFirst();
		    Episode insertedEpisode = cursorToEpisode(cursor);
		    this.db.close();
		    episodes.add(insertedEpisode);
		}
		return episodes;
	}
	
	/**
	 * 
	 * Deletes an Episode from the database.
	 * @param ep Episode object to delete.
	 * @return True if something was deleted. False otherwise.
	 */
	public boolean deleteEpisode(Episode ep) 
	{
		boolean retCheck = false;
	    int id = ep.getEpisodeId();
	    this.db = this.dbHelper.getWritableDatabase();
	    int res = this.db.delete(TABLE_NAME, columns[0] + " = " + id, null);
	    this.db.close();
	    if (res == 0)
	    {
	    	Log.w(LOG_TAG,"No Episode deleted");
	    }
	    else
	    {
	    	retCheck = true;
	    	Log.w(LOG_TAG,"Episode deleted with id: " + id);
	    }
	    return retCheck;
	  }
	
	/**
	 * Converts a Cursor object to an Episode object.
	 * @param c	Cursor containing the data.
	 * @return	A new Episode object.
	 */
	private Episode cursorToEpisode(Cursor c)
	{
		Episode ep = new Episode(Integer.parseInt(c.getString(0)),
				c.getString(1), c.getString(2), c.getString(3), c.getString(4),
				Integer.parseInt(c.getString(5)), Integer.parseInt(c.getString(6)));
		return ep;
	}
}
