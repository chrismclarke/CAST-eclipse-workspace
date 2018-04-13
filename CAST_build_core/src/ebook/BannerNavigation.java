package ebook;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.border.*;

import ebookStructure.*;
import pageStructure.*;


public class BannerNavigation extends JPanel {
	static final public String kTestNamePattern = "<test name=['\"](.*?)['\"]";
	
	static final private String kHideTocString = "Hide table of contents";
	static final private String kShowTocString = "Show table of contents";
	
	static final private String kZoomBiggerString = "Zoom bigger";
	static final private String kZoomSmallerString = "Zoom smaller";
	
	static final private float kZoomBiggerFactor = 1.2f;
	
	private BookFrame theWindow;
	
//	private JLabel ch, chNo, sect, page;
//	private UiImage lineImage;
	private UiImage backImage, nextImage;
	
	private JPopupMenu optionsPopup;
	@SuppressWarnings("unused")
	private int nCoreOptions;
	
	public BannerNavigation(BookFrame theWindowParam, CastEbook theEbook) {
		this.theWindow = theWindowParam;
		
		setOpaque(false);
//		setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		setLayout(new BorderLayout(0, 0));
		
		JPanel buttonColumn = createButtonColumn(theEbook);
//		add(buttonColumn);
		add("East", buttonColumn);
		
		updateArrows();
	}
	
	private JPanel createButtonColumn(CastEbook theEbook) {
		JPanel thePanel = new JPanel();
		thePanel.setOpaque(false);
		GridBagLayout gbl = new GridBagLayout();
		thePanel.setLayout(gbl);
		
		
			GridBagConstraints backC = new GridBagConstraints();
			backC.anchor = GridBagConstraints.EAST;
			backC.fill = GridBagConstraints.NONE;
			backC.gridheight = 1;
			backC.gridwidth = 1;
			backC.gridx = 0;
			backC.gridy = 0;
			backC.insets = new Insets(0,3,0,1);
			backC.ipadx = backC.ipady = 0;
			backC.weightx = 1.0;
			backC.weighty = 0.0;
		
		File structureDir = theEbook.getStructureDir();
		File backStdFile = new File(structureDir, "images/backArrow_std.png");
		File backDimFile = new File(structureDir, "images/backArrow_dim.png");
		File backBoldFile = new File(structureDir, "images/backArrow_bold.png");
		backImage = new UiImage(backStdFile, backDimFile, backBoldFile, true) {
																		protected void doClickAction() {
																			showPreviousFile();
																		}
											};
		thePanel.add(backImage);
			gbl.setConstraints(backImage, backC);
		
		
			GridBagConstraints starC = new GridBagConstraints();
			starC.anchor = GridBagConstraints.CENTER;
			starC.fill = GridBagConstraints.NONE;
			starC.gridheight = 1;
			starC.gridwidth = 1;
			starC.gridx = 1;
			starC.gridy = 0;
			starC.insets = new Insets(0,0,0,0);
			starC.ipadx = starC.ipady = 0;
			starC.weightx = 0.0;
			starC.weighty = 0.0;
		
		File starFile = new File(structureDir, "images/star.png");
		UiImage starImage = new UiImage(starFile, true);
		thePanel.add(starImage);
			gbl.setConstraints(starImage, starC);
		
		
			GridBagConstraints nextC = new GridBagConstraints();
			nextC.anchor = GridBagConstraints.WEST;
			nextC.fill = GridBagConstraints.NONE;
			nextC.gridheight = 1;
			nextC.gridwidth = 1;
			nextC.gridx = 2;
			nextC.gridy = 0;
			nextC.insets = new Insets(0,1,0,0);
			nextC.ipadx = nextC.ipady = 0;
			nextC.weightx = 1.0;
			nextC.weighty = 0.0;
		
		File nextStdFile = new File(structureDir, "images/nextArrow_std.png");
		File nextDimFile = new File(structureDir, "images/nextArrow_dim.png");
		File nextBoldFile = new File(structureDir, "images/nextArrow_bold.png");
		nextImage = new UiImage(nextStdFile, nextDimFile, nextBoldFile, true) {
																		protected void doClickAction() {
																			showNextFile();
																		}
											};
		thePanel.add(nextImage);
			gbl.setConstraints(nextImage, nextC);
		
		
			GridBagConstraints bookC = new GridBagConstraints();
			bookC.anchor = GridBagConstraints.CENTER;
			bookC.fill = GridBagConstraints.NONE;
			bookC.gridheight = 1;
			bookC.gridwidth = 3;
			bookC.gridx = 0;
			bookC.gridy = 1;
			bookC.insets = new Insets(0,3,0,3);
			bookC.ipadx = bookC.ipady = 0;
			bookC.weightx = 0.0;
			bookC.weighty = 0.0;
		
		String versionImageName = theEbook.getDomBook().getVersionImage();
		File bookDir = theEbook.getBookDir();
		File bookFile = new File(bookDir, versionImageName + ".png");
//		File bookFile = new File("../bk/general/images/castGeneral.png");
		UiImage bookImage = new UiImage(bookFile, true);
		thePanel.add(bookImage);
			gbl.setConstraints(bookImage, bookC);
		
//		if (summaryPdfDirUrl != null) {
			GridBagConstraints optionsC = new GridBagConstraints();
			optionsC.anchor = GridBagConstraints.SOUTHEAST;
			optionsC.fill = GridBagConstraints.NONE;
			optionsC.gridheight = 1;
			optionsC.gridwidth = 3;
			optionsC.gridx = 0;
			optionsC.gridy = 2;
			optionsC.insets = new Insets(0,0,3,8);
			optionsC.ipadx = optionsC.ipady = 0;
			optionsC.weightx = 1.0;
			optionsC.weighty = 1.0;
		
//			File printStdFile = new File(structureDir, "images/print.gif");
//			File printDimFile = null;
//			File printBoldFile = new File(structureDir, "images/print_bold.gif");
//			UiImage printImage = new UiImage(printStdFile, printDimFile, printBoldFile, false);
			File optionsStdFile = new File(structureDir, "images/options.png");
			File optionsDimFile = null;
			File optionsBoldFile = new File(structureDir, "images/options_bold.png");
			final UiImage optionsImage = new UiImage(optionsStdFile, optionsDimFile, optionsBoldFile, true);
			
				
				optionsPopup = createCorePopup(theEbook);
				nCoreOptions = optionsPopup.getComponentCount();
			optionsImage.addMouseListener(new MouseAdapter() {
													public void mousePressed(MouseEvent e) {
														optionsPopup.show(optionsImage, e.getX(), e.getY());
													}

													public void mouseClicked(MouseEvent e) {
														optionsPopup.show(optionsImage, e.getX(), e.getY());
													}
												});
			thePanel.add(optionsImage);
			
			gbl.setConstraints(optionsImage, optionsC);
//		}
		
		return thePanel;
	}
	
