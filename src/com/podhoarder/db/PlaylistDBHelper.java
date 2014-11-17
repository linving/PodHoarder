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
import java.util.Queue;

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
	public void savePlaylist(Queue<Episode> playlist)
	{
        Episode[] templist = playlist.toArray(new Episode[]{});
		this.db = this.dbHelper.getWritableDatabase();
	    this.db.delete(TABLE_NAME, null, null);	//Clear the table.
		for (int i=0; i<templist.length; i++)
		{
			ContentValues values = new ContentValues();
		    values.put(columns[0], i);
		    values.put(columns[1], templist[i].getEpisodeId());
		    this.db = this.dbHelper.getWritableDatabase();
		    //long insertId = this.db.insert(TABLE_NAME, null, values);
		    this.db.insert(TABLE_NAME, null, values);
		    //Log.i(LOG_TAG,"SAVED " + playlist.get(i).getTitle() + "(ID: " + insertId + ")");
		}
        templist = null;
		Log.i(LOG_TAG, "Updated playlist!");
	}
}
