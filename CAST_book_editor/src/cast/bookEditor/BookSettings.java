package cast.bookEditor;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

import javax.swing.*;
import javax.imageio.*;
import javax.swing.border.*;

import cast.bookManager.*;
import cast.utils.*;
import cast.core.*;


public class BookSettings extends JDialog {
	
	static final private Color kBannerBackground = new Color(0x0066FF);
	static final private Color kTocBackground = new Color(0xCC0033);
	static final private Color kBorderTitleColor = new Color(0x660000);
	
	static final public String kDefaultBookGroup = "Miscellaneous";
	static final public String kDefaultDescription = "No description";
	
	public static void editSettings(Frame parent, CastEbook castEbook) {
		DomBook domBook = castEbook.getDomBook();
		boolean oldHasSummaries = domBook.hasSummaries();
		boolean oldHasVideos = domBook.hasVideos();
		
		BookSettings dialog = new BookSettings(castEbook.getDomBook(), parent, castEbook);

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

		if (dialog.ok) {
			domBook.setLongBookName(dialog.getLongBookName());
			domBook.setTOCTitle(dialog.getLongBookName());
			domBook.setVersionImage(dialog.getVersionImage());
			domBook.setLogoGif(dialog.getLogoGif());
			domBook.setSummaryPdfUrl(dialog.getSummaryPdfUrl());
			
			String coreBookType = dialog.getCoreBookType();
			boolean withSummaries = dialog.getWithSummaries();
			boolean withVideos = dialog.getWithVideos();
			domBook.setBookType(coreBookType, withSummaries, withVideos);
			
			boolean newHasSummaries = domBook.hasSummaries();
			if (newHasSummaries && !oldHasSummaries)
				ExtraVersionFinder.findExtraVersions(domBook, dialog, castEbook, "s_", "summary");
			
			boolean newHasVideos = domBook.hasVideos();
			if (newHasVideos && !oldHasVideos)
				ExtraVersionFinder.findExtraVersions(domBook, dialog, castEbook, "v_", "video");
			
			String description = dialog.getDescription();
			domBook.setDescription(description);
			
			if (Options.hasMultipleCollections)
				domBook.setLanguage(dialog.getLanguage());
			
			castEbook.setDomChanged();
		}

		dialog.dispose();
	}
	
	private boolean ok = false;
	
	private JTextField longBookNameEdit, bookGroupEdit, languageEdit, summaryPdfUrlEdit;
	private String versionImage, logoGif;
	private JCheckBox summariesCheck, videosCheck;
	private JRadioButton bookButton , lectureButton, moduleButton;
	private JTextArea descriptionEdit;
	
