package cast.core;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.regex.*;
import java.io.*;
import java.net.*;

import javax.swing.*;
import javax.swing.border.*;

import cast.utils.*;
import cast.server.*;

import ebook.*;


public class BookChoiceWindow extends JFrame {
//	static final private String kVideoText = changeToHtml("Some CAST e-books contain video versions of pages. The videos are large so, by default, they are stored on our web server. However the e-books for any e-book can be downloaded for offline use.", 250);
//	static final private String kBookDownloadText = changeToHtml("Downloaded copies of CAST only include core e-books. Additional CAST e-books can be installed from the CAST web site or a zip file.", 250);
//	static final private String kUpdateText = changeToHtml("Update this downloaded copy of CAST and its e-books to their latest versions. New e-books and videos cannot be downloaded until CAST is up to date.", 250);
//	static final private String kCustomiseText = changeToHtml("This provides additional advanced commands for authors, allowing e-books and exercises to be customised.", 250);
	
//	static final private String kServerErrorHeading = "Error!  Could not connect to the CAST server.";
	static final private String kMasterCastMessage = "Master copy of CAST, so downloading is disabled.";
//	static final private String kServerErrorString = changeToHtml("You cannot download new e-books or videos since the CAST server cannot be accessed. Check your internet connection then try again.", 350);
	
//	static final private String kHeadingDescription = changeToHtml("CAST is a collection of e-books for learning statistics; select one from the list below.", 450);
//	static final private String kAdvancedDescription = changeToHtml("(The advanced commands let you download videos and install further e-books.)", 450);
	
	static final private Color kCheckboxBackground = HeadingPanel.kHeadingBackground;
	static final private Color kDimTextColor = new Color(0x999999);
	
	static final private Font kShortListFont = new Font("SansSerif", Font.BOLD, 18);
	
	static final private String kEbookCollectionsPattern = "var tabName = \\[(.*?)\\].*var tabLink = \\[(.*?)\\]";
	static final private String kCustomNamePattern = "tabName\\[[45]\\] = \"(.*?)\"";
	
	static public String changeToHtml(String s, int width) {
		return "<html><div style='width:" + width + "px;'>" + s + "</div></html>";
	}
	
	static public void showUrl(String urlString) {
		try {
			final URI theUri = new URI("http://" + Options.kHelpPath + "/" + urlString);
			Desktop desktop = Desktop.getDesktop();
			desktop.browse(theUri);
		} catch(IOException ioe) {
			System.out.println("The system cannot find the \"" + urlString + "\" file specified");
			ioe.printStackTrace();
		} catch(URISyntaxException use) {
			System.out.println("Illegal character in path");
			use.printStackTrace();
		}
	}
	
	private class ScrollingPanel extends JPanel implements Scrollable {
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 10;
		}
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 100;
		}
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}
	}
	
	private File castDir, coreDir;
	private String[] localBookNames;
	private AllDates localDates, serverDates;
	private StringsHash bookDescriptionsHash;
	private boolean noServerAccess, castNeedsUpdate;
	
	private JButton videoButton, bookDownloadButton, updateButton, zipInstallButton, customiseButton;
	private JButton videoInfoButton, bookDownloadInfoButton, updateInfoButton, zipInstallInfoButton, customiseInfoButton;
	private JLabel videoLabel, bookDownloadLabel, updateLabel, zipInstallLabel, customiseLabel;
	@SuppressWarnings("unused")
	private JFrame videosFrame = null, bookDownloadFrame = null;
	@SuppressWarnings("unused")
	private JFrame customiseFrame = null;
