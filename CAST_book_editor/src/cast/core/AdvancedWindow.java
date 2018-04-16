package cast.core;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

import javax.swing.*;

import cast.utils.*;
import cast.bookManager.*;
import cast.exercise.*;
import cast.server.*;
//import cast.pageEditor.*;
import cast.other.*;


public class AdvancedWindow extends JFrame {
	static final private String kCustomiseText = changeToHtml("The core public e-books cannot be modified. It is however possible to create or edit a customised e-book. Use the \"Info\" button to find how.", 250);
	static final private String kTranslateText = changeToHtml("CAST e-books can be translated into other languages &mdash; click the \"Info\" button for detailed instructions. As part of the translation process, the \"Translate\" button allows titles and other parts of an e-book to be translated.", 250);
	static final private String kExercisesText = changeToHtml("The exercises in the CAST exercises e-book already have several variations. New variations of these exercises can also be created, based on different data sets or scenarios.", 250);
	
	static final private String kHeadingDescription = changeToHtml("The following advanced commands are only intended for teachers who might want to customise CAST e-books.", 450);
	
	static final public Color kMenuBackground = new Color(0x999999);
//	static final private Color kCheckboxBackground = HeadingPanel.kHeadingBackground;
//	static final private Color kDimTextColor = new Color(0x999999);
	
	static public String changeToHtml(String s, int width) {
		return "<html><div style='width:" + width + "px;'>" + s + "</div></html>";
	}
	
	static public void showUrl(String urlString) {
		try {
			final URI theUri = new URI(urlString);
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
	
	private File castDir, coreDir;
	private String[] localBookNames;
//	private AllDates localDates, serverDates;
//	private StringsHash bookDescriptionsHash;
	private boolean noServerAccess;
	private JButton callingButton;
	private BookChoiceWindow callingWindow;
	
	private JButton customiseButton, translateButton, exercisesButton;
	private JButton customiseInfoButton, translateInfoButton, exercisesInfoButton;
	private JLabel customiseLabel, translateLabel, exercisesLabel;
	private JFrame customiseFrame = null, translateFrame = null, exercisesFrame = null;
	
	protected Action tidySummariesAction, tidyHtmlAction, eclipseLauncherAction, collectionCreateAction, 
					createTabletBookAction, updateServerCastAction, pageEditAction, imageConverterAction, editSettingsAction;
	
	private JMenuBar mainMenuBar = new JMenuBar();
	protected JMenu toolMenu, settingsMenu;
	
	public AdvancedWindow(File castDir, File coreDir, AllDates localDates, AllDates serverDates,
																					StringsHash bookDescriptionsHash, boolean noServerAccess,
																					String[] localBookNames, JButton callingButton, BookChoiceWindow callingWindow) {
		super("CAST commands for authors");
		this.castDir = castDir;
		this.coreDir = coreDir;
//		this.localDates = localDates;
//		this.serverDates = serverDates;
//		this.bookDescriptionsHash = bookDescriptionsHash;
		this.noServerAccess = noServerAccess;
		this.localBookNames = localBookNames;
		this.callingButton = callingButton;
		this.callingWindow = callingWindow;
		setupWindow();
	}
	

//---------------------------------------------------------------------

	
	private void setupWindow() {
		setBackground(Color.white);
		
		setLayout(new BorderLayout(0, 20));
		
		add("North", new HeadingPanel("Advanced commands", "for authors", kHeadingDescription));
		
		add("Center", createAdvancedPanel());
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
										public void windowClosing(WindowEvent e) {
											if (customiseFrame == null && translateFrame == null && exercisesFrame ==  null) {
												dispose();
												callingWindow.reenable(callingButton);
											}
										}
									});
		
		toolMenu = new JMenu("Tools");
		toolMenu.setBackground(kMenuBackground);
		mainMenuBar.add(toolMenu);

		settingsMenu = new JMenu("Settings");
		settingsMenu.setBackground(kMenuBackground);
		mainMenuBar.add(settingsMenu);
		
		mainMenuBar.setBackground(kMenuBackground);
		setJMenuBar(mainMenuBar);
		createActions(coreDir);
		addToolMenus();
		addSettingsMenus();
		
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}


//--------------------------------------------------
	
