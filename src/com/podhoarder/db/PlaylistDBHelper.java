package com.podhoarder.db;

/**
 * @author Emil Almrot
 * 2013-03-16
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.podhoarder.object.Episode;
import com.podhoarder.object.EpisodePointer;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDBHelper
{
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.PlaylistDBHelper";
	
	private DBHelper dbHelper;
	private SQLiteDatabase db;
	private Context ctx;
	private static final String TABLE_NAME = DBHelper.playlistTable;
	private final String[] columns = { DBHelper.colId, DBHelper.colEpisodeId };

	public PlaylistDBHelper(Context ctx)
	{
		this.ctx = ctx;
		this.dbHelper = new DBHelper(this.ctx );
	}

	/**
	 * 
	 * Sorts a List<Episode> according to the stored playlist ordering.
	 * @param playlist The list to be sorted.
	 * @return An ordered playlist containing Episode objects.
	 */
	public List<Episode> sort(List<Episode> playlist)
	{
		List<Episode> ret = new ArrayList<Episode>();
		List<EpisodePointer> pointers = new ArrayList<EpisodePointer>();
		this.db = this.dbHelper.getWritableDatabase();
		Cursor cursor = this.db.query(TABLE_NAME, columns, null, null,
				null, null, null);
		if (cursor.moveToFirst())
		{
			do
			{
				pointers.add(EpisodePointer.cursorToEpisodePointer(cursor));
			} while (cursor.moveToNext());
		}
		//Now that we have the correct order in pointers we need to reorder the playlist accordingly.
		for (EpisodePointer pointer:pointers)
		{
			
			for (int i=0; i<playlist.size(); i++)
			{
				if (playlist.get(i).getEpisodeId() == pointer.getEpisodeId()) 
				{
					//Log.i(LOG_TAG,"LOADED " + playlist.get(i).getTitle() + "(ID: " + pointer.getId() + ")");
					ret.add(playlist.get(i));
					break;
				}
			}
		}
		
		if (ret.size() != playlist.size())	//This means that there are playlist entries that have not been stored in the database. They should be added to the end and then we should save the playlist.
		{
			//Log.w(LOG_TAG, "Playlist size doesn't match number of stored pointers. Mismatch!");
			for (Episode ep:playlist)	//Go through playlist and look for the entries that haven't been added to ret yet.
			{
				if (!ret.contains(ep)) ret.add(ep);	//Add the missing entry at the last index.
			}
			this.savePlaylist(ret);	//Save the appended playlist to the db before returning.
		}
		
		return ret;
	}

	public List<EpisodePointer> loadPlaylistPointers()
	{
		//Load the pointers from the db.
		List<EpisodePointer> pointers = new ArrayList<EpisodePointer>();
		this.db = this.dbHelper.getWritableDatabase();
		Cursor cursor = this.db.query(TABLE_NAME, columns, null, null,
				null, null, null);
		if (cursor.moveToFirst())
		{
			do
			{
				pointers.add(EpisodePointer.cursorToEpisodePointer(cursor));
			} while (cursor.moveToNext());
		}
		return pointers;
	}
	
	/**
	 * 
	 * Stores the playlist in the database. (Replaces the one already there!)
	 * @param playlist Playlist to save.
	 */
	public void savePlaylist(List<Episode> playlist)
	{
		this.db = this.dbHelper.getWritableDatabase();
	    this.db.delete(TABLE_NAME, null, null);	//Clear the table.
		for (int i=0; i<playlist.size(); i++)
		{
			ContentValues values = new ContentValues();
		    values.put(columns[0], i);
		    values.put(columns[1], playlist.get(i).getEpisodeId());
		    this.db = this.dbHelper.getWritableDatabase();
		    //long insertId = this.db.insert(TABLE_NAME, null, values);
		    this.db.insert(TABLE_NAME, null, values);
		    //Log.i(LOG_TAG,"SAVED " + playlist.get(i).getTitle() + "(ID: " + insertId + ")");
		}
		Log.i(LOG_TAG, "Updated playlist!");
	}
	
	public void deleteEntry(int episodeId)
	{
		this.db = this.dbHelper.getWritableDatabase();
	    int res = this.db.delete(TABLE_NAME, columns[1] + " = " + episodeId, null);
	    if (res == 0)
	    {
	    	Log.w(LOG_TAG,"No Episode deleted");
	    }
	    else
	    {
	    	Log.w(LOG_TAG,"Episode deleted with id: " + episodeId + " from the playlist");
	    }
	}

}
