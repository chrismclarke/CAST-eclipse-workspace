package cast.bookManager;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import cast.core.*;

public class CastEbook {
	static final private String kPublicBooks[] = {"general", "generalx", "biometric", "biometricx", "business", "businessx",
												"agExper", "assessment", "exercises", "indExper", "official", "presentation", "regn",
												"simulation", "slug", "mathStats"};
	
	static public boolean canEditBook(String shortBookName) {
		if (Options.isMasterCast)
			return true;
		else {
			for (int i=0 ; i<kPublicBooks.length ; i++) {
				if (shortBookName.equals(kPublicBooks[i]))
					return false;
			}
			return true;
		}
	}
	
	static public boolean isPreface(String filePrefix) {
		if (Options.isMasterCast)
			return false;
		else
			return filePrefix.equals("ch_preface") || filePrefix.equals("sec_aboutCast") || filePrefix.equals("sec_otherInfo");
	}
	
	static public boolean isPublic(String shortBookName) {
		for (int i=0 ; i<kPublicBooks.length ; i++) {
			if (shortBookName.equals(kPublicBooks[i]))
				return true;
		}
		return false;
	}
	
	private File coreDir;
	private String shortBookName;
	private boolean translateOnly;
	
	private Document bookDomDocument = null;
	private DomBook bookDom = null;
	private boolean domChanged;
	
	public CastEbook(File coreDir, String shortBookName, boolean translateOnly) {
		this.coreDir = coreDir;
		this.shortBookName = shortBookName;
		this.translateOnly = translateOnly;
	}
	
	public boolean canOnlyTranslate() {
		return translateOnly;
	}
	
	public boolean canChangeStructure() {
		return !translateOnly;
	}
	
	public boolean canEditBook() {
		if (translateOnly)
			return !isEnglish();
		else
			return canEditBook(shortBookName);
	}
	
	public boolean isEnglish() {
		boolean alreadySetup = (bookDom != null);
		if (!alreadySetup)
			setupDom();
		String language = bookDom.getLanguage();
		if (!alreadySetup) {
			bookDom = null;
			bookDomDocument = null;
		}
		return language == null || language.equals("en");
	}
	
	public void setupDom() {
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
			File bookXmlFile = getBookXmlFile();
			bookDomDocument = db.parse(bookXmlFile);
			
			bookDom = new DomBook(bookDomDocument.getDocumentElement(), this);
			
			domChanged = false;
		} catch(Exception e) {
			System.err.println("Error opening book (" + getShortBookName() + ")\n" + e);
		}
	}
	
	public Document getDocument() {
		return bookDomDocument;
	}
	
	public void clearDom() {
		bookDomDocument = null;
		bookDom = null;
		domChanged = false;
	}
	
	public DomBook getDomBook() {
		return bookDom;
	}
	
	public void setDomChanged() {
		domChanged = true;
	}
	
	public boolean domHasChanged() {
		return domChanged;
	}
	
	public void saveDom() {
		try {
				DOMSource domSource = new DOMSource(bookDomDocument);
				File xmlFile = getBookXmlFile();
				StreamResult streamResult = new StreamResult(xmlFile);
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer serializer = tf.newTransformer();
				serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
				serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"../../../structure/bookXmlDefn.dtd");
				serializer.setOutputProperty(OutputKeys.INDENT,"yes");
				serializer.transform(domSource, streamResult);
		}
		catch (TransformerFactoryConfigurationError factoryError) {
			System.err.println("Error creating TransformerFactory");
			factoryError.printStackTrace();
		} catch (TransformerException transformerError) {
			System.err.println("Error transforming document");
			transformerError.printStackTrace();
		}
	}

