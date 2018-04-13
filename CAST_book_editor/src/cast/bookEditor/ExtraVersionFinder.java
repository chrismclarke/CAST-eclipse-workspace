package cast.bookEditor;

import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;

import javax.swing.*;

import cast.bookManager.*;


public class ExtraVersionFinder extends JDialog {
	
//	static final private Color kBannerBackground = new Color(0x0066FF);
//	static final private Color kTocBackground = new Color(0xCC0033);
	
	public static void findExtraVersions(DomBook domBook, Dialog parent, CastEbook castEbook, String extraVersionCode,
																																											String extraVersionName) {
																		//	extraVersionCode = "v_" or "s_"  and extraVersionName = "video" or "summary"
		ExtraVersionFinder dialog = new ExtraVersionFinder(parent, castEbook, extraVersionCode, extraVersionName);

		Point p1 = parent.getLocation();
		Dimension d1 = parent.getSize();
		Dimension d2 = dialog.getSize();

		int x = p1.x + (d1.width - d2.width) / 2;
		int y = p1.y + (d1.height - d2.height) / 2;

		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;

		dialog.setLocation(x,y);
		dialog.setVisible(true);

		dialog.dispose();
	}
	
	private static void searchSectionForExtraVersions(String dir, String filePrefix, CastEbook castEbook,
																																		String extraVersionCode, boolean ignorePrevious) {
		if (!dir.startsWith("bk/"))
			return;
		
		String language = castEbook.getLanguage();
		if (language == null)
			language = "en";
		
		boolean summaryNotVideo = extraVersionCode.equals("s_");
		
		CastSection castSection = new CastSection(dir, filePrefix, castEbook);
		Dom2Section domSection = castSection.getDomSection();
		int nPages = domSection.noOfChildren();
		for (int i=0 ; i<nPages ; i++) {
			Dom2Page page = domSection.getChild(i);
			String pageDir = page.getDirFromXml();
			String pagePrefix = page.getFilePrefixFromXml();
			String extraPageDir = ignorePrevious ? "" : summaryNotVideo ? page.getSummaryDirFromXml() : page.getVideoDirFromXml();
			String extraPagePrefix = ignorePrevious ? "" : summaryNotVideo ? page.getSummaryFilePrefixFromXml()
																																					: page.getVideoFilePrefixFromXml();
//			System.out.println("Page " + i + ": (" + pageDir + ", " + pagePrefix
//																			+ "), (" + extraPageDir + ", " + extraPagePrefix + ")");
			if (extraPageDir.equals("") || extraPagePrefix.equals("") || !castEbook.getPageHtmlFile(extraPageDir, extraPagePrefix).exists()) {
//				System.out.println("Needs change");
				String newExtraPageDir = "", newExtraPagePrefix = "";
				
				if (castEbook.getPageHtmlFile(pageDir, extraVersionCode + pagePrefix).exists()) {
//					System.out.println("Found perfect summary (same dir)");
					newExtraPageDir = pageDir;
					newExtraPagePrefix = extraVersionCode + pagePrefix;
				}
				else {
					String fileCore, fileVersion, fileIndex;
					Pattern thePattern = Pattern.compile("(.*)(_[a-z])(\\d*)?$");
					Matcher theMatcher = thePattern.matcher(pagePrefix);
					if (theMatcher.find()) {
						fileCore = theMatcher.group(1);
						fileVersion = theMatcher.group(2);
						fileIndex = theMatcher.group(3);
					}
					else {
						thePattern = Pattern.compile("(.*[^\\d])(\\d*)?$");
						theMatcher = thePattern.matcher(pagePrefix);
						if (theMatcher.find()) {
							fileCore = theMatcher.group(1);
							fileVersion = "";
							fileIndex = theMatcher.group(2);
						}
						else {
							fileCore = pagePrefix;
							fileVersion = "";
							fileIndex = "";
						}
					}
//					System.out.println("Separated file name into (" + fileCore + ", " + fileVersion + ", " + fileIndex + ")");
					if (fileVersion.length() > 0 && castEbook.getPageHtmlFile(pageDir, extraVersionCode + fileCore + fileIndex).exists()) {
//						System.out.println("Found general summary (same dir)");
						newExtraPageDir = pageDir;
						newExtraPagePrefix = extraVersionCode + fileCore + fileIndex;
					}
					else if (castEbook.getPageHtmlFile(language + "/" + fileCore, extraVersionCode + pagePrefix).exists()) {
							newExtraPageDir = language + "/" + fileCore;
							newExtraPagePrefix = extraVersionCode + pagePrefix;
					}
					else if (fileVersion.length() > 0 && castEbook.getPageHtmlFile(language + "/" + fileCore, extraVersionCode + fileCore + fileIndex).exists()) {
						newExtraPageDir = language + "/" + fileCore;
						newExtraPagePrefix = extraVersionCode + fileCore + fileIndex;
					}
				}
			
				boolean changedDir = !extraPageDir.equals(newExtraPageDir);
				boolean changedPage = !extraPagePrefix.equals(newExtraPagePrefix);
				
				if (changedDir || changedPage) {
//					System.out.println("(" + extraPageDir + ", " + extraPagePrefix + ") --> (" + newExtraPageDir + ", " + newExtraPagePrefix + ")");
					if (summaryNotVideo)
						page.setSummaryPageFile(newExtraPageDir, newExtraPagePrefix);
					else
						page.setVideoPageFile(newExtraPageDir, newExtraPagePrefix);
					castSection.setDomChanged();
				}
			}
		}
		if (castSection.domHasChanged())
			castSection.saveDom();
	}
	
