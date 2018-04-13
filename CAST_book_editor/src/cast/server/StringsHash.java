package cast.server;

import java.util.*;
import java.io.*;
import java.net.*;



public class StringsHash extends Hashtable<String, String> {
	public StringsHash(URL url) {
		super();
		try {
			parse(url.openStream());
		} catch (IOException e) {
//			System.err.println("Cannot find custom books on server");
		}
	}
	
	public StringsHash(File f) {
		try {
			InputStream is = new FileInputStream(f);
			parse(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public StringsHash() {
		super();
	}
	
	private void parse(InputStream is) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			
			String nextEntry;
			while((nextEntry = in.readLine()) != null) {
				int equalsIndex = nextEntry.indexOf("=");
				if (equalsIndex > 0) {
					String theKey = nextEntry.substring(0, equalsIndex);
					String stringValue = nextEntry.substring(equalsIndex + 1);
					put(theKey, stringValue);
				}
			}
		} catch (UnsupportedEncodingException e) {
		} catch (IOException e) {
			System.err.println("Could not read bookNames file");
			e.printStackTrace();
		}
	}
	
	public String getStringValue(String theKey) {
		return get(theKey);
	}
	
	public void saveToFile(File dateStampFile) {
		try {
			PrintWriter out = new PrintWriter(dateStampFile);
			Enumeration<String> e = keys();
			while(e.hasMoreElements()) {
				String theKey = e.nextElement();
				if (theKey.startsWith("var ")) {					//	sorted so that the declarations in releaseInfo.js come before assignments
					String theString = get(theKey);
					out.println(theKey + "=" + theString);
				}
			}
			e = keys();
			while(e.hasMoreElements()) {
				String theKey = e.nextElement();
				if (!theKey.startsWith("var ")) {
					String theString = get(theKey);
					out.println(theKey + "=" + theString);
				}
			}
			out.close();
		} catch (IOException e) {
			System.err.println(e.toString());
			e.printStackTrace();
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		Enumeration<String> e = keys();
		while (e.hasMoreElements()) {
			String theKey = e.nextElement();
			if (theKey.startsWith("var ")) {					//	sorted so that the declarations in releaseInfo.js come before assignments
				String stringValue = get(theKey);
				if (sb.length() > 0)
					sb.append("\n");
				sb.append(theKey + "=" + stringValue);
			}
		}
		
		e = keys();
		while (e.hasMoreElements()) {
			String theKey = e.nextElement();
			if (!theKey.startsWith("var ")) {
				String stringValue = get(theKey);
				if (sb.length() > 0)
					sb.append("\n");
				sb.append(theKey + "=" + stringValue);
			}
		}
		return sb.toString();
	}
	
	public void printBooks() {
		Enumeration<String> e = keys();
		while (e.hasMoreElements()) {
			String theKey = e.nextElement();
			String theString = get(theKey);
			System.out.println(theKey + ": " + theString);
		}
	}
	
	public void initJsBookEntries() {
		String bookInitKey = "var bookDates ";
		if (!contains(bookInitKey))
			put(bookInitKey, " new Object();");
	}
	
	public void addJsBookDate(String bookName, String dateString) {
		put("bookDates['" + bookName + "'] ", dateString);
	}
	
	public void initJsCollectionEntries() {
		String collectionInitKey = "var collectionDates ";
		if (!contains(collectionInitKey))
			put(collectionInitKey, " new Object();");
	}
	
	public void addJsCollectionDate(String collectionName, String dateString) {
		put("collectionDates['" + collectionName + "'] ", dateString);
	}
	
	public void updateAllEntries(StringsHash serverEntries) {
		Enumeration<String> names = keys();
		while (names.hasMoreElements()) {
			String itemName = names.nextElement();
			put(itemName, serverEntries.get(itemName));
		}
	}
}
