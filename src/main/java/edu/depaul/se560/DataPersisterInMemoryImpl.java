package edu.depaul.se560;

import java.net.URL;
import java.util.HashMap;
import java.util.Set;


public class DataPersisterInMemoryImpl implements DataPersister {
	private HashMap<URL,byte[]> urlDataMap = new HashMap<URL,byte[]>();

	public DataPersisterInMemoryImpl() {
	}

	public void insertURL(URL url, byte[] data) {
		urlDataMap.put(url, data);
	}

	public boolean containsURL(URL url) {
		if (urlDataMap.containsKey(url)) {
			return true;
		}
		return false;
	}

	public byte[] getData(URL url) {
		return urlDataMap.get(url);
	}

	public void delete(URL url) {
		urlDataMap.remove(url);
			
	}
	
	public Set getKeySet() {
		return urlDataMap.keySet();
	}
	
	public int size() {
		return urlDataMap.size();
	}
	
	public void deleteAll() {
		urlDataMap.clear();
	}

}