//	private LocalUpdateFrame updateFrame = null;
	
	private JPanel bookListPanel;
	
	private JCheckBox advancedCheckbox;
	private ButtonListPanel advancedButtonPanel;
	
	private CollectionPanel[] collectionPanels;
	
	private cast.utils.UiTextStrings uiTextStrings;
	
	public BookChoiceWindow(File castDir) {
		super("CAST");
		this.castDir = castDir;
		
		uiTextStrings = new cast.utils.UiTextStrings(getLanguage(), new File(castDir, "core"));
		
		deleteHiddenFiles();
		
		coreDir = new File(castDir, "core");
		localBookNames = getBookNames(coreDir);
		
		if (Options.hasMultipleCollections)
			setupWindow();
		else
			new SetupDatesTask(castDir, this).execute();		//	does nothing else until local and server dates have been found
																											//	After this has happened, finishSetup() is called
	}
	
	public void startCAST(String bookName) {
		try {
			File coreDir = new File(castDir, "core");
//			File javaDir = new File(coreDir, "java");
//			File castJava = new File(javaDir, "coreCAST.jar");
//			String castPath = castJava.getCanonicalPath();
			
			AppletProgram.openBook(bookName, coreDir, null);
			
/*
			String command[] = {"java", "-jar", castPath, bookName};
			Runtime.getRuntime().exec(command, null, javaDir);
*/
		} catch (Exception e) {
			System.out.println("Error starting Java program");
			e.printStackTrace();
		}
	}
	
	private void deleteHiddenFiles() {		//	Hidden files should be automatically deleted when the manager is updated
		File[] hiddenFiles = castDir.listFiles(new FilenameFilter() {
																						public boolean accept(File dir, String name) {
																							return name.startsWith(".temp");
																						}
																					});
		for (int i=0 ; i<hiddenFiles.length ; i++)
			hiddenFiles[i].delete();
	}
	
	private String getLanguage() {
		File[] collections = findCollections();
		if (collections.length > 1)
			return "en";
		
		String s = HtmlHelper.getFileAsString(collections[0]);
		int languageIndex = s.indexOf("writeBody(\"");
		if (languageIndex < 0)
			return "en";
		else
			return s.substring(languageIndex + 11, languageIndex + 13);
	}
	
	private String translate(String key) {
		return uiTextStrings.translate(key);
	}
	

//---------------------------------------------------------------------


	private File[] findCollections() {
		return castDir.listFiles( new FilenameFilter() {
																					public boolean accept(File dir, String name) {
																						return name.startsWith("collection_");
																					}
																			});
	}
	
	private File findSingleCollectionFile() {
		File[] collectionFile = findCollections();
		return collectionFile[0];
	}
	
	private String[] getBookNames(File coreDir) {
		File booksDir = new File(coreDir, "bk");
		File[] dirs = booksDir.listFiles();
		int nBooks = 0;
		for (int i=0 ; i<dirs.length; i++)
			if (dirs[i].isDirectory()) {
				File xmlDir = new File(dirs[i], "xml");
				if (xmlDir.exists()) {
					File bookXmlFile = new File(xmlDir, "book.xml");
					if (bookXmlFile.exists())
						nBooks ++;
				}
			}
			
		String bookName[] = new String[nBooks];
		nBooks = 0;
		
		for (int i=0 ; i<dirs.length; i++)
			if (dirs[i].isDirectory()) {
				File xmlDir = new File(dirs[i], "xml");
				if (xmlDir.exists()) {
					File bookXmlFile = new File(xmlDir, "book.xml");
					if (bookXmlFile.exists())
						bookName[nBooks++] = dirs[i].getName();
				}
			}
		Arrays.sort(bookName);
		return bookName;
	}
	
	
	public void finishSetup(AllDates localDates, AllDates serverDates,
													StringsHash bookDescriptionsHash, boolean noServerAccess, boolean castNeedsUpdate) {
		this.localDates = localDates;
		this.serverDates = serverDates;
		this.bookDescriptionsHash = bookDescriptionsHash;
		this.noServerAccess = noServerAccess;
		this.castNeedsUpdate = castNeedsUpdate;
		
		setupWindow();
	}
	
	private void setupWindow() {
		setBackground(Color.white);
		
		setLayout(new BorderLayout(0, 0));
		
		add("North", new HeadingPanel(translate("CAST e-books"), null, changeToHtml(translate("_banner_text"), 450)));
	
				bookListPanel = new JPanel();
				bookListPanel.setLayout(new BorderLayout(0, 0));
				bookListPanel.setOpaque(false);
				
				JPanel updatePanel;
				if (Options.hasMultipleCollections)
					updatePanel = createMasterHeadingPanel();
				else if (noServerAccess)
					updatePanel = createLocalHeadingPanel();
				else
					updatePanel = createCoreHeadingPanel(castNeedsUpdate);
				updatePanel.setOpaque(false);
				
					Border underlineBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, kCheckboxBackground);
					Border spacingBorder = BorderFactory.createEmptyBorder(5, 0, 4, 0);
				updatePanel.setBorder(BorderFactory.createCompoundBorder(underlineBorder, spacingBorder));
				
				bookListPanel.add("North", updatePanel);
				
				bookListPanel.add("Center", createBookListPanel());
				
		add("Center", bookListPanel);
			
				JPanel bottomPanel = new JPanel();
				bottomPanel.setLayout(new BorderLayout(0, 0));
				bottomPanel.setOpaque(false);
				
					JPanel checkboxPanel = new JPanel();
					checkboxPanel.setBackground(kCheckboxBackground);
					checkboxPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 0));
					checkboxPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 5, 0));
					
						advancedCheckbox = new JCheckBox(translate("Advanced commands (mainly for authors)"));
						advancedCheckbox.setOpaque(false);
						advancedCheckbox.addActionListener(new ActionListener() {
																public void actionPerformed(ActionEvent e) {
																	advancedButtonPanel.setVisible(advancedCheckbox.isSelected());
//																	pack();
																	adjustWindowHeight();
																	setLocationRelativeTo(null);
																}
															});
						advancedCheckbox.setForeground(Color.white);
					checkboxPanel.add(advancedCheckbox);
					
						JLabel advancedDescription = new JLabel(changeToHtml(translate("_advanced_text"), 450));
						advancedDescription.setForeground(Color.white);
					checkboxPanel.add(advancedDescription);
					
	/*
					JPanel dummyPanel = new JPanel();
					dummyPanel.setPreferredSize(new Dimension(50, 200));
					dummyPanel.setBackground(Color.yellow);
					checkboxPanel.add(dummyPanel);
	*/
					
				bottomPanel.add("North", checkboxPanel);
			
					if (Options.hasMultipleCollections)
						advancedButtonPanel = createMasterAdvancedPanel();
					else if (noServerAccess)
						advancedButtonPanel = createLocalAdvancedPanel();
					else
						advancedButtonPanel = createCoreAdvancedPanel(castNeedsUpdate);
					advancedButtonPanel.setOpaque(false);
					advancedButtonPanel.setVisible(false);
					
				bottomPanel.add("South", advancedButtonPanel);
		
		add("South", bottomPanel);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
