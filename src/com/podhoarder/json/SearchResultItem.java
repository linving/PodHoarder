package com.podhoarder.json;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;

public class SearchResultItem
{

	@Expose
	private String wrapperType;
	@Expose
	private String kind;
	@Expose
	private Integer artistId;
	@Expose
	private Integer collectionId;
	@Expose
	private Integer trackId;
	@Expose
	private String artistName;
	@Expose
	private String collectionName;
	@Expose
	private String trackName;
	@Expose
	private String collectionCensoredName;
	@Expose
	private String trackCensoredName;
	@Expose
	private String artistViewUrl;
	@Expose
	private String collectionViewUrl;
	@Expose
	private String feedUrl;
	@Expose
	private String trackViewUrl;
	@Expose
	private String artworkUrl30;
	@Expose
	private String artworkUrl60;
	@Expose
	private String artworkUrl100;
	@Expose
	private Double collectionPrice;
	@Expose
	private Double trackPrice;
	@Expose
	private Integer trackRentalPrice;
	@Expose
	private Integer collectionHdPrice;
	@Expose
	private Integer trackHdPrice;
	@Expose
	private Integer trackHdRentalPrice;
	@Expose
	private String releaseDate;
	@Expose
	private String collectionExplicitness;
	@Expose
	private String trackExplicitness;
	@Expose
	private Integer trackCount;
	@Expose
	private String country;
	@Expose
	private String currency;
	@Expose
	private String primaryGenreName;
	@Expose
	private String contentAdvisoryRating;
	@Expose
	private String radioStationUrl;
	@Expose
	private String artworkUrl600;
	@Expose
	private List<String> genreIds = new ArrayList<String>();
	@Expose
	private List<String> genres = new ArrayList<String>();

	public String getWrapperType()
	{
		return wrapperType;
	}

	public void setWrapperType(String wrapperType)
	{
		this.wrapperType = wrapperType;
	}

	public String getKind()
	{
		return kind;
	}

	public void setKind(String kind)
	{
		this.kind = kind;
	}

	public Integer getArtistId()
	{
		return artistId;
	}

	public void setArtistId(Integer artistId)
	{
		this.artistId = artistId;
	}

	public Integer getCollectionId()
	{
		return collectionId;
	}

	public void setCollectionId(Integer collectionId)
	{
		this.collectionId = collectionId;
	}

	public Integer getTrackId()
	{
		return trackId;
	}

	public void setTrackId(Integer trackId)
	{
		this.trackId = trackId;
	}

	public String getArtistName()
	{
		return artistName;
	}

	public void setArtistName(String artistName)
	{
		this.artistName = artistName;
	}

	public String getCollectionName()
	{
		return collectionName;
	}

	public void setCollectionName(String collectionName)
	{
		this.collectionName = collectionName;
	}

	public String getTrackName()
	{
		return trackName;
	}

	public void setTrackName(String trackName)
	{
		this.trackName = trackName;
	}

	public String getCollectionCensoredName()
	{
		return collectionCensoredName;
	}

	public void setCollectionCensoredName(String collectionCensoredName)
	{
		this.collectionCensoredName = collectionCensoredName;
	}

	public String getTrackCensoredName()
	{
		return trackCensoredName;
	}

	public void setTrackCensoredName(String trackCensoredName)
	{
		this.trackCensoredName = trackCensoredName;
	}

	public String getArtistViewUrl()
	{
		return artistViewUrl;
	}

	public void setArtistViewUrl(String artistViewUrl)
	{
		this.artistViewUrl = artistViewUrl;
	}

	public String getCollectionViewUrl()
	{
		return collectionViewUrl;
	}

	public void setCollectionViewUrl(String collectionViewUrl)
	{
		this.collectionViewUrl = collectionViewUrl;
	}

	public String getFeedUrl()
	{
		return feedUrl;
	}

	public void setFeedUrl(String feedUrl)
	{
		this.feedUrl = feedUrl;
	}

	public String getTrackViewUrl()
	{
		return trackViewUrl;
	}

