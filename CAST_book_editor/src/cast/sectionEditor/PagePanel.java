package cast.sectionEditor;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.w3c.dom.*;

import cast.bookManager.*;
import cast.utils.*;

public class PagePanel extends CorePagePanel {
	private JPanel descriptionPanel;
	private JTextArea descriptionEdit;
	private JScrollPane descriptionEditScroll;
	
	private JPanel notePanel, customPanel;
	private JTextField secNoteEdit = null, bannerNoteEdit = null, titleEdit = null, exerciseEdit = null;
	
	public PagePanel(Dom2Page pageDom, CastSection castSection, int pageNo) {
		super(pageDom, castSection, pageNo);
		
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 2));
		setOpaque(false);
		
			JPanel titlePanel = new JPanel();
			titlePanel.setLayout(new BorderLayout(0, 0));
			titlePanel.setBackground(kPageBackground);
			
				JPanel defaultNamePanel = createNamePanel(pageNo);
			titlePanel.add("Center", defaultNamePanel);
			
				JPanel buttonPanel = createButtonPanel();
			titlePanel.add("South", buttonPanel);
		
		add(titlePanel);
		
			customPanel = new JPanel() {
																	public Insets getInsets() { return new Insets(0, 40, 0, 0); }
																};
			customPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP));
			customPanel.setBackground(kPageBackground);
		add(customPanel);
		updateCustomisedPanel();
		
			notePanel = new JPanel() {
																	public Insets getInsets() { return new Insets(0, 10, 0, 0); }
																};
			notePanel.setLayout(new GridLayout(1, 2, 20, 0));
			notePanel.setBackground(kPageBackground);
		add(notePanel);
		updateNotePanel();
		
		add(createDescriptionPanel());
	}
	
	private String getDescription() {
		description = descriptionEdit.getText();
		return XmlHelper.encodeHtml(description, XmlHelper.WITHOUT_PARAGRAPHS);
	}
	
	private String getNote() {
		if (showNote) {
			String note = secNoteEdit.getText();
			String bannerNote = bannerNoteEdit.getText();
			if (bannerNote.length() > 0)
				note += "#" + bannerNote;
			if (note.length() > 0)
				return XmlHelper.encodeHtml(note, XmlHelper.WITHOUT_PARAGRAPHS);
			else
				return null;
		}
		else
			return null;
	}
	
	private String getCustomTitle() {
		switch (titleType) {
			case TITLE_CUSTOMISED:
				String newTitle = titleEdit.getText();
				return (newTitle.length() == 0) ? null : XmlHelper.encodeHtml(newTitle, XmlHelper.WITHOUT_PARAGRAPHS);
			case TITLE_EXERCISE:
				String newExercise = exerciseEdit.getText();
				String newExerciseTitle = titleEdit.getText();
				if (newExercise.length() == 0 && newExerciseTitle.length() == 0)
					return null;
				else {
					String encodedExercise = XmlHelper.encodeHtml(newExercise, XmlHelper.WITHOUT_PARAGRAPHS);
					String encodedExerciseTitle = XmlHelper.encodeHtml(newExerciseTitle, XmlHelper.WITHOUT_PARAGRAPHS);
					
					String combinedTitle = "#r#";
					if (encodedExercise.length() > 0)
						combinedTitle += encodedExercise;
					if (encodedExerciseTitle.length() > 0)
						combinedTitle += "#+#  " + encodedExerciseTitle;
					return combinedTitle;
				}
			case TITLE_FROM_FILE:
			default:
				return null;
		}
	}
	
	public void updatePageDom() {
		Element pageElement = pageDom.getDomElement();
//		Document domForSection = castSection.getDocument();
		
		String description = getDescription();
		pageElement.getFirstChild().setNodeValue(description);
		
		pageElement.setAttribute("dir", dir);
		pageElement.setAttribute("filePrefix", filePrefix);
		
		String note = getNote();
		if (note == null)
			pageElement.removeAttribute("note");
		else
			pageElement.setAttribute("note", note);
		
		String customTitle = getCustomTitle();
		if (customTitle == null)
			pageElement.removeAttribute("nameOverride");
		else
			pageElement.setAttribute("nameOverride", customTitle);
	}
	