/*
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
										public void windowClosing(WindowEvent e) {
											if (bookDownloadFrame ==  null && videosFrame ==  null) {
												dispose();
												System.exit(0);
											}
										}
									});
*/
		
//		pack();
		adjustWindowHeight();
		setResizable(true);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void adjustWindowHeight() {
		pack();
		int bestHeight = getSize().height;

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// Used by task bar / dock
		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
		int maxWindowHeight = screenSize.height - screenInsets.bottom - screenInsets.top;
		
		if (bestHeight > maxWindowHeight) {
			setSize(new Dimension(getSize().width, maxWindowHeight));
			revalidate();
			repaint();
//			setSize(getSize().width, bestHeight);
//			revalidate();
		}
	}


//--------------------------------------------------
	
	
	private JPanel createMasterHeadingPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			JLabel localLabel = new JLabel(kMasterCastMessage, JLabel.CENTER);
			localLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
			localLabel.setForeground(Color.red);
		thePanel.add("North", localLabel);
		
		return thePanel;
	}
	
	
	private JPanel createLocalHeadingPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
					
			JLabel errorLabel = new JLabel(translate("Error!  Could not connect to the CAST server."), JLabel.CENTER);
			errorLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
			errorLabel.setForeground(Color.red);
		thePanel.add("North", errorLabel);
		
			JLabel errorMessage = new JLabel(changeToHtml(translate("_serverError_message"), 350));
		thePanel.add("Center", errorMessage);
		
		return thePanel;
	}
	
	
	private JPanel createCoreHeadingPanel(boolean castNeedsUpdate) {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(0, 20));
		
		if (!castNeedsUpdate && !Options.hasMultipleCollections) {
			JLabel message = new JLabel(translate("CAST is up to date"), JLabel.CENTER);
			message.setFont(new Font("SansSerif", Font.BOLD, 24));
			thePanel.add("North", message);
		}
		
		if (castNeedsUpdate) {
			ButtonListPanel coreButtonPanel = new ButtonListPanel();
			
			updateButton = new JButton(translate("Update CAST"));
			updateButton.addActionListener(new ActionListener() {
													public void actionPerformed(ActionEvent e) {
														showUpdateDialog();
													}
												});
			updateLabel = new JLabel(changeToHtml(translate("_update_message"), 350));
			updateInfoButton = coreButtonPanel.addRow(updateButton, updateLabel);
			if (updateInfoButton != null)
				updateInfoButton.addActionListener(new ActionListener() {
												public void actionPerformed(ActionEvent e) {
													showUrl("castUpdate_instructions.html");
												}
											});
			
			thePanel.add("Center", coreButtonPanel);
		}
		return thePanel;
	}


