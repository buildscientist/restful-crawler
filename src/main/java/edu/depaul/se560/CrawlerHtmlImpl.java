/**
 * 
 */
package edu.depaul.se560;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Queue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.*;
import java.util.Date;

import edu.depaul.se560.HtmlParser;
import edu.depaul.se560.DataPersisterInMemoryImpl;

/**
 * @author Youssuf ElKalay
 *
 */
public class CrawlerHtmlImpl implements Crawler {

	private int depth;
	private int nodeDepth;
	private int maxURLToVisit = 1000;
	private int nodesVisited;
	private int nodesLeftToVisit;
	private Queue<ArrayList<URL>> urlQueue = new ConcurrentLinkedQueue<ArrayList<URL>>();
	private HashMap<URL,URL> urlsVisited = new HashMap<URL,URL>();
	private URL rootURL;
	private DataPersisterInMemoryImpl urlDataMap;
	private Date crawlStartDateTime;
	private boolean crawlInProgress = false;
	private long startTime;
	private long endTime;


	public CrawlerHtmlImpl(String url,HashMap<URL,URL> urlsVisitedMap) {
		try {
			if(!url.startsWith("http://")) {
				String normalizedURL = "http://" + url;
				rootURL = new URL(normalizedURL);
			}
			else {
				rootURL = new URL(url);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		urlsVisited = urlsVisitedMap;

		//Create the first list of URL's to crawl
		ArrayList<URL> rootURLList = new ArrayList<URL>();
		rootURLList.add(rootURL);
		urlQueue.offer(rootURLList);
	}

	public long getTimeElapsed() {
		return endTime - startTime;

	}

	public void setDataPersister(DataPersister persister) {
		urlDataMap = (DataPersisterInMemoryImpl) persister;
	}

	public void setMaxDepth(int maxDepth) {
		this.depth = maxDepth;

	}

	public int getMaxDepth() {
		return depth;
	}

	public void setMaxURLToVisit(int max) {
		maxURLToVisit = max;
	}

	public int getMaxURLToVisit() {
		return maxURLToVisit;
	}

	public int getNodesTraversed() {
		return nodesVisited;
	}

	public void setNodesTraversed(int visited) {
		nodesVisited = visited;
	}

	public int getNodesLeftToVisit() {
		nodesLeftToVisit = maxURLToVisit - nodesVisited;
		return nodesLeftToVisit;
	}

	public URL getRootURL() {
		return rootURL;
	}

	public String getCrawlStartTime() {
		return crawlStartDateTime.toString();

	}

	public boolean isCrawlInProgress() { 
		return crawlInProgress;
	}

	public void crawl() throws IOException {

		/*Breadth first search implemented using a queue of arraylists of urls 
		 * The depth is the maintained by the order in which the arraylist of urls is added to the queue
		 */
		crawlStartDateTime = new Date();
		startTime = System.currentTimeMillis();
		while (!urlQueue.isEmpty() && nodeDepth <= depth && nodesVisited <= maxURLToVisit) {
			crawlInProgress = true;
			ArrayList<URL> urls = urlQueue.poll();
			ArrayList<URL> newLinks = new ArrayList<URL>();

			//A url is a node. Keep track of depth of urls processed
			nodeDepth++;

			for (URL url : urls) {
				if (!urlsVisited.containsKey(url) && nodesVisited <= maxURLToVisit)	{
					System.out.println("Crawling:" + url.toString());
					nodesVisited++;
					urlsVisited.put(url, url);

					HtmlParser parser = new HtmlParser(rootURL);
					byte[] xHtmlAsBytes = parser.htmlToXHTML().getBytes();

					//In memory hashmap of urls and byte arrays
					urlDataMap.insertURL(url, xHtmlAsBytes);

					ArrayList<URL> links = parser.getAllLinks();
					
					System.out.println("Total nodes visited thus far:" + nodesVisited);

					for (URL x : links) {
						if (!urlsVisited.containsKey(x)) {
							newLinks.add(x);
						}
					}
				}
			}
			//Add the newly made list of urls to the end of the queue
			urlQueue.offer(newLinks);
		}
		crawlInProgress = false;
		endTime = System.currentTimeMillis();
	}
}