	private BookSettings(DomBook domBook, Frame parent, final CastEbook castEbook) {
		super(parent, "Settings for " + castEbook.getShortBookName(), true);
		
		String longBookName = domBook.getLongBookName();
		versionImage = domBook.getVersionImage();
		logoGif = domBook.getLogoGif();
		String language = domBook.getLanguage();
		boolean isLecturingVersion = domBook.isLecturingVersion();
		boolean isModule = domBook.isModule();
		
		boolean hasSummaries = domBook.hasSummaries();
		boolean hasVideos = domBook.hasVideos();
		
		String summaryPdfUrl = domBook.getSummaryPdfUrl();
		
		String description = domBook.getDescription();
		String descriptiveParagraph = kDefaultDescription;
		String bookGroup = kDefaultBookGroup;
		if (description != null) {
			int hashIndex = description.indexOf("#");
			if (hashIndex > 0)  {
				bookGroup = description.substring(0, hashIndex);
				description = description.substring(hashIndex + 1);
			}
			if (description.length() > 0)
				descriptiveParagraph = description;
		}
		
		setLayout(new BorderLayout(0, 12));
		
			JPanel settingsPanel = new JPanel();
			settingsPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 10));
			
			settingsPanel.add(descriptionPanel(castEbook, longBookName, bookGroup, descriptiveParagraph));
			
			settingsPanel.add(bannerGifPanel(castEbook));
			
			settingsPanel.add(logoGifPanel(castEbook));
			
			settingsPanel.add(bookTypePanel(castEbook, isLecturingVersion, isModule, hasSummaries, hasVideos,
																																					summaryPdfUrl));
				
			if (Options.hasMultipleCollections) {
				JPanel languagePanel = new JPanel();
				languagePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					JLabel languageTitle = new JLabel("  Language code:");
					languageEdit = new JTextField(language, 2);
					languageTitle.setLabelFor(languageEdit);
				
				languagePanel.add("West", languageTitle);
				languagePanel.add("Center", languageEdit);
				
				settingsPanel.add(languagePanel);
				
			}
			
		add("Center", settingsPanel);
		
		
			JPanel bottomPanel = new JPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			
				JButton saveButton = new JButton("Save");
				saveButton.addActionListener(new ActionListener() {
																				public void actionPerformed(ActionEvent e) {
																					ok = true;
																					setVisible(false);
																				}
																		});
			bottomPanel.add(saveButton);
			
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
																				public void actionPerformed(ActionEvent e) {
																					ok = false;
																					setVisible(false);
																				}
																		});
			bottomPanel.add(cancelButton);
			
		add("South", bottomPanel);
			
		pack();
	}
	
	private File versionImagesDir;			//	for current version image in banner
	
	private JPanel createTitledPanel(String title) {
		TitledBorder border = BorderFactory.createTitledBorder(title);
		border.setTitleColor(kBorderTitleColor);
		Font f = getFont();
		border.setTitleFont(new Font(f.getName(), Font.BOLD, f.getSize()));
		
		Border innerSpacing = BorderFactory.createEmptyBorder(2, 5, 2, 5);
		
		JPanel thePanel = new JPanel();
		thePanel.setBorder(BorderFactory.createCompoundBorder(border, innerSpacing));
		return thePanel;
	}
	
	private JPanel descriptionPanel(final CastEbook castEbook, String longBookName,
																							String bookGroup, String descriptiveParagraph) {
		JPanel thePanel = createTitledPanel("Description of e-book");
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, 0));
			
			JPanel longNamePanel = new JPanel();
			longNamePanel.setLayout(new BorderLayout(0, 0));
				JLabel longNameTitle = new JLabel(" Long name:");
				longBookNameEdit = new JTextField(longBookName, 25);
				longNameTitle.setLabelFor(longBookNameEdit);
			
			longNamePanel.add("West", longNameTitle);
			longNamePanel.add("Center", longBookNameEdit);
			
		thePanel.add(longNamePanel);
			
			JPanel bookGroupPanel = new JPanel();
			Border topSpacing = BorderFactory.createEmptyBorder(10, 0, 0, 0);
			bookGroupPanel.setBorder(topSpacing);
			bookGroupPanel.setLayout(new BorderLayout(0, 0));
				JLabel bookGroupTitle = new JLabel(" Name of book collection:");
				bookGroupEdit = new JTextField(longBookName, 25);
				bookGroupTitle.setLabelFor(bookGroupEdit);
				bookGroupEdit.setText(bookGroup);
			
			bookGroupPanel.add("West", bookGroupTitle);
			bookGroupPanel.add("Center", bookGroupEdit);
			
		thePanel.add(bookGroupPanel);
			
			JLabel descriptionTitle = new JLabel(" Description paragraph:", JLabel.LEFT);
			descriptionTitle.setBorder(topSpacing);
		thePanel.add(descriptionTitle);
		
			descriptionEdit = new JTextArea(5, 20);
			descriptionEdit.setLineWrap(true);
			descriptionEdit.setWrapStyleWord(true);
			JScrollPane scrollPane = new JScrollPane(descriptionEdit);
			descriptionEdit.setText(descriptiveParagraph);
			
		thePanel.add(scrollPane);
		
		return thePanel;
	}
	
	private JPanel bannerGifPanel(final CastEbook castEbook) {
		JPanel thePanel = createTitledPanel("PNG image in banner");
		thePanel.setLayout(new BorderLayout(0, 4));
			
			JLabel description = new JLabel("Pick a 130x20 image in the \"images\" folder of any e-book.", JLabel.CENTER);
			
		thePanel.add("North", description);
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			
				final JLabel imageLabel = new JLabel();
				imageLabel.setBackground(kBannerBackground);
				imageLabel.setOpaque(true);
				updateVersionPng(imageLabel, castEbook);
			mainPanel.add(imageLabel);
			
				JButton changeButton = new JButton("Change image");
				changeButton.addActionListener(new ActionListener() {
																public void actionPerformed(ActionEvent e) {
																	File newGifFile = castEbook.selectPngFile(BookSettings.this, versionImagesDir, true);
																	if (newGifFile != null) {
																		String newFileName = newGifFile.getName();
																		String newFilePrefix = newFileName.substring(0, newFileName.length() - 4);
																		
																		String newBookName = newGifFile.getParentFile().getParentFile().getName();
																		
																		versionImage = "images/" + newFilePrefix;
																		if (!newBookName.equals(castEbook.getShortBookName()))
																			versionImage = "../" + newBookName + "/" + versionImage;
																		
																		updateVersionPng(imageLabel, castEbook);
																	}
																}
												});
				
			mainPanel.add(changeButton);
			
		thePanel.add("South", mainPanel);
		
		return thePanel;
	}
	
	private void updateVersionPng(JLabel imageLabel, CastEbook castEbook) {
		String imageBookDir = castEbook.getShortBookName();
		String imageFileName = versionImage;
		if (imageFileName.indexOf("../") == 0) {		//	must be something like "../biometric/images/xxxx"
			imageFileName = imageFileName.substring(3);
			int slashIndex = imageFileName.indexOf('/');
			imageBookDir = imageFileName.substring(0, slashIndex);
			imageFileName = imageFileName.substring(slashIndex + 1);
		}
		if (imageFileName.indexOf("images/") == 0)
			imageFileName = imageFileName.substring(7);
		imageFileName += ".png";
		
		File booksDir = new File(castEbook.getCoreDir(), "bk");
		File bookDir = new File(booksDir, imageBookDir);
		versionImagesDir = new File(bookDir, "images");
		File imageFile = new File(versionImagesDir, imageFileName);
		
		try {
				BufferedImage img = ImageIO.read(imageFile);
				ImageIcon iconImg = new ImageIcon(img);
				imageLabel.setIcon(iconImg);
		} catch (IOException e) {
		}
	}
	
	private JPanel logoGifPanel(final CastEbook castEbook) {
		JPanel thePanel = createTitledPanel("GIF image under the chapter list");
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
		thePanel.add(new JLabel("A logo that is no wider than 233 pixels can be shown.", JLabel.LEFT));
		thePanel.add(new JLabel("This GIF file must be in the folder CAST/core/" + castEbook.getHomeDirName() + "/images", JLabel.LEFT));
		
			JPanel checkPanel = new JPanel();
			checkPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				final JButton changeButton = new JButton("Change logo image");
				final JCheckBox useImageCheck = new JCheckBox("Show a GIF logo");
				final JLabel imageLabel = new JLabel();
				useImageCheck.addItemListener(new ItemListener() {
													public void itemStateChanged(ItemEvent itemEvent) {
														int state = itemEvent.getStateChange();
														if (state == ItemEvent.SELECTED)
															changeButton.setEnabled(true);
														else {
															changeButton.setEnabled(false);
															logoGif = null;
														}
														changeButton.setEnabled(state == ItemEvent.SELECTED);
														updateLogoGif(imageLabel, castEbook);
													}
											});
				useImageCheck.setSelected(logoGif != null);
				changeButton.setEnabled(logoGif != null);
			checkPanel.add(useImageCheck);
		
		thePanel.add(checkPanel);
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			
				imageLabel.setBackground(kTocBackground);
				imageLabel.setOpaque(true);
			mainPanel.add(imageLabel);
			
				changeButton.addActionListener(new ActionListener() {
																public void actionPerformed(ActionEvent e) {
																	File bookDir = new File(castEbook.getCoreDir(), castEbook.getHomeDirName());
																	File imagesDir = new File(bookDir, "images");
																	File newGifFile = castEbook.selectGifFile(BookSettings.this, imagesDir, false);
																	if (newGifFile != null) {
																		logoGif = newGifFile.getName();
																		updateLogoGif(imageLabel, castEbook);
																	}
																}
												});
				
			mainPanel.add(changeButton);
			
		thePanel.add(mainPanel);
		
		return thePanel;
	}
	
	private void updateLogoGif(JLabel imageLabel, CastEbook castEbook) {
		ImageIcon iconImg = null;
		
		if (logoGif != null) {
			File bookDir = new File(castEbook.getCoreDir(), castEbook.getHomeDirName());
			File imagesDir = new File(bookDir, "images");
			File logoGifFile = new File(imagesDir, logoGif);
			
			try {
				BufferedImage img = ImageIO.read(logoGifFile);
				iconImg = new ImageIcon(img);
			} catch (IOException e) {
			}
		}
		
		imageLabel.setIcon(iconImg);
		imageLabel.revalidate();
		pack();
	}
	
	