//--------------------------------------------------
	
	
	private JScrollPane createBookListPanel() {
		JScrollPane outerPanel = new JScrollPane();
		outerPanel.setBorder(BorderFactory.createEmptyBorder());
		
		JPanel thePanel = new ScrollingPanel();
		thePanel.setOpaque(false);

		if (Options.hasMultipleCollections) {	//	just a simple list of all books
			thePanel.setLayout(new GridLayout(0, 3));
			for (int i=0 ; i<localBookNames.length ; i++) {
				final String name = localBookNames[i];
				final JLabel nameLabel = new JLabel(name, JLabel.LEFT);
				nameLabel.setFont(kShortListFont);
				nameLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
				
				nameLabel.addMouseListener(new MouseListener() {
									public void mouseReleased(MouseEvent e) {}
									public void mousePressed(MouseEvent e) {}
									public void mouseExited(MouseEvent e) {
										nameLabel.setForeground(Color.black);
									}
									public void mouseEntered(MouseEvent e) {
										nameLabel.setForeground(Color.red);
									}
									public void mouseClicked(MouseEvent e) {
										startCAST(name);
									}
								});
				nameLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				thePanel.add(nameLabel);
			}
		}
		else {
			File collectionFile = findSingleCollectionFile();
//			File collectionFile = new File(castDir, "collection_massey.html");
			String collectionHtml = HtmlHelper.getFileAsString(collectionFile);
			Pattern collectionPattern = Pattern.compile(kEbookCollectionsPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher collectionMatcher = collectionPattern.matcher(collectionHtml);
			if (collectionMatcher.find()) {
				String nameString = collectionMatcher.group(1);
				nameString = nameString.substring(1, nameString.length()-1);		//	to get rid of start and end quotes
				String names[] = nameString.split("\", \"");
				
				String fileString = collectionMatcher.group(2);
				fileString = fileString.substring(1, fileString.length()-1);		//	to get rid of start and end quotes
				String files[] = fileString.split("\", \"");
				
				thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
				
				boolean hasCustomised = files[2].endsWith("ebooks.html");
					File installedFile = new File(coreDir, "installedBooks.js");
				boolean hasInstalled = installedFile.exists();
				int nCollections = 1 + (hasCustomised ? 1 : 0) + (hasInstalled ? 1 : 0);
				collectionPanels = new CollectionPanel[nCollections];
				collectionPanels[0] = new CollectionPanel(names[1], files[1], castDir, this);
				int i = 1;
				if (hasCustomised)
					collectionPanels[i++] = new CollectionPanel(names[2], files[2], castDir, this);
				if (hasInstalled) {
					Pattern customPattern = Pattern.compile(kCustomNamePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
					Matcher customMatcher = customPattern.matcher(collectionHtml);
					customMatcher.find();
					String customName = customMatcher.group(1);
			
					collectionPanels[i++] = new CollectionPanel(customName, "core/installedBooks.js", castDir, this);
				}
				
				for (int j=0 ; j<collectionPanels.length ; j++) {
					if (j == 0) {
						Border innerSpacing = BorderFactory.createEmptyBorder(12, 20, 0, 20);
						collectionPanels[j].setBorder(innerSpacing);
					}
					else {
						Border innerSpacing = BorderFactory.createEmptyBorder(10, 20, 0, 20);
						Border outerSpacing = BorderFactory.createEmptyBorder(10, 0, 0, 0);
						Border lineSpacing = BorderFactory.createMatteBorder(1, 0, 0, 0, HeadingPanel.kHeadingBackground);
						collectionPanels[j].setBorder(new CompoundBorder(outerSpacing, new CompoundBorder(lineSpacing, innerSpacing)));
					}
					thePanel.add(collectionPanels[j]);
				}
				
				expandCollection(collectionPanels[0]);
			}
			else {
				thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				JLabel errorLabel = new JLabel("Error: Cannot find collections", JLabel.LEFT);
				errorLabel.setForeground(Color.red);
				errorLabel.setFont(kShortListFont);
				thePanel.add(errorLabel);
			}
		}

		outerPanel.setViewportView(thePanel);
		outerPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		return outerPanel;
	}
	
	public void expandCollection(CollectionPanel thePanel) {
		for (int i=0 ; i<collectionPanels.length ; i++)
			collectionPanels[i].setExpanded(collectionPanels[i] == thePanel); 
	}


//--------------------------------------------------
	
	
	private ButtonListPanel createMasterAdvancedPanel() {
		ButtonListPanel masterButtonPanel = new ButtonListPanel();
			zipInstallButton = new JButton(translate("Install e-book from zip file"));
			zipInstallButton.addActionListener(new ActionListener() {
													public void actionPerformed(ActionEvent e) {
														showZipInstallFrame(coreDir, zipInstallButton);
														zipInstallButton.setEnabled(false);
													}
												});
			zipInstallLabel = new JLabel(changeToHtml(translate("_bookDownload_message"), 350));
			zipInstallInfoButton = masterButtonPanel.addRow(zipInstallButton, zipInstallLabel);
			if (zipInstallInfoButton != null)
				zipInstallInfoButton.addActionListener(new ActionListener() {
												public void actionPerformed(ActionEvent e) {
													showUrl("install_instructions.html");
												}
											});
		
		addCustomiseButton(masterButtonPanel);
		
		return masterButtonPanel;
	}
	
	
	private ButtonListPanel createLocalAdvancedPanel() {
		ButtonListPanel localButtonPanel = new ButtonListPanel();
			zipInstallButton = new JButton(translate("Install e-book from zip file"));
			zipInstallButton.addActionListener(new ActionListener() {
													public void actionPerformed(ActionEvent e) {
														showZipInstallFrame(coreDir, zipInstallButton);
														zipInstallButton.setEnabled(false);
													}
												});
			zipInstallLabel = new JLabel(changeToHtml(translate("_bookDownload_message"), 350));
			zipInstallInfoButton = localButtonPanel.addRow(zipInstallButton, zipInstallLabel);
			if (zipInstallInfoButton != null)
				zipInstallInfoButton.addActionListener(new ActionListener() {
												public void actionPerformed(ActionEvent e) {
													showUrl("install_instructions.html");
												}
											});
		
		addCustomiseButton(localButtonPanel);
		
		return localButtonPanel;
	}
	
	
	private ButtonListPanel createCoreAdvancedPanel(boolean castNeedsUpdate) {
		ButtonListPanel coreButtonPanel = new ButtonListPanel();
		
		bookDownloadButton = new JButton(translate("Install new e-book"));
		bookDownloadButton.addActionListener(new ActionListener() {
												public void actionPerformed(ActionEvent e) {
													showBookDownloadFrame(coreDir, bookDownloadButton);
													bookDownloadButton.setEnabled(false);
												}
											});
		bookDownloadLabel = new JLabel(changeToHtml(translate("_bookDownload_message"), 350));
		bookDownloadInfoButton = coreButtonPanel.addRow(bookDownloadButton, bookDownloadLabel);
		if (bookDownloadInfoButton != null)
			bookDownloadInfoButton.addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent e) {
												showUrl("install_instructions.html");
											}
										});
		if (castNeedsUpdate) {
			bookDownloadButton.setEnabled(false);
			bookDownloadLabel.setForeground(kDimTextColor);
		}

		videoButton = new JButton(translate("Download videos"));
		videoButton.addActionListener(new ActionListener() {
												public void actionPerformed(ActionEvent e) {
													showVideoFrame(coreDir, localBookNames, videoButton);
													videoButton.setEnabled(false);
												}
											});
		videoLabel = new JLabel(changeToHtml(translate("_video_message"), 350));
		videoInfoButton = coreButtonPanel.addRow(videoButton, videoLabel);
		if (videoInfoButton != null)
			videoInfoButton.addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent e) {
												showUrl("videoDownload_instructions.html");
											}
										});
		if (castNeedsUpdate) {
			videoButton.setEnabled(false);
			videoLabel.setForeground(kDimTextColor);
		}
		
		addCustomiseButton(coreButtonPanel);
		
		return coreButtonPanel;
	}
	
	private void addCustomiseButton(ButtonListPanel buttonPanel) {
		customiseButton = new JButton(translate("For authors only"));
		customiseButton.addActionListener(new ActionListener() {
												public void actionPerformed(ActionEvent e) {
														showCustomiseFrame(coreDir, customiseButton);
														customiseButton.setEnabled(false);
												}
											});
		customiseLabel = new JLabel(changeToHtml(translate("_customise_message"), 350));
		customiseInfoButton = buttonPanel.addRow(customiseButton, customiseLabel);
		if (customiseInfoButton != null)
			customiseInfoButton.addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent e) {
												showUrl("author_instructions.html");
											}
										});
	}
	