//----------------------------------------------------------------------
	
	
	private File getFolderDir(String folderPath) {
		File f = coreDir;
		StringTokenizer st = new StringTokenizer(folderPath, "/");
		while (st.hasMoreTokens())
			f = new File(f, st.nextToken());
		return f;
	}
	
	private File getXmlDir(String shortBookName) {
		return new File(getFolderDir(shortBookName), "xml");
	}
	
	private File getSecDir(String shortBookName) {
		return new File(getFolderDir(shortBookName), "sec");
	}
	
	private File getBookXmlFile(String shortBookName) {
		return new File(getXmlDir(shortBookName), "book.xml");
	}
	
	public File getXmlFile(String shortBookName, String filePrefix) {
		return new File(getXmlDir(shortBookName), filePrefix + ".xml");
	}
	
	public File getSectionJsFile(String shortBookName, String filePrefix) {
		return new File(getSecDir(shortBookName), filePrefix + ".js");
	}
	
	public File getBookDir(String bookDirPath) {
		return getFolderDir(bookDirPath);
	}
	
	public File getSectionDir(String sectionPath) {
		return getFolderDir(sectionPath);
	}
	
	//----------------------------------------------------
	
	public File getBookDir() {
		return getFolderDir(getHomeDirName());
	}
	
/*
	private File getXmlDir() {
		return getXmlDir(getHomeDirName());
	}
*/
	
	private File getTextDir() {
		return new File(getBookDir(), "text");
	}
	
	public File getBookXmlFile() {
		return getBookXmlFile(getHomeDirName());
	}
	
	public File getXmlFile(String filePrefix) {
		return getXmlFile(getHomeDirName(), filePrefix);
	}
	
	public File getIndexFile() {
		return new File(getBookDir(), "book_index.html");
	}
	
	public File getDataSetsFile() {
		return new File(getBookDir(), "book_dataSets.html");
	}
	
	public File getStructureFile() {
		return new File(getBookDir(), "book_structure.js");
	}
	
	public File getTocFile() {
		return new File(getTextDir(), "TOC.rtf");
	}
	
	public File getPrintFile(String sectionName) {
		return new File(getTextDir(), sectionName + ".html");
	}
	
	public File getPageHtmlFile(String dirPath, String filePrefix) {
		File theDir = getFolderDir(dirPath);
		return new File(theDir, filePrefix + ".html");
	}
	
	public File getIndexTermsFile(String language) {
		return getTermsFile("index", language);
	}
	
	public File getUiTermsFile(String language) {
		return getTermsFile("uiTerms", language);
	}
	
	private File getTermsFile(String fileName, String language) {
		File termsDir = new File(coreDir, "terms");
		File f;
		if (language != null) {
			f = new File(termsDir, fileName + "_" + language + ".properties");
			if (f.exists())
				return f;
		}
		f = new File(termsDir, fileName + ".properties");
		if (f.exists())
			return f;
		else
			return null;
	}
	
	public File getDataSourceFile() {
		return new File(coreDir, "sources.data");
	}
	
	//----------------------------------------------------
	
	public String getShortBookName() {
		return shortBookName;
	}
	
	public String getHomeDirName() {
		return "bk/" + shortBookName;
	}
	
	public boolean isHomeDirName(String dir) {
		return getHomeDirName().equals(dir);
	}
	
	public File getCoreDir() {
		return coreDir;
	}

//----------------------------------------------------------------------
	
	public String getLongBookName() {
		return bookDom.getLongBookName();
	}
	
	protected String getTOCTitle() {
		return bookDom.getTOCTitle();
	}
	
	protected String getVersionImage() {
		return bookDom.getVersionImage();
	}
	
	protected String getLogoGif() {
		return bookDom.getLogoGif();
	}
	
	public boolean isLecturingVersion() {
		return bookDom.isLecturingVersion();
	}
	
	public boolean hasSummaries() {
		return bookDom.hasSummaries();
	}
	
	public boolean hasVideos() {
		return bookDom.hasVideos();
	}
	
	public String getLanguage() {
		return bookDom.getLanguage();
	}
	
	public String getDescription() {
		return bookDom.getDescription();
	}
	