	private ExtraVersionFinder(Dialog parent, final CastEbook castEbook, final String extraVersionCode, final String extraVersionName) {
		super(parent, "Search for " + extraVersionName + " versions of pages?", true);
		
		setLayout(new BorderLayout(0, 25));
		
			JPanel innerPanel = new JPanel() { public Insets getInsets() { return new Insets(10, 20, 10, 20); }};
			innerPanel.setLayout(new BorderLayout(0, 25));
			
				JTextArea textArea = new JTextArea(5, 50);
				textArea.setEditable(false);
				textArea.setLineWrap(true);
				textArea.setWrapStyleWord(true);
				textArea.setOpaque(false);
				textArea.setText("You have asked for the e-book to show a tab for " + extraVersionName + " pages. " + 
												"If you click Yes below, an attempt will be made to find " + extraVersionName + " " + 
												"versions of all pages in the e-book. (These will have similar " +
												"names to the html file for the pages but with prefix \"" + extraVersionCode + "\".)");
			innerPanel.add("North", textArea);
			
						
				JPanel centerPanel = new JPanel();
				centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					final JCheckBox ignorePreviousCheck = new JCheckBox("Ignore any previously selected " + extraVersionName + " pages");
					ignorePreviousCheck.setSelected(true);
				centerPanel.add(ignorePreviousCheck);
				
			innerPanel.add("Center", centerPanel);
						
				JPanel bottomPanel = new JPanel();
				bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
				
					JButton saveButton = new JButton("Yes");
					saveButton.addActionListener(new ActionListener() {
																					public void actionPerformed(ActionEvent e) {
																						searchForAllExtraVersions(castEbook, extraVersionCode, ignorePreviousCheck.isSelected());
																						setVisible(false);
																					}
																			});
				bottomPanel.add(saveButton);
				
					JButton cancelButton = new JButton("No");
					cancelButton.addActionListener(new ActionListener() {
																					public void actionPerformed(ActionEvent e) {
																						setVisible(false);
																					}
																			});
				bottomPanel.add(cancelButton);
				
			innerPanel.add("South", bottomPanel);
			
		add("Center", innerPanel);
		pack();
	}
	
	private void searchForAllExtraVersions(CastEbook castEbook, String extraVersionCode, boolean ignorePrevious) {
//		int nSections = 0;
		DomBook domBook = castEbook.getDomBook();
		int nChildren = domBook.noOfChildren();
		for (int i=0 ; i<nChildren ; i++) {
			DomElement child = domBook.getChild(i);
			if (child instanceof DomChapter) {
				int nSubChildren = child.noOfChildren();
				for (int j=0 ; j<nSubChildren ; j++) {
					DomElement subChild = child.getChild(j);
					if (subChild instanceof DomSection) {
						DomSection section = (DomSection)subChild;
						searchSectionForExtraVersions(section.getDir(), section.getFilePrefix(), castEbook,
																																						extraVersionCode, ignorePrevious);
					}
				}
			}
		}
	}
}
