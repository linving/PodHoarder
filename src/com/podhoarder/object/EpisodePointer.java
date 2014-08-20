package com.podhoarder.object;

import android.database.Cursor;

/**
 * @author Emil Almrot
 * 2014-05-25
 */
public class EpisodePointer
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.EpisodePointer";
	
	private int id;
	private int episodeId;
	
	public EpisodePointer(int id, int episodeId)
	{
		this.id = id;
		this.episodeId = episodeId;
	}
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public int getEpisodeId()
	{
		return episodeId;
	}
	public void setEpisodeId(int episodeId)
	{
		this.episodeId = episodeId;
	}
	
	/**
	 * Converts a Cursor object to a Feed object.
	 * @param c	Cursor containing the data.
	 * @return	A new Feed object.
	 */
	public static EpisodePointer cursorToEpisodePointer(Cursor c)
	{
		EpisodePointer pointer = new EpisodePointer(Integer.parseInt(c.getString(0)), Integer.parseInt(c.getString(1)));
		return pointer;
	}
}
