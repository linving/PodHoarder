package com.podhoarder.db;

/**
 * @author Emil Almrot
 * 2013-03-16
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.podhoarder.fragment.GridFragment;
import com.podhoarder.object.Episode;
import com.podhoarder.object.EpisodePointer;
import com.podhoarder.util.Constants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class EpisodeDBHelper
{
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.EpisodeDBHelper";
	
	private DBHelper dbHelper;
	private SQLiteDatabase db;
	private PlaylistDBHelper plDbH;
	private static final String TABLE_NAME = DBHelper.episodeTable;
	private final String[] columns = { DBHelper.colEpisodeId,
			DBHelper.colEpisodeTitle, DBHelper.colEpisodeLink,
			DBHelper.colEpisodeLocalLink, DBHelper.colEpisodePubDate, 
			DBHelper.colEpisodeDescription,DBHelper.colEpisodeElapsedTime, 
			DBHelper.colEpisodeTotalTime, DBHelper.colIsFavorite, DBHelper.colParentFeedId };

	public EpisodeDBHelper(Context ctx)
	{
		this.dbHelper = new DBHelper(ctx);
		this.plDbH = new PlaylistDBHelper(ctx);
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
		Cursor cursor = this.db.query(TABLE_NAME, columns,  columns[9] + "=" + feedId, null, null, null, DBHelper.colEpisodePubDate + " DESC");
		try
		{
			if (cursor.moveToFirst())
			{
				do
				{
					episodes.add(cursorToEpisode(cursor));
				} while (cursor.moveToNext());
			}
		}
		catch (IllegalStateException e)
		{
			Log.d(LOG_TAG, "Cursor caused IllegalStateException on FeedID: " + feedId);
			e.printStackTrace();
		}
		
		return episodes;
	}
	
	/**
	 * 
	 * Retrieves a list of the latest Episodes across all Feeds.
	 * @return 			A list containing the latest Episode objects.
	 */
	public List<Episode> getLatestEpisodes()
	{
		this.db = this.dbHelper.getWritableDatabase();
		Cursor cursor = this.db.query(TABLE_NAME, columns, null, null, null, null, DBHelper.colEpisodePubDate + " DESC", ""+Constants.LATEST_EPISODES_COUNT);
		List<Episode> episodes = new ArrayList<Episode>();
		try
		{
			if (cursor.moveToFirst())
			{
				do
				{
					episodes.add(cursorToEpisode(cursor));
				} while (cursor.moveToNext());
			}
		}
		catch (IllegalStateException e)
		{
			Log.d(LOG_TAG, "Cursor caused IllegalStateException on getLatestEpisodes!");
			e.printStackTrace();
		}
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
		try
		{
			if (cursor.moveToFirst())
			{
				do
				{
					episodes.add(cursorToEpisode(cursor));
				} while (cursor.moveToNext());
			}
		}
		catch (IllegalStateException e)
		{
			Log.d(LOG_TAG, "Cursor caused IllegalStateException on getLatestEpisodes!");
			e.printStackTrace();
		}
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
		return episodes;	
	}
	
	/**
	 * Retrieves a list of all NEW Episodes.
	 * @return A List<Episode> of Episode objects that are flagged as NEW.
	 */
	public List<Episode> getNewEpisodes()
	{
		this.db = this.dbHelper.getWritableDatabase();
		Cursor cursor = this.db.query(TABLE_NAME, columns, DBHelper.colEpisodeLocalLink + " IS NULL OR "
		+ DBHelper.colEpisodeLocalLink + " = '' AND " 
		+ DBHelper.colEpisodeTotalTime + " = 0 AND " 
		+ DBHelper.colEpisodeElapsedTime + " = 0", null, null, null, DBHelper.colEpisodePubDate + " DESC");
		List<Episode> episodes = new ArrayList<Episode>();
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
	 * Retrieves a list of all Episodes marked as favorites in the db.
	 * @return A List<Episode> object containing all favorited Episode objects.
	 */
	public List<Episode> getFavoriteEpisodes()
	{
        List<Episode> episodes = new ArrayList<Episode>();
        this.db = this.dbHelper.getWritableDatabase();
        Cursor cursor = this.db.query(TABLE_NAME, columns,  columns[8] + "= ?", new String[]{Integer.toString(1)}, null, null, DBHelper.colEpisodePubDate + " DESC");
        try
        {
            if (cursor.moveToFirst())
            {
                do
                {
                    episodes.add(cursorToEpisode(cursor));
                } while (cursor.moveToNext());
            }
        }
        catch (IllegalStateException e)
        {
            Log.d(LOG_TAG, "Cursor caused IllegalStateException on getFavorites()");
            e.printStackTrace();
        }

        return episodes;
	}

	/**
	 * Performs a search on all episodes stored in the db with the current filter applied. Matches searchstring on both title and description.
	 * @param currentFilter The currently used filter (with searchString set)
	 * @return	List<Episode> containing the 100 most relevant results.
	 */
	public List<Episode> search(GridFragment.ListFilter currentFilter)
	{
        List<Episode> episodes = new ArrayList<Episode>();
        this.db = this.dbHelper.getWritableDatabase();

        String whereString = "";
        String[] args = new String[]{};
        switch (currentFilter) {
            case ALL:
                whereString = DBHelper.colEpisodeTitle + " LIKE ? OR " + DBHelper.colEpisodeDescription + " LIKE ?";    //Apply search to title and description
                args = new String[] {"%"+ currentFilter.getSearchString() + "%" , "%"+ currentFilter.getSearchString() + "%"};
                break;
            case FEED:
                whereString = DBHelper.colParentFeedId + "=" + currentFilter.getFeedId() + " AND ("     //Apply FEED filter.
                        + DBHelper.colEpisodeTitle + " LIKE ? OR " + DBHelper.colEpisodeDescription + " LIKE ?)"; //Apply search to title and description
                args = new String[] {"%"+ currentFilter.getSearchString() + "%" , "%"+ currentFilter.getSearchString() + "%"};
                break;
            default:
                break;
        }
        Cursor cursor = this.db.query(true, TABLE_NAME, columns, whereString, args, null, null, null,
                ""+Constants.LOCAL_SEARCH_RESULT_LIMIT);
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
	 * Retrieves a list of all Episodes referenced in the Playlist db.
	 * @return List<Episode> containing all the Episodes referenced in the Playlist db.
	 */
	public Queue<Episode> getPlaylistEpisodes()
	{
		LinkedList<Episode> ret = new LinkedList<Episode>();
        LinkedList<Episode> unordered = new LinkedList<Episode>();
		List<EpisodePointer> pointers = plDbH.loadPlaylistPointers();
		
		if (pointers.size() > 0)
		{
			String[] ids = new String[pointers.size()];
			int i = 0;
			for (EpisodePointer p : pointers)
			{
				ids[i] = ""+p.getEpisodeId();
				i++;
			}
			this.db = this.dbHelper.getWritableDatabase();
			String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + columns[0] + " IN (" + makePlaceholders(ids.length) + ")";
			Cursor cursor = this.db.rawQuery(query, ids);
			if (cursor.moveToFirst())
			{
				do
				{
					unordered.addFirst(cursorToEpisode(cursor));
				} while (cursor.moveToNext());
			}
			
			/*//Now that we have the correct order in pointers we need to reorder the playlist accordingly.
			for (EpisodePointer pointer:pointers)
			{

                for (Episode ep : unordered) {
                    if (ep.getEpisodeId() == pointer.getEpisodeId()) {
                        //Log.i(LOG_TAG,"LOADED " + playlist.get(i).getTitle() + "(ID: " + pointer.getId() + ")");
                        ret.add(ep);
                        break;
                    }
                }
			}
			
			if (ret.size() != unordered.size())	//This means that there are playlist entries that have not been stored in the database. They should be added to the end and then we should save the playlist.
			{
				//Log.w(LOG_TAG, "Playlist size doesn't match number of stored pointers. Mismatch!");
				for (Episode ep : unordered)	//Go through playlist and look for the entries that haven't been added to ret yet.
				{
					if (!ret.contains(ep)) ret.add(ep);	//Add the missing entry at the last index.
				}
				this.plDbH.savePlaylist(ret);	//Save the appended playlist to the db before returning.
			}*/
		}
		return unordered;
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
		cursor.moveToFirst();
		try
		{
			ep = cursorToEpisode(cursor);
			return ep;
		}
		catch (CursorIndexOutOfBoundsException ex) //If the cursor is empty it means there is no episode with that ID. Return null instead.
		{
			return null;
		}
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
        values.put(columns[8], ep.isFavorite());
	    values.put(columns[9], feedId);
	    
	    try
	    {
	    	this.db = this.dbHelper.getWritableDatabase();
		    long insertId = this.db.insert(TABLE_NAME, null, values);
            if (insertId != -1) {
                Episode insertedEpisode = new Episode((int) insertId, ep.getTitle(), ep.getLink(), ep.getLocalLink(), ep.getPubDate(), ep.getDescription(), ep.getElapsedTime(), ep.getTotalTime(), false, feedId);
                Log.i(LOG_TAG, "Added Episode with id: " + insertId);
                return insertedEpisode;
            }
            else {
                Log.i(LOG_TAG, "Failed to add episode");
                return null;
            }
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
        for (Episode ep : eps) {
            ContentValues values = new ContentValues();
            values.put(columns[1], ep.getTitle());
            values.put(columns[2], ep.getLink());
            values.put(columns[3], ep.getLocalLink());
            values.put(columns[4], ep.getPubDate());
            values.put(columns[5], ep.getDescription());
            values.put(columns[6], ep.getElapsedTime());
            values.put(columns[7], ep.getTotalTime());
            values.put(columns[8], ep.isFavorite());
            values.put(columns[9], feedId);

            this.db = this.dbHelper.getWritableDatabase();
            long insertId = this.db.insert(TABLE_NAME, null, values);
            if (insertId != -1) {
                Episode insertedEpisode = new Episode((int) insertId, ep.getTitle(), ep.getLink(), ep.getLocalLink(), ep.getPubDate(), ep.getDescription(), ep.getElapsedTime(), ep.getTotalTime(), false, feedId);
                episodes.add(insertedEpisode);
                Log.i(LOG_TAG, "Added Episode with id: " + insertId);
            }
            else {
                Log.i(LOG_TAG, "Failed to add episode");
            }

//            Cursor cursor = this.db.query(TABLE_NAME, columns, columns[0] + " = " + insertId, null, null, null, null);
//            cursor.moveToFirst();
//            Episode insertedEpisode = cursorToEpisode(cursor);
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
		//TODO: Make sure that Episodes also delete the associated files if they have been downloaded.
		boolean retCheck = false;
	    int id = ep.getEpisodeId();
	    this.db = this.dbHelper.getWritableDatabase();
	    int res = this.db.delete(TABLE_NAME, columns[0] + " = " + id, null);
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
		retVal = this.db.delete(TABLE_NAME, columns[9] + "=" + feedId, null);
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
        values.put(columns[8], updatedEpisode.isFavorite());
	    values.put(columns[9], updatedEpisode.getFeedId());
	    try
	    {
	    	this.db = this.dbHelper.getWritableDatabase();
		    this.db.update(TABLE_NAME, values, columns[0] + " = " + updatedEpisode.getEpisodeId(), null);
		    Cursor cursor = this.db.query(TABLE_NAME, columns, columns[0] + " = " + updatedEpisode.getEpisodeId(), null, null, null, null);
		    Log.w(LOG_TAG,"Updated Episode with id: " + updatedEpisode.getEpisodeId());
		    cursor.moveToFirst();
		    ep = cursorToEpisode(cursor);
			return ep;
	    }
	    catch (IllegalStateException e)
	    {
	    	Log.e(LOG_TAG, "Failed to update Episode with id: " + updatedEpisode.getEpisodeId() + "!");
	    	return updatedEpisode;
	    }
	    
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
                + columns[8] + ", "
				+ columns[9] + ") values(?,?,?,?,?,?,?,?,?,?)";
		
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
            statement.bindLong(9,  ep.isFavorite() ? 1 : 0 );
			statement.bindLong(10, ep.getFeedId());
			statement.execute();
		}
		this.db.setTransactionSuccessful();
		this.db.endTransaction();
		return updatedEpisodes;
	}
	
	/**
	 * Converts a Cursor object to an Episode object.
	 * @param c	Cursor containing the data.
	 * @return	A new Episode object.
	 */
	private Episode cursorToEpisode(Cursor c)
	{
		return new Episode(Integer.parseInt(c.getString(0)),
                c.getString(1), c.getString(2), c.getString(3), c.getString(4),
                c.getString(5), Integer.parseInt(c.getString(6)), Integer.parseInt(c.getString(7)), (Integer.parseInt(c.getString(8)) != 0), Integer.parseInt(c.getString(9)));
	}
	
	/**
	 * Makes a correctly formatted placeholder string for queries.
	 * @param length Number of items to represent.
	 * @return A correctly formatted placeholder string.
	 */
	private String makePlaceholders(int length) 
	{
	    if (length < 1) 
	    {
	    	return "";
	    } 
	    else 
	    {
	        StringBuilder sb = new StringBuilder(length * 2 - 1);
	        sb.append("?");
	        for (int i = 1; i < length; i++) {
	            sb.append(",?");
	        }
	        return sb.toString();
	    }
	}

	/**
	 * Tries to close the database if it is open. Use this to make sure that no db conections leak!
	 */
	public void closeDatabaseIfOpen()
	{
		if (db != null && db.isOpen())
			db.close();
	}
}
