package com.podhoarder.object;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchResultRow implements Parcelable
{
	private String title;
	private String author;
	private String description;
	private String link;
	private String category;
	private String imageUrl;
	private Document xml;
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

	public String getLastUpdated()
	{
		return lastUpdated;
	}
	public void setLastUpdated(String lastUpdated)
	{
		this.lastUpdated = lastUpdated;
	}
	public void startImageDownload()
	{
		
	}

	
	public String getStringFromDocument(Document doc)
	{
	    try
	    {
	       DOMSource domSource = new DOMSource(doc);
	       StringWriter writer = new StringWriter();
	       StreamResult result = new StreamResult(writer);
	       TransformerFactory tf = TransformerFactory.newInstance();
	       Transformer transformer = tf.newTransformer();
	       transformer.transform(domSource, result);
	       return writer.toString();
	    }
	    catch(TransformerException ex)
	    {
	       ex.printStackTrace();
	       return null;
	    }
	}
	
	@Override
	public int describeContents()
	{
		return 0;
	}
	

	@Override
	public void writeToParcel(Parcel p, int flags)
	{
		// TODO Auto-generated method stub
		p.writeString(title);
		p.writeString(author);
		p.writeString(description);
		p.writeString(link);
		p.writeString(category);
		p.writeString(imageUrl);
		p.writeString(getStringFromDocument(xml));
		p.writeString(lastUpdated);
	}
	
	private void readFromParcel(Parcel in) 
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		InputSource is = new InputSource();
		try
		{
			db = dbf.newDocumentBuilder();
			
			title = in.readString();
			author = in.readString();
			description = in.readString();
			link = in.readString();
			category = in.readString();
			imageUrl = in.readString();
			
			is.setCharacterStream(new StringReader(in.readString()));
			
			xml = db.parse(is);
		} catch (ParserConfigurationException e1)
		{
			e1.printStackTrace();
		} catch (SAXException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
