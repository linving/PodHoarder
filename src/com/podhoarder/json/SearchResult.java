package com.podhoarder.json;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;

public class SearchResult
{

	@Expose
	private Integer resultCount;
	@Expose
	private List<SearchResultItem> results = new ArrayList<SearchResultItem>();

	public Integer getResultCount()
	{
		return resultCount;
	}

	public void setResultCount(Integer resultCount)
	{
		this.resultCount = resultCount;
	}

	public List<SearchResultItem> getResults()
	{
		return results;
	}

	public void setResults(List<SearchResultItem> results)
	{
		this.results = results;
	}

}