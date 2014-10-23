package com.podhoarder.object;

import android.os.Parcel;
import android.os.Parcelable;

import com.podhoarder.util.FileUtils;

import org.w3c.dom.Document;

public class SearchResultRow implements Parcelable
{
	private String title;
	private String author;
	private String description;
	private String link;
	private String category;
	private String imageUrl;
	private Document xml;
    private String  cachedFileName;
	private String lastUpdated;
	
	public SearchResultRow() {	}
	public SearchResultRow(Parcel in) 
	{ 
		readFromParcel(in); 
	}
		
	public String getTitle()
	{
		return title;
	}
	public void setTitle(String title)
	{
		this.title = title;
	}
	public String getAuthor()
	{
		return author;
	}
	public void setAuthor(String author)
	{
		this.author = author;
	}
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}
	public String getLink()
	{
		return link;
	}
	public void setLink(String link)
	{
		this.link = link;
	}
	public String getCategory()
	{
		return category;
	}
	public void setCategory(String category)
	{
		this.category = category;
	}
	public String getImageUrl()
	{
		return imageUrl;
	}
	public void setImageUrl(String imageUrl)
	{
		this.imageUrl = imageUrl;
	}

	public Document getXml()
	{
		return xml;
	}
	public void setXml(Document xml)
	{
		this.xml = xml;
	}
    public String getCachedFileName() {return this.cachedFileName; }

	public String getLastUpdated()
	{
		return lastUpdated;
	}
	public void setLastUpdated(String lastUpdated)
	{
		this.lastUpdated = lastUpdated;
	}

    /**
     * Caches the stored XML file.
     * @return True if the file was cached. False otherwise.
     */
	public boolean cacheXml()
	{
        this.cachedFileName = FileUtils.cacheXML(this.xml);
        if (this.cachedFileName != null || !this.cachedFileName.isEmpty()) {
            this.xml = null;
            return true;
        }
        else
            return false;

	}

    /**
     * Loads the cached xml file.
     * @return True if the file was loaded. False otherwise.
     */
    public boolean loadXML()
    {
        if (this.cachedFileName != null || !this.cachedFileName.isEmpty())
            this.xml = FileUtils.loadCachedXml(this.cachedFileName);

        if (this.xml != null) {
            this.cachedFileName = "";
            return true;
        }
        else
            return false;
    }
	
	@Override
	public int describeContents()
	{
		return 0;
	}
	

	@Override
	public void writeToParcel(Parcel p, int flags)
	{
		p.writeString(title);
		p.writeString(author);
		p.writeString(description);
		p.writeString(link);
		p.writeString(category);
		p.writeString(imageUrl);
		p.writeString(cachedFileName);
		p.writeString(lastUpdated);
	}
	
	private void readFromParcel(Parcel in) 
	{
        title = in.readString();
        author = in.readString();
        description = in.readString();
        link = in.readString();
        category = in.readString();
        imageUrl = in.readString();
        cachedFileName = in.readString();
		lastUpdated = in.readString();
	}
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
	{
		public SearchResultRow createFromParcel(Parcel in)
		{
			return new SearchResultRow(in);
		}

		public SearchResultRow[] newArray(int size)
		{
			return new SearchResultRow[size];
		}
	};
}
