package cast.server;

import java.util.*;
import java.io.*;
import java.net.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import cast.core.*;
import cast.bookManager.*;


public class AllDates {
	private String collection;
	private DatesHash coreDatesHash, collectionDatesHash, bookDatesHash, sectionDatesHash, videoDatesHash;
	
	public AllDates(String collection, String language, SetupDatesTask setupTask) {		//	always includes "en", even if language is null
		this.collection = collection;
		try {
			if (setupTask != null)
				setupTask.noteNextStage("Core dates on server");
			String serverDatesPath = "http://" + Options.kCastDownloadUrl + "/core/dates/";
			URL coreDatesUrl = new URL(serverDatesPath + CoreCopyTask.kCoreDatesFileName);
			coreDatesHash = new DatesHash(coreDatesUrl);
			
			if (setupTask != null)
				setupTask.noteNextStage("Collection dates on server");
			URL collectionDatesUrl = new URL(serverDatesPath + CoreCopyTask.kCollectionDatesPrefix + collection + ".text");
			collectionDatesHash = new DatesHash(collectionDatesUrl);
			
			if (setupTask != null)
				setupTask.noteNextStage("Book dates on server");
			URL bookDatesUrl = new URL(serverDatesPath + CoreCopyTask.kBookDatesFileName);
			bookDatesHash = new DatesHash(bookDatesUrl);
			
			if (setupTask != null)
				setupTask.noteNextStage("Section dates on server");
			URL sectionDatesUrl = new URL(serverDatesPath + CoreCopyTask.kSectionDatesFileName);
			sectionDatesHash = new DatesHash(sectionDatesUrl);
			restrictSectionToLanguage(language);
			
			if (setupTask != null)
				setupTask.noteNextStage("Video dates on server");
			URL videoDatesUrl = new URL(serverDatesPath + CoreCopyTask.kVideoDatesFileName);
			videoDatesHash = new DatesHash(videoDatesUrl);
			
		} catch (Exception e) {		//	no datestamp file on server
			System.out.println("Could not find date stamp file on server.");
			e.printStackTrace();
		}
	}
	
	public AllDates(String collection, String language) {
		this(collection, language, null);
	}
	
	public AllDates(File castLocalDir, String collection) {
		this.collection = collection;
		File coreDir = new File(castLocalDir, "core");
		File datesDir = new File(coreDir, "dates");
		coreDatesHash = new DatesHash(new File(datesDir, CoreCopyTask.kCoreDatesFileName));
		collectionDatesHash = new DatesHash(new File(datesDir, CoreCopyTask.kCollectionDatesPrefix + collection + ".text"));
		bookDatesHash = new DatesHash(new File(datesDir, CoreCopyTask.kBookDatesFileName));
		sectionDatesHash = new DatesHash(new File(datesDir, CoreCopyTask.kSectionDatesFileName));
		videoDatesHash = new DatesHash(new File(datesDir, CoreCopyTask.kVideoDatesFileName));
	}
	
	public boolean getReadFailed() {
		return coreDatesHash.getReadFailed() || collectionDatesHash.getReadFailed() ||
								bookDatesHash.getReadFailed() || sectionDatesHash.getReadFailed() || videoDatesHash.getReadFailed();
	}
	
//---------------------------------------------------------------------------
	
	private String[] needingUpdate(DatesHash serverDates, DatesHash localDates, DatesHash requiredItems) {
		TreeSet<String> updateItems = new TreeSet<String>();
		
		Enumeration<String> itemNames = requiredItems.keys();
		while (itemNames.hasMoreElements()) {
			String itemName = itemNames.nextElement();
			long serverDate = serverDates.getDate(itemName);
			long localDate = localDates.getDate(itemName);
			if (serverDate > localDate)
				updateItems.add(itemName);
		}
		
		String[] s = new String[updateItems.size()];
		Iterator<String> iter = updateItems.iterator();
		for (int i=0 ; i<s.length ; i++)
			s[i] = iter.next();
		
		return s;
	}
	
	public String[] coreNeedingUpdate(AllDates serverDates) {
		return needingUpdate(serverDates.coreDatesHash, coreDatesHash, serverDates.coreDatesHash);
	}
	
	public String[] collectionNeedingUpdate(AllDates serverDates) {
		return needingUpdate(serverDates.collectionDatesHash, collectionDatesHash, serverDates.collectionDatesHash);
	}
	
	public String[] booksNeedingUpdate(AllDates serverDates) {
		return needingUpdate(serverDates.bookDatesHash, bookDatesHash, bookDatesHash);
	}
	
	public String[] sectionsNeedingUpdate(AllDates serverDates) {
		return needingUpdate(serverDates.sectionDatesHash, sectionDatesHash, serverDates.sectionDatesHash);
	}
	
	public String[] videosNeedingUpdate(AllDates serverDates) {
		return needingUpdate(serverDates.videoDatesHash, videoDatesHash, videoDatesHash);
	}
	
