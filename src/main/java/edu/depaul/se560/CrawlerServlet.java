package edu.depaul.se560;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.net.URLDecoder;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import spark.servlet.SparkApplication;
import static spark.Spark.*;
import spark.*;



public class CrawlerServlet implements SparkApplication {

	/*Crawler object will be made available to ALL requests but given that the project requirements 
	 *did not call for a multi-threaded crawler this is ok. We'll treat the crawler as if it is a singleton.
	 */
	private CrawlerHtmlImpl crawler;
	private DataPersisterInMemoryImpl htmlDataMap = new DataPersisterInMemoryImpl();
	private HashMap<URL,URL> urlsVisited = new HashMap<URL,URL>();
	
	public String decodeURL(String url) {
		try {
			url = URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		return url;
	}

	public void init() {

		get(new Route("/") {
			@Override
			public Object handle(Request request, Response response) {
				response.status(200);
				response.type("application/xml");
				return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<api>" + "<endpoint>" + "<url>/crawler?url=http://www.depaul.edu&amp;depth=1&amp;max=1000</url>" 
				+ "<action>GET</action>" + "</endpoint>" + "<endpoint>" + "<url>/crawler/status</url>" + "<action>GET</action>" +  "</endpoint>" 
				+ "<endpoint>" + "<url>/list/documents</url>" 
				+ "<action>GET</action>"+ "</endpoint>" + "<endpoint>" + "<url>/find?url=http://www.depaul.edu</url>" 
				+ "<action>DELETE</action>"+ "</endpoint>" + "<endpoint>" + "<url>/purge?url=http://www.depaul.edu</url>" 
				+ "<action>DELETE</action>" + "</endpoint>" + "<endpoint>" + "<url>/purge/all</url>" 
				+ "<action>GET</action>" + "</endpoint>" + "<endpoint>" + "<url>/query/xpath?expression=//[contains.http://www.depaul.edu</url>" 
				+ "<action>GET</action>" + "</endpoint>" + "<endpoint>" + "<url>/query/stax?element=foo&amp;text=bar</url>" 
				+ "<action>GET</action>" + "</endpoint>" + "</api>"; 
			}
		});

		get(new Route("/crawler") {
			@Override
			public Object handle(Request request, Response response) {
				String url = request.queryParams("url");
				String depth = request.queryParams("depth");
				String max = request.queryParams("max");
				long elapsedTime;

				if (url == null || url == "" || depth == null || depth == "" || max == null || max == "") {
					response.status(400);
					return "Invalid/Missing Query Parameters";
				}

				//Verify the URL passed is valid
				URL testConnection = null;
				try {
					if (!url.startsWith("http://")) {
						url = "http://" + url;
					}
					testConnection = new URL(url);
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
				try {
					testConnection.openConnection();
				} catch (IOException e1) {
					e1.printStackTrace();
					response.status(400);
					return "Cannot resolve hostname. Check URL.";
				}

				if(crawler == null || !crawler.isCrawlInProgress()) {
					crawler = new CrawlerHtmlImpl(url,urlsVisited);
					crawler.setMaxDepth(Integer.parseInt(depth));
					crawler.setMaxURLToVisit(Integer.parseInt(max));
					crawler.setDataPersister(htmlDataMap);
					try {
						crawler.crawl();
						//Depending on the depth/breadth of the graph the max URL set may be less than the actual number of possible urls to visit
						crawler.setMaxURLToVisit(0);
						crawler.setNodesTraversed(0);
					} catch (IOException e) {
						e.printStackTrace();
					}
					elapsedTime = crawler.getTimeElapsed();
					response.type("application/xml");
					response.status(200);
					return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<crawler>" + "<summary>" + "<url>" + url + "</url>" 
					+ "<depth>" + depth + "</depth>" + "<max>" + max + "</max>" +"<processingtime>" + elapsedTime + "</processingtime>" +  "</summary>" + "</crawler>"; 
				}
				response.redirect("/crawler/status");
				return "";
			}
		});


		get(new Route("/crawler/status") {
			@Override
			public Object handle(Request request,Response response) {
				//A crawler object needs to be instantiated before the status can be run against it
				if(crawler != null && crawler.isCrawlInProgress()) {
					response.type("application/xml");
					response.status(200);
					return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<crawler>" + "<status>" +  "<rooturl>" + crawler.getRootURL() + "</rooturl>" 
					+ "<nodesvisited>" + crawler.getNodesTraversed() + "</nodesvisited>" + "<started>" + crawler.getCrawlStartTime() + "</started>"
					+ "<nodesleft>" + crawler.getNodesLeftToVisit() + "</nodesleft>" + "</status> " + "</crawler>"; 
				}
				response.type("application/xml");
				response.status(200);
				return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<crawler/>";
			}

		});

		get(new Route("/find") {
			@Override
			public Object handle(Request request, Response response) {
				String urlQuery = request.queryParams("url");
				urlQuery = decodeURL(urlQuery);
				URL url = null;
				if (urlQuery == null || urlQuery == "") {
					response.status(400); 
					return "Invalid/Missing Query Parameter";
				}
				try {
					if(!urlQuery.startsWith("http://")) {
						urlQuery = "http://" + urlQuery;
						url = new URL(urlQuery);
					}
					else {
						url = new URL(urlQuery);
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

				if(htmlDataMap.containsURL(url)) {
					byte[] htmlAsBytes = htmlDataMap.getData(url);
					response.status(200);
					return new String(htmlAsBytes);
				}

				response.status(404);
				return "Document corresponding to " +  urlQuery + " not found";
			}
		});

		delete(new Route("/purge") {
			@Override
			public Object handle(Request request, Response response) {
				String urlQuery = request.queryParams("url");
				urlQuery = decodeURL(urlQuery);
				URL url = null;
				if (urlQuery == null || urlQuery == "") {
					response.status(400); 
					return "Invalid/Missing Query Parameter";
				}
				try {
					if(!urlQuery.startsWith("http://")) {
						urlQuery = "http://" + urlQuery;
						url = new URL(urlQuery);
					}
					else {
						url = new URL(urlQuery);
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				if(htmlDataMap.containsURL(url) && urlsVisited.containsKey(url)) {
					htmlDataMap.delete(url);
					urlsVisited.remove(url);
					response.status(200);
					return "Resource purged from database";
				}

				response.status(404);
				return "Resource not found";

			}
		});

		delete(new Route("/purge/all") {
			@Override
			public Object handle(Request request, Response response) {
				int docCount = htmlDataMap.size();
				if(docCount > 0) {
					htmlDataMap.deleteAll();
					urlsVisited.clear();
					response.status(200);
					response.type("application/xml");
					return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<crawler>" + "<documents>" + "<purged>" + docCount + "</purged>" + "</documents>" + "</crawler>";
				}
				response.status(404);
				return "<crawler/>";
			}
		});



		get(new Route("/list/documents") {
			@Override
			public Object handle(Request request, Response response) {
				if(htmlDataMap.size() > 0) {
					Set keySet = htmlDataMap.getKeySet();
					String docs = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<crawler>" + "<documents>";
					Iterator itr = keySet.iterator();

					while(itr.hasNext()) {
						String url = itr.next().toString();
						if(url.contains("&")) {
							url = url.replaceAll("&", "&amp;");
						}
						docs += "<url>" + url + "</url>";
					}
					docs += "</documents>" + "</crawler>";
					response.type("application/xml");
					response.status(200);
					return docs;
				}
				response.type("application/xml");
				response.status(404);
				return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<crawler/>";
			}
		});

		get(new Route("/query/xpath") {
			@Override
			public Object handle(Request request, Response response) {
				String xPression = null;
				try {
					xPression = URLDecoder.decode(request.queryParams("expression"), "UTF-8");
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}

				if(xPression == null || xPression == "") {
					response.status(400); 
					return "Missing query parameter";
				}


				if (!XPathParser.validXPression(xPression)) {
					response.status(400);
					response.type("application/xml");
					return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<crawler/>";
				}	

				Set keySet = htmlDataMap.getKeySet();
				String docs = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<crawler>" + "<documents>";
				Iterator itr = keySet.iterator();

				while(itr.hasNext()) {
					URL url = (URL) itr.next();
					byte[] xHtmlAsBytes = htmlDataMap.getData(url);
					String xHtml = new String(xHtmlAsBytes);

					StringReader sr = new StringReader(xHtml);
					InputSource is = new InputSource(sr);
					XPathParser parser = new XPathParser(xPression, is);
					try {
						if(!parser.evaluate()) {
							continue;
						}
					} catch (XPathExpressionException e) {
						e.printStackTrace();
						continue;
					} catch (ParserConfigurationException e) {
						e.printStackTrace();
						continue;
					} catch (SAXException e) {
						e.printStackTrace();
						continue;
					} catch (IOException e) {
						e.printStackTrace();
						continue;
					}
					String urlString = url.toString();
					if(urlString.contains("&")) {
						urlString = urlString.replaceAll("&", "&amp");
					}
					docs += "<url>" + urlString + "</url>";
				}
				response.status(200);
				response.type("application/xml");
				docs += "</documents>" + "</crawler>";
				return docs;
			}
		});

		get(new Route("/query/stax") {
			@Override
			public Object handle(Request request, Response response) {
				String element = request.queryParams("element");
				String text = request.queryParams("text");

				if(element == null || element == "" || text == null || text == "") {
					response.status(400); 
					return "Invalid/Missing Query Parameter";
				}

				Set keySet = htmlDataMap.getKeySet();
				String docs = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<crawler>" + "<documents>";
				Iterator itr = keySet.iterator();

				while(itr.hasNext()) {
					URL url = (URL) itr.next();
					byte[] xHtmlAsBytes = htmlDataMap.getData(url);
					String xHtml = new String(xHtmlAsBytes);

					StringReader sr = new StringReader(xHtml);
					StaxParser sp = new StaxParser(element, text, sr);
					try {
						if(sp.parse()) {
							String urlString = url.toString();
							if(urlString.contains("&")) {
								urlString = urlString.replaceAll("&", "&amp");
							}
							docs += "<url>" + urlString + "</url>";
						}
					} catch (XMLStreamException e) {
						e.printStackTrace();
					}
				}
				response.status(200);
				response.type("application/xml");
				docs += "</documents>" + "</crawler>";
				return docs;
			}
		});

	}
}
