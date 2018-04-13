package ebook;

import java.awt.*;
//import java.awt.event.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.text.html.*;

import ebookStructure.*;
import pageStructure.*;


public class BookFrame extends JFrame {
	static final private String kFilePrefixCorePattern = "(.*)_\\D(\\d+)$";
	
//	static final private String kWidthString = "width=\"";
	static final private int kInitialWindowWidth = 900;
	static final private int kInitialWindowHeight = 1000;
	
	
	static final public int FROM_BANNER = 0;
	static final public int FROM_TOC = 1;
	static final public int FROM_PAGE = 2;
	static final public int FROM_TAB = 3;
	static final public int FROM_ZOOM = 4;
	
//	static int noOfOpenBooks = 0;
	
	
	private CastEbook theEbook;
	private PageContentView mainPanel;
	
	private TopPanel topPanel;
	private TocColumn theToc;
	
	private IndexFrame theIndexWindow = null;
	private IndexFrame theDatasetWindow = null;
	
	public DomElement currentElement = null;
	private int currentPageVersion = DomElement.FULL_VERSION;
	
	private UiTextStrings uiTextStrings;
	
	
	public BookFrame(CastEbook theEbook, UiTextStrings uiTextStrings) {
		this.theEbook = theEbook;
		currentElement = theEbook.getDomBook();
//		noOfOpenBooks ++;
		
		this.uiTextStrings = uiTextStrings;

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
/*
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (noOfOpenBooks == 1)
					System.exit(0);
				else {
					noOfOpenBooks --;
					dispose();
				}

			}
		});
*/
		
		CoreDrawer.setCoreDir(theEbook.getCoreDir());

		setTitle(theEbook.getLongBookName());
		setLayout(new BorderLayout(0, 0));
		
		mainPanel = new PageContentView(this);
		
		add("Center", mainPanel);
			
			topPanel = new TopPanel(theEbook, this);
		add("North", topPanel);
			
			theToc = new TocColumn(theEbook, this);
		add("West", theToc);
	}
	
	public CastEbook getEbook() {
		return theEbook;
	}
	
	public TocColumn getToc() {
		return theToc;
	}
	
	public StyleSheet getPageStyleSheet() {
		return theEbook.isLecturingVersion() ? CoreDrawer.getLecturingStyleSheet()
																															: CoreDrawer.getStyleSheet();
	}
	
	public String translate(String key) {
		return uiTextStrings.translate(key);
	}
	
/*
	private int getMaximumTagWidth(String htmlString, String tagStart, String tagEnd) {
		int maxWidth = 0;
		while (true) {
			int startIndex = htmlString.indexOf(tagStart);
			if (startIndex < 0 )
				break;
			int endIndex = htmlString.indexOf(tagEnd, startIndex);
			String tagContent = htmlString.substring(startIndex + tagStart.length(), endIndex);
			htmlString = htmlString.substring(endIndex + tagEnd.length());
			int widthIndex = tagContent.indexOf(kWidthString);
			if (widthIndex < 0)
				continue;
			String widthString = tagContent.substring(widthIndex + kWidthString.length());
			int widthEndIndex = widthString.indexOf("\"");
			if (widthEndIndex < 0)
				continue;
			int tagWidth = Integer.parseInt(widthString.substring(0, widthEndIndex));
			maxWidth = Math.max(maxWidth, tagWidth);
		}
		return maxWidth;
	}
*/
	
	public void showPage(DomElement nextElement, int sourceOfCommand) {
		if (sourceOfCommand != FROM_TAB && sourceOfCommand != FROM_ZOOM
																&& (nextElement instanceof DomChapter
																|| nextElement instanceof DomSection || nextElement instanceof DomBook))
			currentPageVersion = DomElement.FULL_VERSION;			//	always change 
		else if (!nextElement.sameParent(currentElement))
			currentPageVersion = DomElement.SUMMARY_VERSION;		//	always start pages in new section as summaries 
			
		currentElement = nextElement;
		
//		showWaitCursor();
		
		mainPanel.setHtml(currentElement);
		
		if (sourceOfCommand != FROM_ZOOM) {
			topPanel.updateContents(sourceOfCommand);
			topPanel.revalidate();
			
			if (sourceOfCommand != BookFrame.FROM_TAB)
				theToc.updateHighlight();
		}
	}
	
/*
	private java.util.Timer waitTimer = null;
	
	private void showWaitCursor() {
		TimerTask waitTask = new TimerTask() {
					public void run() {
							if (waitTimer != null) {
								setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
								waitTimer.cancel();
								waitTimer = null;
							}
					}
			};
		waitTimer = new java.util.Timer(); 
		waitTimer.schedule(waitTask, 500);
	}
	
	public void showNormalCursor() {
		if (waitTimer != null) {
			waitTimer.cancel();
			waitTimer = null;
		}
		setCursor(Cursor.getDefaultCursor());
	}
*/
	
	public void showNamedPage(String filePrefix) {
		filePrefix = getPrefixCore(filePrefix);
		DomElement matchedPage = findElement(theEbook.getDomBook(), filePrefix);
		if (matchedPage == null)
			System.out.println("Cannot find file with prefix \"" + filePrefix + "\"");
		else
			showPage(matchedPage, FROM_PAGE);
	}
	
	public void showPageVersion(int i) {
		if (currentPageVersion != i) {
			currentPageVersion = i;
			showPage(currentElement, FROM_TAB);
		}
	}
	
	private String getPrefixCore(String filePrefix) {
		if (filePrefix.startsWith("v_") || filePrefix.startsWith("s_"))
			filePrefix = filePrefix.substring(2);
		Pattern prefixPattern = Pattern.compile(kFilePrefixCorePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher prefixMatcher = prefixPattern.matcher(filePrefix);
		if (prefixMatcher.find())
			return prefixMatcher.group(1) + prefixMatcher.group(2);
		else 
			return filePrefix;
	}
	
	private DomElement findElement(DomElement root, String filePrefix) {
		if (root instanceof DomPart)
			return null;
		String corePrefix = getPrefixCore(root.getFilePrefixStrings()[DomElement.FULL_VERSION]);
		if (filePrefix.equals(corePrefix))
			return root;
		else {
			for (int i=0 ; i<root.noOfChildren() ; i++) {
				DomElement child = root.getChild(i);
				DomElement match = findElement(child, filePrefix);
				if (match != null)
					return match;
			}
			return null;
		}
	}
	
	public int getEffectivePageVersion() {
		boolean[] hasVersion = currentElement.versionsAllowed();
		if (hasVersion[currentPageVersion])
			return currentPageVersion;
		else
			return DomElement.FULL_VERSION;
	}
	
	public void showIndexWindow() {
		if (theIndexWindow == null)
			theIndexWindow = new IndexFrame(this, IndexFrame.INDEX);
		theIndexWindow.setVisible(true);
	}
	
	public void showDatasetWindow() {
		if (theDatasetWindow == null)
			theDatasetWindow = new IndexFrame(this, IndexFrame.DATASETS);
		theDatasetWindow.setVisible(true);
	}

	static public BookFrame showWindow(CastEbook theBook, UiTextStrings uiTextStrings) {
		BookFrame bookWindow = new BookFrame(theBook, uiTextStrings);
		bookWindow.setResizable(true);
		bookWindow.setSize(kInitialWindowWidth, kInitialWindowHeight);
		bookWindow.setLocationRelativeTo(null);
		
		bookWindow.showPage(theBook.getDomBook(), FROM_PAGE);
		
		bookWindow.setVisible(true);
		return bookWindow;
	}
}