//---------------------------------------------------------------------


	private void showVideoFrame(File coreDir, String[] bookName, JButton callingButton) {
		videosFrame = new VideoDownloadFrame(bookName, coreDir, callingButton, this);
	}

	
	private void showBookDownloadFrame(File coreDir, JButton callingButton) {
		bookDownloadFrame = new BookInstallFrame(coreDir, localDates.getBookDates(),
										serverDates.getBookDates(), bookDescriptionsHash, true, callingButton, BookChoiceWindow.this);
	}

	
	private void showZipInstallFrame(File coreDir, JButton callingButton) {
		File datesDir = new File(coreDir, "dates");
		File localBookDatesFile = new File(datesDir, CoreCopyTask.kBookDatesFileName);
		DatesHash localDates = new DatesHash(localBookDatesFile);
		
		bookDownloadFrame = new BookInstallFrame(coreDir, localDates, null, null, false,
																																		callingButton, BookChoiceWindow.this);
	}
	
	
	private void showUpdateDialog() {
		LocalUpdateFrame updateFrame = new LocalUpdateFrame(castDir, localDates, serverDates, this);
		if (updateFrame.finishedUpdate) 
			setUpdated();
	}
	
	
	private void showCustomiseFrame(File coreDir, JButton callingButton) {
		customiseFrame = new AdvancedWindow(castDir, coreDir, localDates, serverDates,
														bookDescriptionsHash, noServerAccess, localBookNames,
														callingButton, this);
	}


