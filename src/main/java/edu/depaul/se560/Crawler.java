
package edu.depaul.se560;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * @author Youssuf ElKalay
 *
 */
public interface Crawler {
		
	public void setMaxDepth(int maxDepth);
	
	public int getMaxDepth();
	
	public void setMaxURLToVisit(int max);
	
	public int getMaxURLToVisit();
		
	public int getNodesTraversed();
	
	public int getNodesLeftToVisit();
		
	public URL getRootURL();
		
	public void crawl() throws MalformedURLException, IOException;

}
