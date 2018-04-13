package cast.server;

import java.util.*;
import java.io.*;
import java.net.*;

import cast.core.*;
import cast.bookManager.*;


public class DatesHash extends Hashtable<String, Long> {
	static final public String kDateStampFileName = "dateStamps.text";
	static final public String[] kBookFolders = {"xml", "images", "sec", "text", "tests"};
	
	static final public String kSystemAdviceFileName = "systemAdvice.html";
	
	static public File getDateStampFile(File dir) {
		return new File(dir, kDateStampFileName);
	}
	
	static public String[] itemsNeedingUpdate(DatesHash destinationDates, DatesHash sourceDates,
																																				String[] requiredVideo) {
		TreeSet<String> updateNames = new TreeSet<String>();
		
		if (requiredVideo == null) {
			Enumeration<String> e = sourceDates.keys();
			while(e.hasMoreElements()) {
				String serverFile = e.nextElement();
				long sourceDate = sourceDates.getDate(serverFile);
				long destinationDate = destinationDates.getDate(serverFile);
				if (destinationDate < sourceDate)
					updateNames.add(serverFile);
			}
		}
		else
			for (int i=0 ; i<requiredVideo.length ; i++) {
				long sourceDate = sourceDates.getDate(requiredVideo[i]);
				long destinationDate = destinationDates.getDate(requiredVideo[i]);
				if (destinationDate < sourceDate)
					updateNames.add(requiredVideo[i]);
			}
		
		String[] s = new String[updateNames.size()];
		Iterator<String> iter = updateNames.iterator();
		for (int i=0 ; i<s.length ; i++)
			s[i] = iter.next();
		
		return s;
	}
	
	static public String[] itemsNeedingUpdate(DatesHash destinationDates, DatesHash sourceDates) {
		return itemsNeedingUpdate(destinationDates, sourceDates, null);
	}
	
	private boolean readFailed = false;

	public DatesHash() {
		super();
	}
	
	public DatesHash(File f) {
		super();
		if (f.isFile())
			readDateFile(f);															//	a standard dateStamp file
		else if (f.isDirectory()) {
			File parentFile = f.getParentFile();
			if (parentFile.getName().equals("bk")) {			//	a book folder
				findFilesInDir(f, "");
				for (int i=0 ; i<kBookFolders.length ; i++)
					findFilesInDir(new File(f, kBookFolders[i]), kBookFolders[i] + "/");
			}
			else {
				File grandparentFile = parentFile.getParentFile();
				if (grandparentFile != null && grandparentFile.getName().equals("core") && parentFile.getName().length() == 2) {			//	a section folder
					findFilesInDir(f, "");
					findFilesInDir(new File(f, "images"), "images/");
				}
				else {																			//	all files recursively in another folder
					findRecursiveFilesInDir(f, "");
				}
			}
		}
	}
	
	public DatesHash(URL url) {
		super();
		readUrl(url);
	}
	
	public DatesHash(String urlString) {
		super();
		try {
			URL url = new URL(urlString);
			readUrl(url);
		} catch (MalformedURLException e) {
			readFailed = true;
		}
	}
	
	public DatesHash(File castDir, String collection) {
		super();				//		all entries relative to CAST folder
		
		File collectionFolder = new File(castDir, collection);
		if (collectionFolder.exists())
			findRecursiveFilesInDir(collectionFolder, collection + "/");
		addEntryForFile(new File(castDir, "collection_" + collection + ".html"));
		
		String languageCode = SetupDatesTask.getLanguageCode(collection);
		if (languageCode == null)
			languageCode = "en";
		File coreDir = new File(castDir, "core");
		addSystemAdviceFile(coreDir, "en");
		if (!languageCode.equals("en"))
			addSystemAdviceFile(coreDir, languageCode);
	}
	
	private void addSystemAdviceFile(File coreDir, String languageCode) {
		File sectionsDir = new File(coreDir, languageCode);
		File systemAdviceFile = new File(sectionsDir, kSystemAdviceFileName);
		if (systemAdviceFile.exists())
			put("core/" + languageCode + "/" + kSystemAdviceFileName, Long.valueOf(systemAdviceFile.lastModified()));
	}

	
	public boolean getReadFailed() {
		return readFailed;
	}
	
	public long getDate(String itemName) {
		Long lastModDate = get(itemName);
		return (lastModDate == null) ? 0 : lastModDate.longValue();
	}
	
	
//------------------------------------------------------

	
	private void readUrl(URL url) {
		try {
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();
			parseDateFile(is);
		} catch (FileNotFoundException e) {		//	no datestamp file on server
			readFailed = true;
		} catch (IOException e) {
			System.err.println(e.toString());
			readFailed = true;
		}
	}
	
	protected void readDateFile(File f) {
		try {
			InputStream is = new FileInputStream(f);
			parseDateFile(is);
		} catch (FileNotFoundException e) {
			readFailed = true;
			System.err.println(e.toString());
		}
	}
	
