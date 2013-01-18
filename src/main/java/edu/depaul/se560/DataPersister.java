package edu.depaul.se560;

import java.net.URL;

public interface DataPersister {
	public void insertURL(URL url, byte[] data);
	
	boolean containsURL(URL url);
	
	byte[] getData (URL url);
	
	public void delete(URL url);

}