//---------------------------------------------------------------------------------
	
	private JPanel bookTypePanel(CastEbook castEbook, boolean isLecturingVersion, boolean isModule,
																				boolean hasSummaries, boolean hasVideos, String summaryPdfUrl) {
		JPanel thePanel = createTitledPanel("Type of e-book");
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
		
			JPanel typePanel = new JPanel();
			typePanel.setLayout(new GridLayout(3, 2));
			
				bookButton  = new JRadioButton("Book", !isLecturingVersion && !isModule);
				bookButton.addActionListener(new ActionListener() {
																						public void actionPerformed(ActionEvent e) {
																							summariesCheck.setEnabled(true);
																							videosCheck.setEnabled(true);
																						}
																				});
				lectureButton = new JRadioButton("Lecturing", isLecturingVersion);
				lectureButton.addActionListener(new ActionListener() {
																						public void actionPerformed(ActionEvent e) {
																							summariesCheck.setEnabled(false);
																							videosCheck.setEnabled(false);
																						}
																				});
				moduleButton = new JRadioButton("Module", isModule);
				moduleButton.addActionListener(new ActionListener() {
																						public void actionPerformed(ActionEvent e) {
																							summariesCheck.setEnabled(false);
																							videosCheck.setEnabled(false);
																						}
																				});

				ButtonGroup typeButtonGroup = new ButtonGroup();
				typeButtonGroup.add(bookButton);
				typeButtonGroup.add(lectureButton);
				typeButtonGroup.add(moduleButton);
				
				summariesCheck = new JCheckBox("With summaries");
				summariesCheck.setSelected(hasSummaries);
				videosCheck = new JCheckBox("With videos");
				videosCheck.setSelected(hasVideos);
				if (isLecturingVersion || isModule) {
					summariesCheck.setEnabled(false);
					videosCheck.setEnabled(false);
				}

			typePanel.add(bookButton);
			typePanel.add(summariesCheck);
			typePanel.add(lectureButton);
			typePanel.add(videosCheck);
			typePanel.add(moduleButton);
			
		thePanel.add(typePanel);
		
		thePanel.add(new JSeparator());
		
		thePanel.add(new JLabel("For books, optional URLs can be specified where PDF versions", JLabel.LEFT));
		thePanel.add(new JLabel("of the summary chapters can be downloaded for printing.", JLabel.LEFT));
			
			JPanel summaryPdfUrlPanel = new JPanel();
			summaryPdfUrlPanel.setLayout(new BorderLayout(0, 10));
				JLabel summaryUrlTitle = new JLabel(" Summary folder:");
				summaryPdfUrlEdit = new JTextField(summaryPdfUrl == null ? "" : summaryPdfUrl, 25);
				summaryUrlTitle.setLabelFor(summaryPdfUrlEdit);
			
			summaryPdfUrlPanel.add("West", summaryUrlTitle);
			summaryPdfUrlPanel.add("Center", summaryPdfUrlEdit);
			
		thePanel.add(summaryPdfUrlPanel);
		thePanel.add(new JLabel("   (Summaries must be called Chapter_s_1.pdf, etc.)", JLabel.LEFT));
		
		return thePanel;
	}
	
	
