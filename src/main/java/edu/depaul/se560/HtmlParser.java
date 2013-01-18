package edu.depaul.se560;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.htmlcleaner.*;


public class HtmlParser {

	/*
	 * Basic wrapper class around the HTMLCleaner (http://htmlcleaner.sourceforge.net/) library. 
	 * I decided to go with the HTMLCleaner library instead of JTidy primarily because the documentation
	 * was much more detailed and the latest version of the HTMLCleaner is available in Maven central. 
	 */

	private HtmlCleaner cleaner = new HtmlCleaner();
	private TagNode rootNode;
	private URL rootURL;
	
	public HtmlParser(URL url) {
		rootURL = url;
	}
	
	public String htmlToXHTML() throws IOException {
		rootNode = cleaner.clean(rootURL,"UTF-8");
		
		CleanerProperties props = cleaner.getProperties();
		//Parser properties
		props.setOmitXmlDeclaration(false);
		props.setOmitDoctypeDeclaration(false);
		props.setOmitHtmlEnvelope(false);
		props.setUseCdataForScriptAndStyle(false);
		props.setAllowMultiWordAttributes(true);
		props.setRecognizeUnicodeChars(true);
		
		
		SimpleXmlSerializer serial = new SimpleXmlSerializer(props);
		String xHTML = serial.getAsString(rootNode,"UTF-8");

		return xHTML;


	}

	public ArrayList<URL> getAllLinks() throws MalformedURLException {
		TagNode[] links = rootNode.getElementsByName("a", true);
		ArrayList<URL> urls = new ArrayList<URL>();

		for (TagNode href : links) {
			String link = href.getAttributeByName("href");
			if (link != null && link.length() > 0 ) {
				if(isAnchorLink(link)) {
					continue;
				}

				if(isAbsoluteLink(link)) {
					urls.add(new URL(link));
					continue;
				}

				if (isRelativeLink(link)) {
					if(!link.startsWith("/")) {
						String url = rootURL + "/" + link;
						urls.add(new URL(url));
						continue;
					}
					String url = rootURL + link;
					urls.add(new URL(url));
					continue;
				}
			}

		}

		return urls;
	}

	public boolean isAnchorLink(String hrefValue) {
		if (hrefValue.startsWith("#") || hrefValue.startsWith("/#")) {
			return true;
		}
		return false;
	}

	public boolean isJavaScriptLink(String hrefValue) {
		if(hrefValue.startsWith("javascript:")) {
			return true;
		}
		return false;
	}

	public boolean isMailToLink(String hrefValue) {
		if(hrefValue.startsWith("mailto:")) {
			return true;
		}
		return false;
	}

	public boolean isRelativeLink(String hrefValue) {
		if (hrefValue.startsWith("/")) {
			return true;
		}

		if (!isAbsoluteLink(hrefValue) && !isAnchorLink(hrefValue) && !isJavaScriptLink(hrefValue) && !isMailToLink(hrefValue)) {
			return true;
		}
		return false;
	}

	public boolean isAbsoluteLink(String hrefValue) {
		if(hrefValue.startsWith("http://") || hrefValue.startsWith("https://")) {
			return true;
		}
		return false;
	}


}
