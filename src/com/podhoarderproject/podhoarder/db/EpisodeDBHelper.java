package com.podhoarderproject.podhoarder.db;

/**
 * @author Emil Almrot
 * 2013-03-16
 */
import java.util.ArrayList;
import java.util.List;

import com.podhoarderproject.podhoarder.util.Episode;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class EpisodeDBHelper
{
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.EpisodeDBHelper";
	
	private DBHelper dbHelper;
	private SQLiteDatabase db;
	private static final String TABLE_NAME = DBHelper.episodeTable;
	private final String[] columns = { DBHelper.colEpisodeId,
			DBHelper.colEpisodeTitle, DBHelper.colEpisodeLink,
			DBHelper.colEpisodeLocalLink, DBHelper.colEpisodePubDate, 
			DBHelper.colEpisodeDescription,DBHelper.colEpisodeElapsedTime, 
			DBHelper.colEpisodeTotalTime, DBHelper.colParentFeedId };

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
				null, null, DBHelper.colEpisodePubDate + " DESC");
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
	 * Retrieves a list of all Episodes in a certain Feed from the SQLite database.
	 * @param feedId	Id of the Feed to get all Episodes from.
	 * @return 			A list containing all Episodes of the given Feed.
	 */
	public List<Episode> getAllEpisodes(int feedId)
	{
		List<Episode> episodes = new ArrayList<Episode>();
		this.db = this.dbHelper.getWritableDatabase();
		Cursor cursor = this.db.query(TABLE_NAME, columns,  columns[8] + "=" + feedId, null, null, null, DBHelper.colEpisodePubDate + " DESC");
		
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
	 * Retrieves a list of the latest Episodes across all Feeds.
	 * @param nrOfEpisodes	The amount of Episodes to return.
	 * @return 			A list containing the latest Episode objects.
	 */
	public List<Episode> getLatestEpisodes(int nrOfEpisodes)
	{
		this.db = this.dbHelper.getWritableDatabase();
		Cursor cursor = this.db.query(TABLE_NAME, columns, null, null, null, null, DBHelper.colEpisodePubDate + " DESC", ""+nrOfEpisodes);
		List<Episode> episodes = new ArrayList<Episode>();
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
	 * Retrieves a list of all the downloaded Episodes.
	 * @return 			A list containing the downloaded Episode objects.
	 */
	public List<Episode> getDownloadedEpisodes()
	{
		this.db = this.dbHelper.getWritableDatabase();
		Cursor cursor = this.db.query(TABLE_NAME, columns, DBHelper.colEpisodeLocalLink +" IS NOT NULL AND "+ DBHelper.colEpisodeLocalLink + " != ''", null, null, null, DBHelper.colEpisodePubDate + " DESC");
		List<Episode> episodes = new ArrayList<Episode>();
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
	public Episode getEpisode(int id)
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
	public Episode insertEpisode(Episode ep, int feedId) throws SQLiteConstraintException
	{
		
		ContentValues values = new ContentValues();
	    values.put(columns[1], ep.getTitle());
	    values.put(columns[2], ep.getLink());
	    values.put(columns[3], ep.getLocalLink());
	    values.put(columns[4], ep.getPubDate());
	    values.put(columns[5], ep.getDescription());
	    values.put(columns[6], ep.getElapsedTime());
	    values.put(columns[7], ep.getTotalTime());
	    values.put(columns[8], feedId);
	    
	    try
	    {
	    	this.db = this.dbHelper.getWritableDatabase();
		    long insertId = this.db.insert(TABLE_NAME, null, values);
		    Cursor cursor = this.db.query(TABLE_NAME, columns, columns[0] + " = " + insertId, null, null, null, null);
		    Log.w(LOG_TAG,"Added Episode with id: " + insertId);
		    cursor.moveToFirst();
		    Episode insertedEpisode = cursorToEpisode(cursor);
		    this.db.close();
		    return insertedEpisode;
	    }
	    catch (SQLiteConstraintException e)
		{
			Log.e(LOG_TAG, "SQLiteConstraintException: Insert failed. Episode link not unique?");
			throw e;
		}
	}
	
	/**
	 * 
	 * Stores a collection of Episode objects in the database.
	 * @param eps Episode objects to insert.
	 * @param feedId ID of the Feed that the Episodes belong to.
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
		    values.put(columns[3], eps.get(i).getLocalLink());
		    values.put(columns[4], eps.get(i).getPubDate());
		    values.put(columns[5], eps.get(i).getDescription());
		    values.put(columns[6], eps.get(i).getElapsedTime());
		    values.put(columns[7], eps.get(i).getTotalTime());
		    values.put(columns[8], feedId);
		    
		    this.db = this.dbHelper.getWritableDatabase();
		    long insertId = this.db.insert(TABLE_NAME, null, values);
		    Cursor cursor = this.db.query(TABLE_NAME, columns, columns[0] + " = " + insertId, null, null, null, null);
		    Log.i(LOG_TAG,"Added Episode with id: " + insertId);
		    cursor.moveToFirst();
		    Episode insertedEpisode = cursorToEpisode(cursor);
		    this.db.close();
		    episodes.add(insertedEpisode);
		}
		return episodes;
	}
	
	/**
	 * **UNDER CONSTRUCTION**
	 * @param eps Episode objects to insert.
	 * @param feedId ID of the Feed that the Episodes belong to.
	 * @return A List<Episode> object with updated Id's relative to the table.
	 */
	public List<Episode> insertEpisodesAsync(List<Episode> eps, int feedId)
	{
		//TODO: Make this
		return null;
	}
	
	/**
	 * 
	 * Deletes an Episode from the database.
	 * @param ep Episode object to delete.
	 * @return True if something was deleted. False otherwise.
	 */
	public boolean deleteEpisode(Episode ep) 
	{
		//TODO: Make sure that Episodes also delete the associated files if they have been downloaded.
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
	 * 
	 * Deletes all episodes belonging to the supplied FeedId.
	 * @param feedId	Id of the Feed to delete all Episodes from.
	 * @return 			Number of Episodes that were removed.
	 */
	public int deleteEpisodes(int feedId)
	{
		//TODO: Make sure that all downloaded Episode files are also deleted.
		int retVal=0;
		this.db = this.dbHelper.getWritableDatabase();
		retVal = this.db.delete(TABLE_NAME, columns[8] + "=" + feedId, null);
		this.db.close();
		return retVal;
	}	
	
	/**
	 * Updates the specified Episode in the database.
	 * @param updatedEpisode Episode to update with the new values already stored.
	 * @return The updated Episode object.
	 */
	public Episode updateEpisode(Episode updatedEpisode)
	{
		Episode ep;
		ContentValues values = new ContentValues();
	    values.put(columns[1], updatedEpisode.getTitle());
	    values.put(columns[2], updatedEpisode.getLink());
	    values.put(columns[3], updatedEpisode.getLocalLink());
	    values.put(columns[4], updatedEpisode.getPubDate());
	    values.put(columns[5], updatedEpisode.getDescription());
	    values.put(columns[6], updatedEpisode.getElapsedTime());
	    values.put(columns[7], updatedEpisode.getTotalTime());
	    values.put(columns[8], updatedEpisode.getFeedId());
	    
	    this.db = this.dbHelper.getWritableDatabase();
	    this.db.update(TABLE_NAME, values, columns[0] + " = " + updatedEpisode.getEpisodeId(), null);
	    Cursor cursor = this.db.query(TABLE_NAME, columns, columns[0] + " = " + updatedEpisode.getEpisodeId(), null, null, null, null);
	    Log.w(LOG_TAG,"Updated Episode with id: " + updatedEpisode.getEpisodeId());
	    cursor.moveToFirst();
	    ep = cursorToEpisode(cursor);
	    this.db.close();
		return ep;
	}
	
	/**
	 * Updates the specified Episodes in the database.
	 * @param updatedEpisodes List of Episode objects to update with the new values already stored.
	 * @return The updated Episode List object.
	 */
	public List<Episode> bulkUpdateEpisodes(List<Episode> updatedEpisodes)
	{
		String sql = "INSERT OR REPLACE INTO " + TABLE_NAME + " ("
				+ columns[0] + ", "
				+ columns[1] + ", "
				+ columns[2] + ", "
				+ columns[3] + ", "		
				+ columns[4] + ", "
				+ columns[5] + ", "
				+ columns[6] + ", "
				+ columns[7] + ", "
				+ columns[8] + ") values(?,?,?,?,?,?,?,?,?)";
		
		this.db = this.dbHelper.getWritableDatabase();
		SQLiteStatement statement = this.db.compileStatement(sql);
		this.db.beginTransaction();
		for (Episode ep : updatedEpisodes)
		{
			statement.clearBindings();
			statement.bindLong(1, ep.getEpisodeId());
			statement.bindString(2, ep.getTitle());
			statement.bindString(3, ep.getLink());
			statement.bindString(4, ep.getLocalLink());
			statement.bindString(5, ep.getPubDate());
			statement.bindString(6, ep.getDescription());
			statement.bindLong(7, ep.getElapsedTime());
			statement.bindLong(8, ep.getTotalTime());
			statement.bindLong(9, ep.getFeedId());
			statement.execute();
		}
		this.db.setTransactionSuccessful();
		this.db.endTransaction();
		this.db.close();
		return updatedEpisodes;
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
				c.getString(5), Integer.parseInt(c.getString(6)), Integer.parseInt(c.getString(7)), Integer.parseInt(c.getString(8)));
		return ep;
	}
}