	public void setTrackViewUrl(String trackViewUrl)
	{
		this.trackViewUrl = trackViewUrl;
	}

	public String getArtworkUrl30()
	{
		return artworkUrl30;
	}

	public void setArtworkUrl30(String artworkUrl30)
	{
		this.artworkUrl30 = artworkUrl30;
	}

	public String getArtworkUrl60()
	{
		return artworkUrl60;
	}

	public void setArtworkUrl60(String artworkUrl60)
	{
		this.artworkUrl60 = artworkUrl60;
	}

	public String getArtworkUrl100()
	{
		return artworkUrl100;
	}

	public void setArtworkUrl100(String artworkUrl100)
	{
		this.artworkUrl100 = artworkUrl100;
	}

	public Double getCollectionPrice()
	{
		return collectionPrice;
	}

	public void setCollectionPrice(Double collectionPrice)
	{
		this.collectionPrice = collectionPrice;
	}

	public Double getTrackPrice()
	{
		return trackPrice;
	}

	public void setTrackPrice(Double trackPrice)
	{
		this.trackPrice = trackPrice;
	}

	public Integer getTrackRentalPrice()
	{
		return trackRentalPrice;
	}

	public void setTrackRentalPrice(Integer trackRentalPrice)
	{
		this.trackRentalPrice = trackRentalPrice;
	}

	public Integer getCollectionHdPrice()
	{
		return collectionHdPrice;
	}

	public void setCollectionHdPrice(Integer collectionHdPrice)
	{
		this.collectionHdPrice = collectionHdPrice;
	}

	public Integer getTrackHdPrice()
	{
		return trackHdPrice;
	}

	public void setTrackHdPrice(Integer trackHdPrice)
	{
		this.trackHdPrice = trackHdPrice;
	}

	public Integer getTrackHdRentalPrice()
	{
		return trackHdRentalPrice;
	}

	public void setTrackHdRentalPrice(Integer trackHdRentalPrice)
	{
		this.trackHdRentalPrice = trackHdRentalPrice;
	}

	public String getReleaseDate()
	{
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate)
	{
		this.releaseDate = releaseDate;
	}

	public String getCollectionExplicitness()
	{
		return collectionExplicitness;
	}

	public void setCollectionExplicitness(String collectionExplicitness)
	{
		this.collectionExplicitness = collectionExplicitness;
	}

	public String getTrackExplicitness()
	{
		return trackExplicitness;
	}

	public void setTrackExplicitness(String trackExplicitness)
	{
		this.trackExplicitness = trackExplicitness;
	}

	public Integer getTrackCount()
	{
		return trackCount;
	}

	public void setTrackCount(Integer trackCount)
	{
		this.trackCount = trackCount;
	}

	public String getCountry()
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String getCurrency()
	{
		return currency;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	public String getPrimaryGenreName()
	{
		return primaryGenreName;
	}

	public void setPrimaryGenreName(String primaryGenreName)
	{
		this.primaryGenreName = primaryGenreName;
	}

	public String getContentAdvisoryRating()
	{
		return contentAdvisoryRating;
	}

	public void setContentAdvisoryRating(String contentAdvisoryRating)
	{
		this.contentAdvisoryRating = contentAdvisoryRating;
	}

	public String getRadioStationUrl()
	{
		return radioStationUrl;
	}

	public void setRadioStationUrl(String radioStationUrl)
	{
		this.radioStationUrl = radioStationUrl;
	}

	public String getArtworkUrl600()
	{
		return artworkUrl600;
	}

	public void setArtworkUrl600(String artworkUrl600)
	{
		this.artworkUrl600 = artworkUrl600;
	}

	public List<String> getGenreIds()
	{
		return genreIds;
	}

	public void setGenreIds(List<String> genreIds)
	{
		this.genreIds = genreIds;
	}

	public List<String> getGenres()
	{
		return genres;
	}

	public void setGenres(List<String> genres)
	{
		this.genres = genres;
	}

}