	private JPopupMenu createCorePopup(CastEbook theEbook) {
		JPopupMenu corePopup = new JPopupMenu();
		String summaryPdfDirUrl = theEbook.getDomBook().getSummaryPdfUrl();
		if (summaryPdfDirUrl != null) {
			JMenuItem printHeadingItem = new JMenuItem(theWindow.translate("PDF summaries of chapters for printing") + ":");
			printHeadingItem.setEnabled(false);
			corePopup.add(printHeadingItem);
			
			DomElement bookElement = theEbook.getDomBook();
			
			for (int i=1 ; i<bookElement.noOfChildren() ; i++) {		//	none for Preface
				DomElement e = bookElement.getChild(i);
				if (e instanceof DomChapter)
					corePopup.add(createPrintItem(e, summaryPdfDirUrl));
			}
			corePopup.addSeparator();
		}
		
		final JMenuItem tocItem = new JMenuItem(theWindow.translate(kHideTocString));
		tocItem.addActionListener(new ActionListener() {
															public void actionPerformed(ActionEvent e) {
																TocColumn theToc = theWindow.getToc();
																theToc.setVisible(!theToc.isVisible());
																tocItem.setText(theWindow.translate(theToc.isVisible() ? kHideTocString : kShowTocString));
															}
														});
		corePopup.add(tocItem);
		
		if (!theEbook.isLecturingVersion()) {
			final JMenuItem zoomItem = new JMenuItem(theWindow.translate(kZoomBiggerString));
			zoomItem.addActionListener(new ActionListener() {
																public void actionPerformed(ActionEvent e) {
																	if (zoomItem.getText().equals(kZoomBiggerString)) {
																		CoreDrawer.setScaleFactor(kZoomBiggerFactor);
																		zoomItem.setText(theWindow.translate(kZoomSmallerString));
																	}
																	else {
																		CoreDrawer.setScaleFactor(1.0f);
																		zoomItem.setText(theWindow.translate(kZoomBiggerString));
																	}
																	theWindow.showPage(theWindow.currentElement, BookFrame.FROM_ZOOM);
																}
															});
			corePopup.add(zoomItem);
		}
		
		File bookDir = theEbook.getBookDir();
		File testsDir = new File(bookDir, "tests");
		if (testsDir.exists()) {
			File[] tests = testsDir.listFiles(new FilenameFilter() {
																public boolean accept(File dir, String name) {
																	return name.endsWith(".xml");
																}
															});
			if (tests.length > 0) {
				corePopup.addSeparator();
				JMenuItem testHeadingItem = new JMenuItem(theWindow.translate("Tests"));
				testHeadingItem.setEnabled(false);
				corePopup.add(testHeadingItem);
				
				for (int i=0 ; i<tests.length ; i++)
					corePopup.add(createTestItem(tests[i], theEbook));
				corePopup.add(createReviewAttemptItem(theEbook));
			}
		}
		
		return corePopup;
	}
	
