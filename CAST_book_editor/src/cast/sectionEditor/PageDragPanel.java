package cast.sectionEditor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import cast.bookManager.*;
import cast.utils.*;

public class PageDragPanel extends CorePagePanel {
	static final private Color kDarkBlue = new Color(0x000066);
	static final private Color kSelectedBackground = new Color(0xBBBBFF);
	
	private JLabel alternativeLocations;
	private JMenuItem changeSummaryItem, clearSummaryItem, changeVideoItem, clearVideoItem;
	
	public PageDragPanel(final Dom2Page pageDom, final CastSection castSection, final int pageNo) {
		super(pageDom, castSection, pageNo);
		
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 2));
		setOpaque(false);
			
		add(createNamePanel(pageNo));
		
		add(createDescriptionPanel());
		
//		final CastEbook castEbook = castSection.getCastEbook();
		if (castSection.canEditSection()) {
			menu = new JPopupMenu();
			updateMenu();
		}
		
		addMouseListener(new MouseDragListener());
	}
	
	public void updatePageDom() {
	}
	
	private void updateMenu() {
		final CastEbook castEbook = castSection.getCastEbook();
		if (!pageDom.getDirFromXml().equals(castEbook.getHomeDirName())) {
			JMenuItem copyItem = new JMenuItem("Use editable copy...");
			menu.add(copyItem);
			copyItem.addActionListener(new ActionListener() {
																	public void actionPerformed(ActionEvent e) {
																		if (pageDom.createCopyInEbook(PageDragPanel.this, castEbook)) {
																			filePrefix = pageDom.getFilePrefixFromXml();
																			dir = pageDom.getDirFromXml();
																			location.setText("(" + dir + ", " + filePrefix + ")");
																			castSection.setDomChanged();
																
																			menu.removeAll();
																			updateMenu();
																		}
																	}
													});
		}
		
		JMenuItem changeItem = new JMenuItem("Change HTML file...");
		menu.add(changeItem);
		changeItem.addActionListener(new ActionListener() {
														public void actionPerformed(ActionEvent e) {
															File newPageFile = choosePageFile(null);
															if (newPageFile != null) {
																String filename = newPageFile.getName();
																filePrefix = filename.substring(0, filename.length() - 5);
																File sectBook = newPageFile.getParentFile();
																File langBk = sectBook.getParentFile();
																dir = langBk.getName() + "/" + sectBook.getName();
																
																pageTitle = HtmlHelper.getTagInFile(dir, filePrefix, castEbook, "title");
																titleInFile.setText(pageNo + ". " + pageTitle);
																location.setText("(" + dir + ", " + filePrefix + ")");
																pageDom.setPageFile(dir, filePrefix);
																
																pageDom.setSummaryPageFile(null, null);
																pageDom.setVideoPageFile(null, null);
																setAlternativeFileNames();
																
																castSection.setDomChanged();
																
																menu.removeAll();
																updateMenu();
															}
														}
										});
		
		
		JMenuItem deleteItem = new JMenuItem("Delete");
		menu.add(deleteItem);
		deleteItem.addActionListener(new ActionListener() {
														public void actionPerformed(ActionEvent e) {
															int result = JOptionPane.showConfirmDialog(PageDragPanel.this, "Are you sure that you want to delete this page?",
																					"Delete Page?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
															
															if (result == JOptionPane.OK_OPTION)
																deleteSelf();
														}
										});
	
		if (hasSummaries()) {
			menu.addSeparator();
			
			changeSummaryItem = new JMenuItem("Change summary HTML file...");
			menu.add(changeSummaryItem);
			changeSummaryItem.addActionListener(new ActionListener() {
															public void actionPerformed(ActionEvent e) {
																File newSummaryPageFile = choosePageFile("summary");
																if (newSummaryPageFile != null) {
																	String filename = newSummaryPageFile.getName();
																	filePrefix = filename.substring(0, filename.length() - 5);
																	File sectBook = newSummaryPageFile.getParentFile();
																	File langBk = sectBook.getParentFile();
																	dir = langBk.getName() + "/" + sectBook.getName();
																	
																	pageDom.setSummaryPageFile(dir, filePrefix);
																	setAlternativeFileNames();
																	
																	castSection.setDomChanged();
																	
																	clearSummaryItem.setEnabled(true);
																}
															}
											});
			
			clearSummaryItem = new JMenuItem("Clear summary file");
			if (pageDom.getSummaryFilePrefixFromXml().length() == 0)
				clearSummaryItem.setEnabled(false);
			menu.add(clearSummaryItem);
			clearSummaryItem.addActionListener(new ActionListener() {
														public void actionPerformed(ActionEvent e) {
															int result = JOptionPane.showConfirmDialog(PageDragPanel.this, "Are you sure that you want to clear the summary version of this page?",
																					"Clear summary?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
															
															if (result == JOptionPane.OK_OPTION) {
																	pageDom.setSummaryPageFile(null, null);
																	setAlternativeFileNames();
																	
																	castSection.setDomChanged();
																	
																	clearSummaryItem.setEnabled(false);
															}
														}
											});
		}
	
		if (hasVideos()) {
			menu.addSeparator();
			
			changeVideoItem = new JMenuItem("Change video HTML file...");
			menu.add(changeVideoItem);
			changeVideoItem.addActionListener(new ActionListener() {
															public void actionPerformed(ActionEvent e) {
																File newVideoPageFile = choosePageFile("video");
																if (newVideoPageFile != null) {
																	String filename = newVideoPageFile.getName();
																	filePrefix = filename.substring(0, filename.length() - 5);
																	File sectBook = newVideoPageFile.getParentFile();
																	File langBk = sectBook.getParentFile();
																	dir = langBk.getName() + "/" + sectBook.getName();
																	
																	pageDom.setVideoPageFile(dir, filePrefix);
																	setAlternativeFileNames();
																	
																	castSection.setDomChanged();
																	
																	clearVideoItem.setEnabled(true);
																}
															}
											});
			
			clearVideoItem = new JMenuItem("Clear video file");
			if (pageDom.getSummaryFilePrefixFromXml().length() == 0)
				clearVideoItem.setEnabled(false);
			menu.add(clearVideoItem);
			clearVideoItem.addActionListener(new ActionListener() {
														public void actionPerformed(ActionEvent e) {
															int result = JOptionPane.showConfirmDialog(PageDragPanel.this, "Are you sure that you want to clear the video version of this page?",
																					"Clear video?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
															
															if (result == JOptionPane.OK_OPTION) {
																	pageDom.setVideoPageFile(null, null);
																	setAlternativeFileNames();
																	
																	castSection.setDomChanged();
																	
																	clearVideoItem.setEnabled(false);
															}
														}
											});
		}
	}
	
//------------------------------------------------------------------
	
	private JPanel createNamePanel(int pageNo) {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		thePanel.setOpaque(false);
		
			JPanel titlePanel = new JPanel();
			titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			titlePanel.setOpaque(false);
			
			String title = pageNo + ". ";
			
			switch (titleType) {
				case TITLE_FROM_FILE:
					title += pageTitle;
					titleInFile = createLabel(title, Font.BOLD, null);
					titlePanel.add(titleInFile);
					break;
				case TITLE_CUSTOMISED:
					titleInFile = createLabel(title, Font.BOLD, null);
					titlePanel.add(titleInFile);
					titlePanel.add(createLabel(customTitle, Font.BOLD | Font.ITALIC, kDarkBlue));
					break;
				case TITLE_EXERCISE:		//		customTitle starts with #r#
			
					titleInFile = createLabel(title, Font.BOLD, null);
					titlePanel.add(titleInFile);
					int mainIndex = customTitle.indexOf("#+#");
					if (mainIndex > 0) {
						titlePanel.add(createLabel(customTitle.substring(3, mainIndex), Font.BOLD, Color.red));
						titlePanel.add(createLabel(customTitle.substring(mainIndex + 3), Font.BOLD, null));
					}
					break;
			}
			
			if (note != null) {
				int hashIndex = note.indexOf('#');
				if (hashIndex < 0)
					hashIndex = note.length();
				titlePanel.add(createLabel("   " + note.substring(0, hashIndex), Font.BOLD | Font.ITALIC, Color.red));
			}
			
		thePanel.add("Center", titlePanel);
		
			JPanel filePanel = new JPanel();
			filePanel.setOpaque(false);
			filePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER));
				location = new JLabel("(" + dir + ", " + filePrefix + ")");
				location.setFont(new Font("SansSerif", Font.PLAIN, 10));
			filePanel.add(location);
				
			if (hasSummaries() || hasVideos()) {
				alternativeLocations = new JLabel("");
				setAlternativeFileNames();
				alternativeLocations.setFont(new Font("SansSerif", Font.PLAIN, 10));
				alternativeLocations.setForeground(Color.gray);
				
				filePanel.add(alternativeLocations);
			}
		
		thePanel.add("East", filePanel);
		
		return thePanel;
	}
	
	private boolean hasSummaries() {
		return castSection.getCastEbook().hasSummaries();
	}
	
	private boolean hasVideos() {
		return castSection.getCastEbook().hasVideos();
	}
	
	private void setAlternativeFileNames() {
		if (hasSummaries() || hasVideos()) {
			String alternativeString = "";
			
			if (hasSummaries()) {
				String summaryDir = pageDom.getSummaryDirFromXml();
				String summaryFilePrefix = pageDom.getSummaryFilePrefixFromXml();
				if (summaryDir.length() == 0)
					summaryDir = "-";
				if (summaryFilePrefix.length() == 0)
					summaryFilePrefix = "-";
				alternativeString = "(" + summaryDir + ", " + summaryFilePrefix + ")";
				
				if (hasVideos())
					alternativeString += "  ";
			}
			
			if (hasVideos()) {
				String videoDir = pageDom.getVideoDirFromXml();
				String videoFilePrefix = pageDom.getVideoFilePrefixFromXml();
				if (videoDir.length() == 0)
					videoDir = "-";
				if (videoFilePrefix.length() == 0)
					videoFilePrefix = "-";
				alternativeString += "(" + videoDir + ", " + videoFilePrefix + ")";
			}
			
			alternativeLocations.setText(alternativeString);
		}
	}
	
	private JLabel createLabel(String text, int style, Color c) {
		JLabel l = new JLabel(text);
		l.setFont(new Font("SansSerif", style, 14));
		if (c != null)
			l.setForeground(c);
		return l;
	}
	
	private JPanel createDescriptionPanel() {
		JPanel descriptionPanel = new JPanel() {
																	public Insets getInsets() { return new Insets(3, 40, 3, 3); }
																};
		descriptionPanel.setLayout(new BorderLayout(0, 0));
		descriptionPanel.setOpaque(false);
			
			TextBox descriptionArea = new TextBox(description, 3);
			descriptionArea.addMouseListener(new MouseDragListener());		//	effectively passes event to panel
		
		descriptionPanel.add("Center", descriptionArea);
		
		return descriptionPanel;
	}
	
//-------------------------------------------------------
	
	private SectionContents getContentsPanel() {
		return (SectionContents)getParent();
	}
	
	public void doHighlight(boolean selected) {
		if (selected) {
			setBackground(kSelectedBackground);
			setOpaque(true);
		}
		else
			setOpaque(false);
			setTransferHandler(null);
			setDropTarget(null);
			repaint();
	}
	
	public void select() {
		SectionContents.select(this);
	}
	
	private void deleteSelf() {
		Dom2Section domSection = castSection.getDomSection();
		int nPages = domSection.noOfChildren();
		for (int i=0 ; i<nPages ; i++) {
			Dom2Page domPageI = domSection.getChild(i);
			if (domPageI == pageDom)  {
				domSection.cutChild(i);
				((SectionContents)getContentsPanel()).relayout();
				return;
			}
		}
	}
	
	
	private File choosePageFile(String typeString) {
		CastEbook castEbook = castSection.getCastEbook();
		File currentFile = castEbook.getPageHtmlFile(dir, filePrefix);
		JFileChooser fc = new JFileChooser(currentFile);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("Pick HTML file for " + (typeString != null ? (typeString + " ") : "") + "page");
		fc.setFileHidingEnabled(true);

// Show open dialog; this method does not return until the dialog is closed
		int result = fc.showOpenDialog(this);
	
		switch (result) {
			case JFileChooser.APPROVE_OPTION:
				File pageFile = fc.getSelectedFile();
				
				String filename = pageFile.getName();
				if (filename.length() < 6 || filename.indexOf(".html") != filename.length() - 5) {
					JOptionPane.showMessageDialog(PageDragPanel.this, "You have not chosen an HTML file (with extention \".html\").", "Error!", JOptionPane.ERROR_MESSAGE);
					return null;
				}
				
				try {
					String chosenCoreDir = pageFile.getParentFile().getParentFile().getParentFile().getCanonicalPath();
					String coreDir = castEbook.getCoreDir().getCanonicalPath();
					if (!coreDir.equals(chosenCoreDir)) {
						JOptionPane.showMessageDialog(PageDragPanel.this, "The page file must be located in\na folder within a \"CAST/core/xxx/\" folder.", "Error!", JOptionPane.ERROR_MESSAGE);
						return null;
					}
				} catch (IOException e) {
					return null;
				}
				
				return pageFile;
		}
		return null;
	}
	
}