//------------------------------------------------------------------
	
	private JPanel createDescriptionPanel() {
		descriptionPanel = new JPanel() {
																	public Insets getInsets() { return new Insets(3, 40, 3, 3); }
																};
		descriptionPanel.setLayout(new BorderLayout(0, 0));
		descriptionPanel.setBackground(kPageBackground);
		
			descriptionEdit = castSection.createMonitoredTextArea(description, 4, 30);
			descriptionEdit.setFont(new Font("SansSerif", Font.PLAIN, 12));
			descriptionEditScroll = new JScrollPane(descriptionEdit);
			
		descriptionPanel.add("Center", descriptionEditScroll);
		
		return descriptionPanel;
	}
	
//------------------------------------------------------------------
	
	private JPanel createNamePanel(int pageNo) {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		thePanel.setBackground(kPageBackground);
		
			titleInFile = new JLabel(pageNo + ". " + pageTitle);
			titleInFile.setFont(new Font("SansSerif", Font.BOLD, 14));
		thePanel.add("Center", titleInFile);
		
			location = new JLabel("(" + dir + ", " + filePrefix + ")");
			location.setFont(new Font("SansSerif", Font.PLAIN, 10));
		thePanel.add("East", location);
		return thePanel;
	}
	
	private JPanel createButtonPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 20, 0));
		thePanel.setBackground(kPageBackground);
		
		final CastEbook castEbook = castSection.getCastEbook();
		if (castEbook.canChangeStructure()) {
/*
			JButton pickFileButton = new JButton("Change file...");
			pickFileButton.addActionListener(new ActionListener() {
																	public void actionPerformed(ActionEvent e) {
																		File newPageFile = choosePageFile();
																		if (newPageFile != null) {
																			String filename = newPageFile.getName();
																			filePrefix = filename.substring(0, filename.length() - 5);
																			dir = newPageFile.getParentFile().getName();
																			
																			pageTitle = HtmlHelper.getTagInFile(dir, filePrefix, castEbook, "title");
																			titleInFile.setText(pageNo + ". " + pageTitle);
																			location.setText("(" + dir + ", " + filePrefix + ")");
																			castSection.setDomChanged();
																		}
																	}
														});
			thePanel.add(pickFileButton);
*/
			
			String[] menuItems = { "Title from file", "Customised title (standard)", "Customised title (exercise)" };
			JComboBox titleMenu = castSection.createMonitoredMenu(menuItems, titleType);
			titleMenu.addActionListener(new ActionListener() {
																	public void actionPerformed(ActionEvent e) {
																		JComboBox cb = (JComboBox)e.getSource();
																		titleType = cb.getSelectedIndex();
																		updateCustomisedPanel();
																	}
														});
			thePanel.add(titleMenu);
		}
		
			JCheckBox showNoteCheck = castSection.createMonitoredCheckBox("Comment", showNote);
			showNoteCheck.addItemListener(new ItemListener() {
																			public void itemStateChanged(ItemEvent itemEvent) {
//																				AbstractButton abstractButton = (AbstractButton)itemEvent.getSource();
																				int state = itemEvent.getStateChange();
																				showNote = (state == ItemEvent.SELECTED);
																				updateNotePanel();
																			}
													});
		thePanel.add(showNoteCheck);
		
		return thePanel;
	}
	