	private ButtonListPanel createAdvancedPanel() {
		ButtonListPanel advancedButtonPanel = new ButtonListPanel();
		
		boolean foreignBooks = false;
		for (int i=0 ; i<localBookNames.length ; i++) {
			CastEbook theEbook = new CastEbook(coreDir, localBookNames[i], true);
			if (!theEbook.isEnglish()) {
				foreignBooks = true;
				break;
			}
		}
		
		customiseButton = new JButton("Customise e-book");
		customiseButton.addActionListener(new ActionListener() {
												public void actionPerformed(ActionEvent e) {
													showBookList(coreDir, localBookNames, customiseButton, false);
													customiseButton.setEnabled(false);
												}
											});
		customiseLabel = new JLabel(kCustomiseText);
		customiseInfoButton = advancedButtonPanel.addRow(customiseButton, customiseLabel);
		if (customiseInfoButton != null)
			customiseInfoButton.addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent e) {
												showUrl("http://" + Options.kHelpPath + "/customisation_instructions.html");
											}
										});
		if (foreignBooks) {
			translateButton = new JButton("Translate e-book");
			translateButton.addActionListener(new ActionListener() {
													public void actionPerformed(ActionEvent e) {
														showBookList(coreDir, localBookNames, translateButton, true);
														translateButton.setEnabled(false);
													}
												});
			translateLabel = new JLabel(kTranslateText);
			translateInfoButton = advancedButtonPanel.addRow(translateButton, translateLabel);
			if (translateInfoButton != null)
				translateInfoButton.addActionListener(new ActionListener() {
												public void actionPerformed(ActionEvent e) {
													showUrl("http://" + Options.kHelpPath + "/translation_instructions.html");
												}
											});
		}
				
		exercisesButton = new JButton("Customise exercise");
		exercisesButton.addActionListener(new ActionListener() {
												public void actionPerformed(ActionEvent e) {
													File exerciseXmlDir = FileFinder.getExerciseXmlFolder(castDir, null);
													if (exerciseXmlDir != null) {
														showTopicList(exerciseXmlDir, exercisesButton);
														exercisesButton.setEnabled(false);
													}
												}
											});
		exercisesLabel = new JLabel(kExercisesText);
		exercisesInfoButton = advancedButtonPanel.addRow(exercisesButton, exercisesLabel);
		if (exercisesInfoButton != null)
			exercisesInfoButton.addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent e) {
												showUrl("http://" + Options.kHelpPath + "/exercises_instructions.html");
											}
										});
		
		return advancedButtonPanel;
	}