	private void parseDateFile(InputStream is) {
		@SuppressWarnings("resource")
		Scanner s = new Scanner(is).useDelimiter("[=\\s]");
		while (s.hasNext()) {
			String itemName = s.next();
			if (s.hasNextLong()) {
				long itemDate = s.nextLong();
				put(itemName, Long.valueOf(itemDate));
			}
		}
		s.close();
	}
	
	
//------------------------------------------------------
	
	
	private void addEntryForFile(File f) {
		if (f.exists())
			put(f.getName(), Long.valueOf(f.lastModified()));
	}
	
	private void findFilesInDir(File dir, String dirPrefix) {
		if (!dir.isDirectory())
			return;
		File[] files = dir.listFiles(new FilenameFilter() {
																		public boolean accept(File dir, String name) {
																			return new File(dir, name).isFile() && !name.equals(kDateStampFileName)
																																						&& !name.startsWith(".");
																								//	top level files that are not the datestamp file or hidden
																		}
																	});
		for (int k=0 ; k<files.length ; k++) {
			String fileName = files[k].getName();
			long lastModifiedDate = files[k].lastModified();
			put(dirPrefix + fileName, Long.valueOf(lastModifiedDate));
		}
	}
	
	private void findRecursiveFilesInDir(File dir, String dirPrefix) {
		File[] items = dir.listFiles(new FilenameFilter() {
																		public boolean accept(File dir, String name) {
																			return !name.equals(kDateStampFileName) && !name.startsWith(".");
																								//	files that are not the datestamp file or hidden
																		}
																	});
		for (int k=0 ; k<items.length ; k++) {
			String itemName = items[k].getName();
			if (items[k].isFile()) {
				long lastModifiedDate = items[k].lastModified();
				put(dirPrefix + itemName, Long.valueOf(lastModifiedDate));
			}
			else
				findRecursiveFilesInDir(items[k], dirPrefix + itemName + "/");
		}
	}
	
	
//------------------------------------------------------

	
	public TreeSet<String> allFileNames() {
		TreeSet<String> allNames = new TreeSet<String>();
		Enumeration<String> e = keys();
		while (e.hasMoreElements())
			allNames.add(e.nextElement());
		return allNames;
	}
	
	public void saveToFile(File dateStampFile) {
		try {
			PrintWriter out = new PrintWriter(dateStampFile);
			Enumeration<String> e = keys();
			while(e.hasMoreElements()) {
				String fileName = e.nextElement();
				Long lastModDate = get(fileName);
				out.println(fileName + "=" + lastModDate.toString());
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
		while(e.hasMoreElements()) {
			String fileName = e.nextElement();
			Long lastModDate = get(fileName);
			if (sb.length() > 0)
				sb.append("\n");
			sb.append(fileName + "=" + lastModDate.toString());
		}
		return sb.toString();
	}
	
	public void printDates() {
		Enumeration<String> names = keys();
		while (names.hasMoreElements()) {
			String itemName = names.nextElement();
			Long modDate = get(itemName);
			System.out.println(itemName + "=" + modDate.longValue());
		}
	}
	
	public long latestChange() {
		long latestDate = 0L;
		Enumeration<String> names = keys();
		while (names.hasMoreElements()) {
			String itemName = names.nextElement();
			Long lastModDate = get(itemName);
			latestDate = Math.max(latestDate, lastModDate.longValue());
		}
		return latestDate;
	}
	
	public void updateEntry(String itemName, DatesHash serverDates) {
		put(itemName, serverDates.get(itemName));
	}
	
	public void updateAllEntries(DatesHash serverDates) {
		Enumeration<String> names = keys();
		while (names.hasMoreElements()) {
			String itemName = names.nextElement();
			put(itemName, serverDates.get(itemName));
		}
	}
	
	
//--------------------------------------------------------------
	
	
	public void removeEntriesWithPrefix(String keyPrefix) {
		Enumeration<String> allKeys = keys();
		while (allKeys.hasMoreElements()) {
			String theKey = allKeys.nextElement();
			if (theKey.startsWith(keyPrefix))
				remove(theKey);
		}
	}
	
	public void removeBooksNotPresent(File coreDir) {
		File booksDir = new File(coreDir, "bk");
		Enumeration<String> serverNames = keys();
		while (serverNames.hasMoreElements()) {
			String bookName = serverNames.nextElement();
			if (!new File(booksDir, bookName).exists())
				remove(bookName);
		}
	}
	
	public String[] matchCustomBooks(DatesHash localFiles) {
		int nFiles = 0;
		Enumeration<String> serverNames = keys();
		while (serverNames.hasMoreElements()) {
			String bookName = serverNames.nextElement();
			if (!CastEbook.isPublic(bookName) && !localFiles.containsKey(bookName))
				nFiles ++;
		}
		String[] newNames = new String[nFiles];
		ArrayList<String> al = new ArrayList(keySet());
		Collections.sort(al);
		Iterator<String> iter = al.iterator();
		int i = 0;
		while (iter.hasNext()) { 
			String bookName = iter.next();
			if (!CastEbook.isPublic(bookName) && !localFiles.containsKey(bookName))
				newNames[i++] = bookName;
		}
		return newNames;
	}
}