//-----------------------------------------
	
	
	public void updateBookList() {
		File coreDir = new File(castDir, "core");
		localBookNames = getBookNames(coreDir);
		
		BorderLayout bl = (BorderLayout)bookListPanel.getLayout();
		Component oldList = bl.getLayoutComponent("Center");
		bookListPanel.remove(oldList);
//		bl.removeLayoutComponent(oldList);
		bookListPanel.add("Center", createBookListPanel());
		bookListPanel.validate();
		pack();
	}
	
	public void reenable(JButton button) {
		button.setEnabled(true);
		if (button == videoButton)
			videosFrame = null;
		else if (button == bookDownloadButton)
			bookDownloadFrame = null;
		else if (button == zipInstallButton)
			bookDownloadFrame = null;
		else if (button == customiseButton)
			customiseFrame = null;
	}
	
	public void setUpdated() {
		updateButton.setEnabled(false);
		updateInfoButton.setEnabled(false);
		updateLabel.setForeground(kDimTextColor);
		
		videoButton.setEnabled(true);
		videoInfoButton.setEnabled(true);
		videoLabel.setForeground(Color.black);
		
		bookDownloadButton.setEnabled(true);
		bookDownloadInfoButton.setEnabled(true);
		bookDownloadLabel.setForeground(Color.black);
		
		customiseButton.setEnabled(true);
		customiseInfoButton.setEnabled(true);
		customiseLabel.setForeground(Color.black);
	}

}