//---------------------------------------------------------------------------------
	
	public String getLongBookName() {
		return longBookNameEdit.getText();
	}
	
	public String getSummaryPdfUrl() {
		String url = summaryPdfUrlEdit.getText();
		if (url.length() > 0)
			return url;
		else
			return null;
	}
	
	public String getVersionImage() {
		return versionImage;
	}
	
	public String getLogoGif() {
		return logoGif;
	}
	
	public String getLanguage() {
		return languageEdit.getText().length() == 2 ? languageEdit.getText() : null;
	}
	
	public boolean getWithSummaries() {
		return summariesCheck.isEnabled() && summariesCheck.isSelected();
	}
	
	public boolean getWithVideos() {
		return videosCheck.isEnabled() && videosCheck.isSelected();
	}
	
	public String getCoreBookType() {
		if (bookButton.isSelected())
			return DomBook.TYPE_BOOK;
		if (lectureButton.isSelected())
			return DomBook.TYPE_LECTURE;
		return DomBook.TYPE_MODULE;
	}
	
	public String getDescription() {
		String bookGroup = bookGroupEdit.getText();
		if (bookGroup.length() == 0)
			bookGroup = kDefaultBookGroup;
		String descriptiveParagraph = descriptionEdit.getText();
		if (descriptiveParagraph.length() == 0)
			descriptiveParagraph = kDefaultDescription;
		return bookGroup + "#" + descriptiveParagraph;
	}
}