//----------------------------------------------------------------------


	
	public File selectHtmlFile(JComponent comp) {
		JFileChooser fc = new JFileChooser(coreDir);
		fc.setDialogTitle("Select HTML file");
		fc.setFileHidingEnabled(true);
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {		//	needs full path since there is FileFilter in java.io too
									public boolean accept(File f) {
										if (f.isDirectory())
											return true;
										String s = f.getName();
										int i = s.lastIndexOf(".html");
										return (i > 0 && i == s.length() - 5);
									}
									
									public String getDescription() {
										return "HTML files";
									}
						});
		
		int result = fc.showOpenDialog(comp);
	
		if (result == JFileChooser.APPROVE_OPTION) {
			File newFile = fc.getSelectedFile();
			
			String filename = newFile.getName();
			if (!newFile.isDirectory() && filename.lastIndexOf(".html") == filename.length() - 5) {
				File newCoreDir = newFile.getParentFile().getParentFile().getParentFile();
				try {
					if (newCoreDir.getCanonicalPath().equals(coreDir.getCanonicalPath()))
						return newFile;
					else
						JOptionPane.showMessageDialog(comp, "You have not chosen an HTML file\nin a sub-sub-folder of CAST/core.", "Error!", JOptionPane.ERROR_MESSAGE);
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	
	public File selectGifPngFile(Component comp, File startDir, final boolean gifNotPng, final boolean allowDirectoryChange) {
		JFileChooser fc = allowDirectoryChange ? new JFileChooser(startDir)
																						: new JFileChooser(startDir, new FixedFileSystemView(startDir));
		final String fileTypeString = gifNotPng ? "GIF" : "PNG";
		fc.setDialogTitle("Select " + fileTypeString + " file");
		fc.setFileHidingEnabled(true);
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {		//	needs full path since there is FileFilter in java.io too
									public boolean accept(File f) {
										if (f.isDirectory() && allowDirectoryChange)
											return true;
										String s = f.getName();
										s = s.toLowerCase();
										int i = gifNotPng ? s.lastIndexOf(".gif") : s.lastIndexOf(".png");
										return (i > 0 && i == s.length() - 4);
									}
									
									public String getDescription() {
										return fileTypeString + " files";
									}
						});
		
		int result = fc.showOpenDialog(comp);
	
		if (result == JFileChooser.APPROVE_OPTION) {
			File newFile = fc.getSelectedFile();
			
			String filename = newFile.getName();
			if (!newFile.isDirectory() && filename.lastIndexOf(gifNotPng ? ".gif" : ".png")
																																		== filename.length() - 4) {
				File imageDir = newFile.getParentFile();
				File bookDir = imageDir.getParentFile();
				File booksDir = bookDir.getParentFile();
				if (imageDir.getName().equals("images") && booksDir.getName().equals("bk"))
					return newFile;
				else
					JOptionPane.showMessageDialog(comp, "You have not chosen a " + fileTypeString + " file\nin an \"images\" folder within CAST/core.", "Error!", JOptionPane.ERROR_MESSAGE);
			}
		}
		return null;
	}

	
	public File selectGifFile(Component comp, File startDir, final boolean allowDirectoryChange) {
		return selectGifPngFile(comp, startDir, true, allowDirectoryChange);
	}

	
	public File selectPngFile(Component comp, File startDir, final boolean allowDirectoryChange) {
		return selectGifPngFile(comp, startDir, false, allowDirectoryChange);
	}
	
	private class FixedFileSystemView extends FileSystemView {
		private File dir;
		
		FixedFileSystemView(File dir) {
			this.dir = dir;
		}
		
		public File getHomeDirectory() {
			return dir;
		}
		
		public Boolean isTraversable(File f) {
			return f.equals(dir);
		}
		
		public File createNewFolder(File containingDir) {
			return null;
		}
		
		public File[] getRoots() {
			return (new File[] {dir});
		}
		
		public File getParentDirectory(File dir) {
			return null;
		}
	}
}