	public boolean needsUpdate(AllDates serverDates) {
		return coreNeedingUpdate(serverDates).length > 0 || collectionNeedingUpdate(serverDates).length > 0
								|| booksNeedingUpdate(serverDates).length > 0 ||  sectionsNeedingUpdate(serverDates).length > 0
								||  videosNeedingUpdate(serverDates).length > 0;
	}
	
//---------------------------------------------------------------------------
	
	public void updateCoreEntry(String itemName, AllDates serverDates, File datesDir) {
		coreDatesHash.updateEntry(itemName, serverDates.coreDatesHash);
		coreDatesHash.saveToFile(new File(datesDir, CoreCopyTask.kCoreDatesFileName));
	}
	
	public void updateCollectionEntry(String itemName, AllDates serverDates, File datesDir) {
		collectionDatesHash.updateEntry(itemName, serverDates.collectionDatesHash);
		collectionDatesHash.saveToFile(new File(datesDir, CoreCopyTask.kCollectionDatesPrefix + collection + ".text"));
	}
	
	public void updateSectionEntry(String itemName, AllDates serverDates, File datesDir) {
		sectionDatesHash.updateEntry(itemName, serverDates.sectionDatesHash);
		sectionDatesHash.saveToFile(new File(datesDir, CoreCopyTask.kSectionDatesFileName));
	}
	
	public void updateBookEntry(String itemName, AllDates serverDates, File datesDir) {
		bookDatesHash.updateEntry(itemName, serverDates.bookDatesHash);
		bookDatesHash.saveToFile(new File(datesDir, CoreCopyTask.kBookDatesFileName));
	}
	
	public void updateVideoEntry(String itemName, AllDates serverDates, File datesDir) {
		videoDatesHash.updateEntry(itemName, serverDates.videoDatesHash);
		videoDatesHash.saveToFile(new File(datesDir, CoreCopyTask.kVideoDatesFileName));
	}
	
//---------------------------------------------------------------------------
	
	public DatesHash getCoreDates() {
		return coreDatesHash;
	}
	
	public DatesHash getCollectionDates() {
		return collectionDatesHash;
	}
	
	public DatesHash getSectionDates() {
		return sectionDatesHash;
	}
	
	public DatesHash getBookDates() {
		return bookDatesHash;
	}
	
	public DatesHash getVideoDates() {
		return videoDatesHash;
	}
	
//---------------------------------------------------------------------------
	
	
	private void restrictSectionToLanguage(String language) {
		Enumeration<String> sectionNames = sectionDatesHash.keys();
		while (sectionNames.hasMoreElements()) {
			String sectionName = sectionNames.nextElement();
			if (sectionName.startsWith("en/"))
				continue;
			if (language != null && sectionName.startsWith(language + "/"))
				continue;
			sectionDatesHash.remove(sectionName);		//	remove entries in other languages
		}
	}
	
	public void findNewVideosInBook(File coreDir, String baseBookName) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(true);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setErrorHandler(new ErrorHandler() {
													public void warning(SAXParseException exception) throws SAXException {
													}
													public void error(SAXParseException exception) throws SAXException {
													}
													public void fatalError(SAXParseException exception) throws SAXException {
													}
												} );
			
			CastEbook theEbook = new CastEbook(coreDir, baseBookName, false);
			File videoXmlFile = theEbook.getXmlFile("videos");
			Document videoDomDocument = db.parse(videoXmlFile);
			
			NodeList bookList = videoDomDocument.getElementsByTagName("book");
			for (int i=0; i<bookList.getLength(); i++) {
				Node bookNode = bookList.item(i);
				if (bookNode.getNodeType() == Node.ELEMENT_NODE) {
					Element bookElement = (Element)bookNode;
					String bookName = bookElement.getAttribute("name");
					
					NodeList sectionList = bookElement.getElementsByTagName("dir");
					for (int j=0 ; j<sectionList.getLength() ; j++) {
						Node sectionNode = sectionList.item(j);
						if (sectionNode.getNodeType() == Node.ELEMENT_NODE) {
							Element sectionElement = (Element)sectionNode;
							String sectionName = sectionElement.getAttribute("name");
							
							NodeList videoList = sectionElement.getElementsByTagName("video");
							for (int k=0 ; k<videoList.getLength() ; k++) {
								Node videoNode = videoList.item(k);
								Element videoElement = (Element)videoNode;
								String videoName = videoElement.getAttribute("name");
								
								String videoPath = bookName + "/videos/" + sectionName + "/" + videoName;
								if (!videoDatesHash.containsKey(videoPath))
									videoDatesHash.put(videoPath, Long.valueOf(0));		//	mark video as required and needing update
							}
						}
					}
				}
			}
		} catch(ParserConfigurationException e) {
			System.err.println("Error creating XML DocumentBuilder\n");
			e.printStackTrace();
		} catch(SAXException e) {
			System.err.println("Error parsing video XML file\n");
			e.printStackTrace();
		} catch(IOException e) {
			System.err.println("Error reading XML file\n");
			e.printStackTrace();
		}
	}
}