	private JMenuItem createPrintItem(DomElement e, final String summaryPdfDirUrl) {
		String chapterName = e.getName();
		final int chapterIndex = e.getIndex();
		JMenuItem theItem = new JMenuItem("   " + chapterIndex + ". " + chapterName);
		theItem.addActionListener(new ActionListener() {
																public void actionPerformed(ActionEvent e) {
																	openPdfInBrowser(summaryPdfDirUrl, chapterIndex);
																}
															});
		return theItem;
	}
	
	private JMenuItem createTestItem(final File testFile, final CastEbook theEbook) {
		String testString = XmlHelper.getFileAsString(testFile);
		
		Pattern testNamePattern = Pattern.compile(kTestNamePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher testNameMatcher = testNamePattern.matcher(testString);
		if (testNameMatcher.find()) {
			String testName = testNameMatcher.group(1);
			JMenuItem theItem = new JMenuItem("   " + testName);
			theItem.addActionListener(new ActionListener() {
																	public void actionPerformed(ActionEvent e) {
																		startTest(testFile, theEbook);
																	}
																});
			return theItem;
		}
		return null;
	}
	
	private void startTest(File testFile, CastEbook theEbook) {
		TestInformation testInfo = TestSetupDialog.getTestInfo(theWindow, testFile);
		if (testInfo != null) {
			TestFrame testWindow = new TestFrame(theEbook, testFile, theWindow, testInfo);
			testWindow.setLocationRelativeTo(theWindow);
			testWindow.setVisible(true);
			
			theWindow.setVisible(false);
		}
	}
	
	private JMenuItem createReviewAttemptItem(final CastEbook theEbook) {
		JMenuItem theItem = new JMenuItem("   Review previous attempt...");
		Border borderWithGap = new CompoundBorder(BorderFactory.createEmptyBorder(7, 0, 0, 0), theItem.getBorder());
		theItem.setBorder(borderWithGap);
		theItem.addActionListener(new ActionListener() {
																	public void actionPerformed(ActionEvent e) {
																		openAttempt(theEbook);
																	}
																});
		return theItem;
	}
	
	private void openAttempt(CastEbook theEbook) {
		JFileChooser fileChooser = new JFileChooser() {
											public void approveSelection() {
												File f = getSelectedFile();
												String fileString = XmlHelper.getFileAsString(f);
												if (fileString.indexOf("<test name=") < 0 || fileString.indexOf("</test>") < 0) {
													JOptionPane.showMessageDialog(this, "This file does not describe a valid test attempt.",
																																							"Error!", JOptionPane.ERROR_MESSAGE);
													cancelSelection();
													return;
												}
												super.approveSelection();
											}
										};

		fileChooser.setDialogTitle("Select an XML file containing a saved attempt.");
		fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		fileChooser.setFileFilter(new FileNameExtensionFilter("XML file","xml"));
		
		if (fileChooser.showOpenDialog(theWindow) == JFileChooser.APPROVE_OPTION) {
			File attemptFile = fileChooser.getSelectedFile();
			TestFrame testWindow = new TestFrame(theEbook, attemptFile, theWindow);
			testWindow.setLocationRelativeTo(theWindow);
			testWindow.setVisible(true);
		}
	}
	
	private void openPdfInBrowser(String summaryPdfDirUrl, int chapterIndex) {
		try {
			String url = summaryPdfDirUrl + "/Chapter_s_" + chapterIndex + ".pdf";
			java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
/*
	private void updateOptionsPopup(final CastEbook theEbook) {
		int nOptions = optionsPopup.getComponentCount();
		for (int i=nOptions-1 : i>=nCoreOptions ; i--)
			optionsPopup.remove(i);
											//		ready for commands to show pages from different contexts
	}
*/
	
	public void updateArrows() {
		DomElement e = theWindow.currentElement;
				
		if (e.nextElement() == null)
			nextImage.setImage(UiImage.DIM_IMAGE);
		else if (nextImage.getImage() == UiImage.DIM_IMAGE)
			nextImage.setImage(UiImage.STD_IMAGE);
		
		if (e.previousElement() == null)
			backImage.setImage(UiImage.DIM_IMAGE);
		else if (backImage.getImage() == UiImage.DIM_IMAGE)
			backImage.setImage(UiImage.STD_IMAGE);
	}
	
	private void showNextFile() {
		theWindow.showPage(theWindow.currentElement.nextElement(), BookFrame.FROM_BANNER);
	}
	
	private void showPreviousFile() {
		theWindow.showPage(theWindow.currentElement.previousElement(), BookFrame.FROM_BANNER);
	}
	
}