/*
	private File choosePageFile() {
		CastEbook castEbook = castSection.getCastEbook();
		File currentFile = castEbook.getPageHtmlFile(dir, filePrefix);
		JFileChooser fc = new JFileChooser(currentFile);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("Pick HTML file for page");
		fc.setFileHidingEnabled(true);

// Show open dialog; this method does not return until the dialog is closed
		int result = fc.showOpenDialog(this);
	
		switch (result) {
			case JFileChooser.APPROVE_OPTION:
				File pageFile = fc.getSelectedFile();
				
				String filename = pageFile.getName();
				if (filename.length() < 6 || filename.indexOf(".html") != filename.length() - 5) {
					JOptionPane.showMessageDialog(PagePanel.this, "You have not chosen an HTML file (with extention \".html\").", "Error!", JOptionPane.ERROR_MESSAGE);
					return null;
				}
				
				try {
					String chosenCoreDir = pageFile.getParentFile().getParentFile().getCanonicalPath();
					String coreDir = castEbook.getCoreDir().getCanonicalPath();
					if (!coreDir.equals(chosenCoreDir)) {
						JOptionPane.showMessageDialog(PagePanel.this, "The page file must be located in\na folder within the \"CAST/core\" folder.", "Error!", JOptionPane.ERROR_MESSAGE);
						return null;
					}
				}	catch (IOException e) {
					return null;
				}
				
				return pageFile;
		}
		return null;
	}
*/
	
	private void updateNotePanel() {
		if (showNote) {
				JPanel secPanel = new JPanel();
				secPanel.setLayout(new BorderLayout(5, 0));
				secPanel.setBackground(kPageBackground);
				
					JLabel secTitle = new JLabel("Comment in section page:");
					secTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
				secPanel.add("West", secTitle);
					
					String secNote = null;
					int hashIndex = -1;
					if (note != null) {
						hashIndex = note.indexOf('#');
						secNote = (hashIndex >= 0) ? note.substring(0, hashIndex) : note;
					}
					secNoteEdit = castSection.createMonitoredTextField(secNote);
					secNoteEdit.setFont(new Font("SansSerif", Font.PLAIN, 12));
				secPanel.add("Center", secNoteEdit);
				
				notePanel.add(secPanel);
				
				JPanel bannerPanel = new JPanel();
				bannerPanel.setLayout(new BorderLayout(5, 0));
				bannerPanel.setBackground(kPageBackground);
				
					JLabel bannerTitle = new JLabel("Comment in banner:");
					bannerTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
				bannerPanel.add("West", bannerTitle);
					
					String bannerNote = (hashIndex >= 0) ? note.substring(hashIndex + 1) : "";
					bannerNoteEdit = castSection.createMonitoredTextField(bannerNote);
					bannerNoteEdit.setFont(new Font("SansSerif", Font.PLAIN, 12));
				bannerPanel.add("Center", bannerNoteEdit);
				
				notePanel.add(bannerPanel);
		}
		else {
			notePanel.removeAll();
			secNoteEdit = null;
			bannerNoteEdit = null;
			note = null;
		}
		
		updateAfterResize();
		notePanel.revalidate();
		notePanel.repaint();
	}
	
	private void updateCustomisedPanel() {
		customPanel.removeAll();
		
		switch (titleType) {
			case TITLE_CUSTOMISED:
					JPanel titlePanel = new JPanel();
					titlePanel.setLayout(new BorderLayout(10, 0));
					titlePanel.setBackground(kPageBackground);
					
					if (customTitle == null || customTitle.length() == 0)
						customTitle = "New page title";
						
						JLabel startLabel = new JLabel("Title override:");
						startLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
					titlePanel.add("West", startLabel);
						
						titleEdit = castSection.createMonitoredTextField(customTitle);
						titleEdit.setFont(new Font("SansSerif", Font.PLAIN, 12));
					titlePanel.add("Center", titleEdit);
				
				customPanel.add(titlePanel);
				titleInFile.setForeground(kGreyColor);
				break;
				
			case TITLE_EXERCISE:
				String startText = null;
				String endText = null;
				if (customTitle == null || customTitle.indexOf("#r#") != 0) {
					startText = "Exercise:";
					endText = "New exercise name";
				}
				else {
					endText = customTitle.substring(3);
					int changeColorIndex = endText.indexOf("#+#");
					if (changeColorIndex >= 0) {
						startText = endText.substring(0, changeColorIndex);
						endText = endText.substring(changeColorIndex + 3);
						while (endText.length() > 0 && endText.charAt(0) ==' ')
							endText = endText.substring(1);
					}
				}
					
					JPanel exercisePanel = new JPanel();
					exercisePanel.setLayout(new BorderLayout(0, 0));
					exercisePanel.setBackground(kPageBackground);
						
						JPanel startPanel = new JPanel();
						startPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
						startPanel.setBackground(kPageBackground);
							JLabel exerciseLabel = new JLabel("Customised exercise title: ");
							exerciseLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
						startPanel.add(exerciseLabel);
					
							exerciseEdit = castSection.createMonitoredTextField(startText);
							exerciseEdit.setColumns(10);
							exerciseEdit.setFont(new Font("SansSerif", Font.PLAIN, 12));
						startPanel.add(exerciseEdit);
						
							JLabel plusLabel = new JLabel(" + ");
							plusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
						startPanel.add(plusLabel);
					
					exercisePanel.add("West", startPanel);
						
						titleEdit = castSection.createMonitoredTextField(endText);
						titleEdit.setFont(new Font("SansSerif", Font.PLAIN, 12));
					exercisePanel.add("Center", titleEdit);
				
				customPanel.add(exercisePanel);
				titleInFile.setForeground(kGreyColor);
				break;
				
			default:
			case TITLE_FROM_FILE:
				titleEdit = null;
				exerciseEdit = null;
				customTitle = null;
				titleInFile.setForeground(Color.black);
		}
		
		customPanel.revalidate();
		updateAfterResize();
	}
}
