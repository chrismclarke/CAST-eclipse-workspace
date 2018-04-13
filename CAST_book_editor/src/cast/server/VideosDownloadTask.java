package cast.server;

import java.io.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import cast.bookManager.*;
import cast.core.*;



public class VideosDownloadTask extends CoreDownloadTask {

	private File coreDir;
	private String homeBookName;
	private VideoDownloadFrame downloadFrame;
	
	private DatesHash serverVideoDates, localVideoDates;
	
	private File sectionDir = null;
	private String currentVideo = null;
	
	public VideosDownloadTask(final File coreDir, final String homeBookName, VideoDownloadFrame downloadFrame) {
		this.coreDir = coreDir;
		this.homeBookName = homeBookName;
		this.downloadFrame = downloadFrame;
	}
	
	public Object doInBackground() throws Exception {
		String serverVideoDatesPath = "core/dates/" + CoreCopyTask.kVideoDatesFileName;
		serverVideoDates = new DatesHash("http://" + Options.kCastDownloadUrl + "/" + serverVideoDatesPath);
		
		File datesDir = new File(coreDir, "dates");
		File localVideoDatesFile = new File(datesDir, CoreCopyTask.kVideoDatesFileName);
		localVideoDates = new DatesHash(localVideoDatesFile);
		
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
			
			CastEbook theEbook = new CastEbook(coreDir, homeBookName, false);
			File videoXmlFile = theEbook.getXmlFile("videos");
			Document videoDomDocument = db.parse(videoXmlFile);
			
			int noOfSections = countSections(videoDomDocument);
			publish(new DownloadStatus(noOfSections));
			failIfInterrupted();
			
			NodeList bookList = videoDomDocument.getElementsByTagName("book");
			for (int i=0; i<bookList.getLength(); i++) {
				Node bookNode = bookList.item(i);
				if (bookNode.getNodeType() == Node.ELEMENT_NODE) {
					Element bookElement = (Element)bookNode;
					String bookName = bookElement.getAttribute("name");
					File bookDir = theEbook.getBookDir("bk/" + bookName);
					File videosDir = new File(bookDir, "videos");
					videosDir.mkdir();
					
					NodeList sectionList = bookElement.getElementsByTagName("dir");
					for (int j=0 ; j<sectionList.getLength() ; j++) {
						Node sectionNode = sectionList.item(j);
						if (sectionNode.getNodeType() == Node.ELEMENT_NODE) {
							Element sectionElement = (Element)sectionNode;
							String sectionName = sectionElement.getAttribute("name");
							File sectionDir = new File(videosDir, sectionName);
							sectionDir.mkdir();
							
							NodeList videoList = sectionElement.getElementsByTagName("video");
							
							publish(new DownloadStatus(bookName + "/" + sectionName, videoList.getLength()));
							failIfInterrupted();
							
							for (int k=0 ; k<videoList.getLength() ; k++) {
								Node videoNode = videoList.item(k);
								Element videoElement = (Element)videoNode;
								String videoName = videoElement.getAttribute("name");
								
								String videoPath = bookName + "/videos/" + sectionName + "/" + videoName;
								long localDate = localVideoDates.getDate(videoPath);
								long serverDate = serverVideoDates.getDate(videoPath);
								if (localDate < serverDate) {
									downloadOneVideo(sectionDir, videoName, videoPath);
									localVideoDates.put(videoPath, Long.valueOf(serverDate));
									localVideoDates.saveToFile(localVideoDatesFile);
								}
							}
						}
					}
				}
			}
			
			rememberVideosDownloaded(theEbook, true);
		} catch(ParserConfigurationException e) {
			System.err.println("Error creating XML DocumentBuilder\n");
			e.printStackTrace();
		} catch(SAXException e) {
			System.err.println("Error parsing video XML file\n");
			e.printStackTrace();
		}
		return null;
	}
	
	private int countSections(Document videoDomDocument) {
		int nSections = 0;
		NodeList bookList = videoDomDocument.getElementsByTagName("book");
		for (int i=0; i<bookList.getLength(); i++) {
			Node bookNode = bookList.item(i);
			if (bookNode.getNodeType() == Node.ELEMENT_NODE) {
				Element bookElement = (Element)bookNode;
				
				NodeList sectionList = bookElement.getElementsByTagName("dir");
				for (int j=0 ; j<sectionList.getLength() ; j++) {
					Node sectionNode = sectionList.item(j);
					if (sectionNode.getNodeType() == Node.ELEMENT_NODE)
						nSections ++;
				}
			}
		}
		return nSections;
	}

	public void done() {
		if (currentVideo != null)
			cancelVideoDownload();
		if (isCancelled())
			downloadFrame.setCancelled();
		else
			downloadFrame.setFinished();
	}
	
	private void cancelVideoDownload() {
		if (currentVideo != null) {
			File pngFile = new File(sectionDir, currentVideo + ".png");
			if (pngFile.exists())
				pngFile.delete();
			File mp4File = new File(sectionDir, currentVideo + ".mp4");
			if (mp4File.exists())
				mp4File.delete();
			File webmFile = new File(sectionDir, currentVideo + ".webm");
			if (webmFile.exists())
				webmFile.delete();
			downloadFrame.setCancelled();
		}
	}
	
	
	protected void process(final java.util.List<DownloadStatus> chunks) {
		for (final DownloadStatus status : chunks) {
			if (status.section == null && status.item ==  null)
				downloadFrame.updateForStart(status.noOfSections);
			else if (status.item == null)
				downloadFrame.updateForNewSection(status.section, status.noOfItems);
			else if (status.fileType == null)
				downloadFrame.updateForNewVideo(status.item);
			else
				downloadFrame.updateForNewFile(status.item + ": downloading " + status.fileType);
		}
	}
}