//-----------------------------------------

	public void createActions(final File coreDir) {
/*
		tidySummariesAction = new AbstractAction("Clean index, datasets and Javascript from summary files...") {
																public void actionPerformed(ActionEvent e) {
																	TidySummaries theWindow = new TidySummaries(coreDir);
																	theWindow.pack();
																	theWindow.setVisible(true);
																	theWindow.toFront();
																}
														};
		tidyHtmlAction = new AbstractAction("Make global edits to files...") {
																public void actionPerformed(ActionEvent e) {
																	TidyHtml theWindow = new TidyHtml(coreDir);
																	theWindow.pack();
																	theWindow.setVisible(true);
																	theWindow.toFront();
																}
														};
*/
		collectionCreateAction = new AbstractAction("Create copy of a CAST collection...") {
																public void actionPerformed(ActionEvent e) {
																	CopyCollectionFrame theWindow = new CopyCollectionFrame(castDir);
																	theWindow.pack();
																	theWindow.setVisible(true);
																	theWindow.toFront();
																}
														};
/*
		createTabletBookAction = new AbstractAction("Create copy of a CAST tablet book ...") {
																public void actionPerformed(ActionEvent e) {
																	CreateTabletBookFrame theWindow = new CreateTabletBookFrame(castDir);
																	theWindow.pack();
																	theWindow.setVisible(true);
																	theWindow.toFront();
																}
														};
*/
		updateServerCastAction = new AbstractAction("Update CAST on server to latest version...") {
																public void actionPerformed(ActionEvent e) {
																	UpdateServerFrame theWindow = new UpdateServerFrame(castDir);
																	theWindow.pack();
																	theWindow.setVisible(true);
																	theWindow.toFront();
																}
														};
/*
		pageEditAction = new AbstractAction("Test page editor...") {
																public void actionPerformed(ActionEvent e) {
																	JFileChooser fc = new JFileChooser((File)null);
																	fc.setDialogTitle("Select an HTML file");
																	fc.setMultiSelectionEnabled(true);

															//	Show open dialog; this method does not return until the dialog is closed
																	int result = fc.showOpenDialog(AdvancedWindow.this);
																
																	switch (result) {
																		case JFileChooser.APPROVE_OPTION:
																			File[] htmlFiles = fc.getSelectedFiles();
																			new PageEditFrame(htmlFiles,  null);
																			break;
																		case JFileChooser.CANCEL_OPTION:
																		case JFileChooser.ERROR_OPTION:
																		default:
																			break;
																	}
																}
														};
		imageConverterAction = new AbstractAction("Convert images to Java code...") {
																public void actionPerformed(ActionEvent e) {
																	File projectsDir = castDir.getParentFile();
																	File bookProjectDir = new File(projectsDir, "CAST_build_book_editor");
																	File coreProjectDir = new File(projectsDir, "CAST_build_all");
																	if (bookProjectDir.exists() && coreProjectDir.exists()) {
																		ConvertImagesFrame theWindow = new ConvertImagesFrame(bookProjectDir);
																		theWindow.pack();
																		theWindow.setVisible(true);
																		theWindow.toFront();
																	}
																	else
																		JOptionPane.showMessageDialog(AdvancedWindow.this, "You cannot convert the images from outside.\n"
																																				+ "the development system structure.",
																																				"Error!", JOptionPane.ERROR_MESSAGE);
																}
														};
		eclipseLauncherAction = new AbstractAction("Create launcher files for Eclipse...") {
																public void actionPerformed(ActionEvent e) {
																	GenerateEclipseLaunches theWindow = new GenerateEclipseLaunches(coreDir);
																	theWindow.pack();
																	theWindow.setVisible(true);
																	theWindow.toFront();
																}
														};
*/
		editSettingsAction = new AbstractAction("Edit servers used by CAST") {
															public void actionPerformed(ActionEvent e) {
																Options.editSettings(coreDir);
															}
													};
	}
	
	private AbstractAction generateRDataAction(final String bookName) {
		return new AbstractAction(bookName) {
															public void actionPerformed(ActionEvent e) {
																GenerateRData theWindow = new GenerateRData(bookName, coreDir);
																theWindow.pack();
																theWindow.setVisible(true);
																theWindow.toFront();
															}
													};
	}
	
	private void addToolMenus() {
//		otherMenu.add(new JMenuItem(tidySummariesAction));
//		otherMenu.add(new JMenuItem(tidyHtmlAction));
//		otherMenu.add(new JMenuItem(pageEditAction));
//		otherMenu.add(new JMenuItem(imageConverterAction));
//		otherMenu.add(new JMenuItem(eclipseLauncherAction));
			JMenu generateRDataMenu = new JMenu("Generate R data set for book...");
			for (int i=0 ; i<localBookNames.length ; i++)
				generateRDataMenu.add(new JMenuItem(generateRDataAction(localBookNames[i])));
		toolMenu.add(generateRDataMenu);
		
		toolMenu.addSeparator();
		
			JMenuItem collectionItem = new JMenuItem(collectionCreateAction);
			collectionItem.setEnabled(Options.hasMultipleCollections);
		toolMenu.add(collectionItem);
			
//		otherMenu.add(new JMenuItem(createTabletBookAction));
		
			JMenuItem updateItem = new JMenuItem(updateServerCastAction);
			updateItem.setEnabled(Options.isMasterCast && !noServerAccess);
		toolMenu.add(updateItem);
	}
	
	private void addSettingsMenus() {
		JMenuItem serverSettingsItem = new JMenuItem(editSettingsAction);
		settingsMenu.add(serverSettingsItem);
	}
	

//---------------------------------------------------------------------


	private void showBookList(File coreDir, String[] bookName, JButton callingButton, boolean translateOnly) {
		BookListFrame bookList = new BookListFrame(bookName, coreDir, callingButton, this, translateOnly);
		bookList.pack();
		bookList.setVisible(true);
		bookList.toFront();
		
		if (translateOnly)
			translateFrame = bookList;
		else
			customiseFrame = bookList;
	}
	
	private void showTopicList(File exerciseXmlDir, JButton callingButton) {
		String[] topicName = ExerciseXmlHelper.getTopics(exerciseXmlDir);
		
		ExerciseListFrame exerciseList = new ExerciseListFrame(topicName, exerciseXmlDir, callingButton, this);
		exerciseList.setSize(800, 800);
		exerciseList.setResizable(false);
		exerciseList.setVisible(true);
		exerciseList.toFront();
		
		exercisesFrame = exerciseList;
	}


//-----------------------------------------
	
	
	public void reenable(JButton button) {
		button.setEnabled(true);
		if (button == customiseButton)
			customiseFrame = null;
		else if (button == translateButton)
			translateFrame = null;
		else if (button == exercisesButton)
			exercisesFrame = null;
	}
}