package com.ar.myfirstmashup.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.springframework.beans.factory.annotation.Value;

import com.ar.myfirstmashup.beans.YoutubeBean;
import com.ar.myfirstmashup.util.Auth;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;

public class MyYoutubeSearch implements Callable {

	@Value("${youtube.apikey}")
	private String api_key;

	private static final long NUMBER_OF_VIDEOS_RETURNED = 25;

	/**
	 * Define a global instance of a Youtube object, which will be used to make
	 * YouTube Data API requests.
	 */
	private static YouTube youtube;

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {

		// Read the developer key from the properties file.

		try {
			// This object is used to make YouTube Data API requests. The last
			// argument is required, but since we don't need anything
			// initialized when the HttpRequest is initialized, we override
			// the interface and provide a no-op function.
			youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT,
					Auth.JSON_FACTORY, new HttpRequestInitializer() {
						public void initialize(HttpRequest request)
								throws IOException {
						}
					}).setApplicationName("youtube-cmdline-search-sample")
					.build();

			// Prompt the user to enter a query term.
			String queryTerm = (String) eventContext.getMessage()
					.getInvocationProperty("keyword");

			// Define the API request for retrieving search results.
			YouTube.Search.List search = youtube.search().list("id,snippet");

			// Set your developer key from the Google Developers Console for
			// non-authenticated requests. See:
			// https://console.developers.google.com/
			search.setKey(api_key);
			search.setQ(queryTerm);

			// Restrict the search results to only include videos. See:
			// https://developers.google.com/youtube/v3/docs/search/list#type
			search.setType("video");

			// To increase efficiency, only retrieve the fields that the
			// application uses.
			search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
			search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

			// Call the API and print results.
			SearchListResponse searchResponse = search.execute();
			List<SearchResult> searchResultList = searchResponse.getItems();
			if (searchResultList != null) {
				return prettyPrint(searchResultList.iterator(), queryTerm);
			}
		} catch (GoogleJsonResponseException e) {
			System.err.println("There was a service error: "
					+ e.getDetails().getCode() + " : "
					+ e.getDetails().getMessage());
		} catch (IOException e) {
			System.err.println("There was an IO error: " + e.getCause() + " : "
					+ e.getMessage());
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return null;
	}

	private List<YoutubeBean>  prettyPrint(
			Iterator<SearchResult> iteratorSearchResults, String query) {

		System.out
				.println("\n=============================================================");
		System.out.println("   First " + NUMBER_OF_VIDEOS_RETURNED
				+ " videos for search on \"" + query + "\".");
		System.out
				.println("=============================================================\n");

		if (!iteratorSearchResults.hasNext()) {
			System.out.println(" There aren't any results for your query.");
		}
		List<YoutubeBean> searchList = new ArrayList<YoutubeBean>();
		while (iteratorSearchResults.hasNext()) {

			SearchResult singleVideo = iteratorSearchResults.next();
			ResourceId rId = singleVideo.getId();

			// Confirm that the result represents a video. Otherwise, the
			// item will not contain a video ID.
			if (rId.getKind().equals("youtube#video")) {
				Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails()
						.getDefault();
				YoutubeBean youtubeBean = new YoutubeBean();
				youtubeBean.setTitle(singleVideo.getSnippet().getTitle());
				youtubeBean.setVideoId(rId.getVideoId());
				youtubeBean.setUrl(thumbnail.getUrl());
				searchList.add(youtubeBean);
				System.out.println(" Video Id" + rId.getVideoId());
				System.out.println(" Title: "
						+ singleVideo.getSnippet().getTitle());
				System.out.println(" Thumbnail: " + thumbnail.getUrl());
				System.out
						.println("\n-------------------------------------------------------------\n");
			}
		}
		return searchList;
	}
}
