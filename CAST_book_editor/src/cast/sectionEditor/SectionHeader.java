package cast.sectionEditor;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import cast.bookManager.*;
import cast.utils.*;


public class SectionHeader extends JPanel {
	static final public Color kHeaderBackground = new Color(0xDDDDFF);
	static final private Color kStaticTitleColor = new Color(0x000066);
//	static final private Color kTextBoxColor = new Color(0x666666);
	static final private Color kCommentColor = new Color(0x990000);
	
	
	static final public int EDIT_NOTHING = 0;
	static final public int EDIT_TEXT = 1;
	static final public int EDIT_FULL = 2;
	
	private CastSection castSection;
	private String sectionName, shortSectionName, topText;
	
	private boolean showingShortName;
	private JPanel namePanel;
	private JPanel nameEditPanel = null;
	
	private JPanel paragraphPanel;
	private JScrollPane initialParagraphScroll;
	
	public SectionHeader(CastSection castSection, int editType) {
		this.castSection = castSection;
		
		Dom2Section sectionDom = castSection.getDomSection();
		sectionName = sectionDom.getSectionNameFromXml();
		shortSectionName = sectionDom.getShortSectionNameFromXml();
		showingShortName = shortSectionName != null && shortSectionName.length() > 0;
		
		topText = sectionDom.getTopTextFromXml();
		
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 2));
		setBackground(kHeaderBackground);
		
		relayout(editType);
	}
	
	private boolean showingInitialParagraph() {
		return topText != null && topText.length() > 0;
	}
	
	public void relayout(int editType) {
		removeAll();
		
		paragraphPanel = null;
		initialParagraphScroll = null;
		
		createNamePanel(editType);
		add(namePanel);
		
		createParagraphPanel(editType);
		add(paragraphPanel);
		
		revalidate();
		repaint();
	}
	
	public Insets getInsets() {
		return new Insets(3, 10, 3, 10);
	}
	
	public String getSectionName() {
		return sectionName;
	}
	
	public String getShortSectionName() {
		return shortSectionName;
	}
	
	public String getTopText() {
		return topText;
	}
	
//------------------------------------------------------------------
	
	private void createParagraphPanel(int editType) {
		paragraphPanel = new JPanel();
		paragraphPanel.setLayout(new BorderLayout(10, 0));
		paragraphPanel.setOpaque(false);
		
		
		if (editType == EDIT_NOTHING) {
			if (showingInitialParagraph()) {
				JPanel tempPanel = new JPanel() {  public Insets getInsets() {  return new Insets(2, 30, 2, 30); }
																				};
				tempPanel.setOpaque(false);
				tempPanel.setLayout(new BorderLayout(0, 0));
				
				TextBox initialParagraphText = new TextBox(topText, 3);
				initialParagraphText.setDisabledTextColor(Color.black);
				tempPanel.add("Center", initialParagraphText);
				
				paragraphPanel.add("Center", tempPanel);
			}
		}
		else {
			JPanel leftPanel = new JPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_TOP, 0));
			leftPanel.setOpaque(false);
			
			if (editType == EDIT_FULL) {
				JCheckBox paragraphCheck = castSection.createMonitoredCheckBox("Initial paragraph", showingInitialParagraph());
				paragraphCheck.addItemListener(new ItemListener() {
																				public void itemStateChanged(ItemEvent itemEvent) {
//																					AbstractButton abstractButton = (AbstractButton)itemEvent.getSource();
																					int state = itemEvent.getStateChange();
																					topText = null;
																					updateParagraphPanel(state == ItemEvent.SELECTED);
																				}
														});
				leftPanel.add(paragraphCheck);
			}
			else {
				JLabel initialParagraphLabel = new JLabel("Initial paragraph: ");
				leftPanel.add(initialParagraphLabel);
			}
		
			paragraphPanel.add("West", leftPanel);
		
			updateParagraphPanel(showingInitialParagraph());
		}
	}
	
	private void updateParagraphPanel(boolean displayEditField) {
		if (displayEditField) {
			final JTextArea initialParagraph = castSection.createMonitoredTextArea(topText, 4, 30);
			initialParagraph.getDocument().addDocumentListener(new DocumentListener() {
																					public void changedUpdate(DocumentEvent e) {
																						updateTopText();
																					}
																					public void insertUpdate(DocumentEvent e) {
																						updateTopText();
																					}
																					public void removeUpdate(DocumentEvent e) {
																						updateTopText();
																					}
																					
																					private void updateTopText() {
																						String s = initialParagraph.getText();
																						topText = (s.length() == 0) ? null : s;
																					}
															});
			initialParagraphScroll = new JScrollPane(initialParagraph);
			
			paragraphPanel.add("Center", initialParagraphScroll);
		}
		else {
			if (initialParagraphScroll != null) {
				paragraphPanel.remove(initialParagraphScroll);
				initialParagraphScroll = null;
			}
		}
		paragraphPanel.revalidate();
		paragraphPanel.repaint();
	}
	
//------------------------------------------------------------------
	
	private void createNamePanel(int editType) {
		namePanel = new JPanel();
		namePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 2));
		namePanel.setOpaque(false);
		
		if (editType == EDIT_NOTHING)
			add(staticNamePanel());
		else {
			namePanel.add(nameControlPanel());
			updateNamePanel();
		}
	}
	
	private JPanel staticNamePanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(50, 0));
		thePanel.setOpaque(false);
		
			JLabel title = new JLabel(sectionName);
			title.setFont(new Font("SansSerif", Font.BOLD, 14));
			title.setForeground(kStaticTitleColor);
		thePanel.add("Center", title);
		
			JLabel comment = new JLabel("Can only be edited in book \"" + castSection.getDir() + "\"");
			comment.setFont(new Font("SansSerif", Font.PLAIN, 12));
			comment.setForeground(kCommentColor);
		thePanel.add("East", comment);
		
		return thePanel;
	}
	
	private JPanel nameControlPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(50, 0));
		thePanel.setOpaque(false);
		
			JLabel title = new JLabel("Section name");
			title.setFont(new Font("SansSerif", Font.BOLD, 14));
		thePanel.add("Center", title);
		
		JCheckBox shortNameCheck = castSection.createMonitoredCheckBox("Separate short name", showingShortName);
		shortNameCheck.addItemListener(new ItemListener() {
																			public void itemStateChanged(ItemEvent itemEvent) {
//																				AbstractButton abstractButton = (AbstractButton)itemEvent.getSource();
																				int state = itemEvent.getStateChange();
																				showingShortName = (state == ItemEvent.SELECTED);
																				updateNamePanel();
																			}
													});
		thePanel.add("East", shortNameCheck);
		
		return thePanel;
	}
	
	private void updateNamePanel() {
		if (nameEditPanel != null) {			nameEditPanel.removeAll();
			remove(nameEditPanel);
		}
		
		if (showingShortName)
			nameEditPanel = doubleNamePanel();
		else
			nameEditPanel = singleNamePanel();
		namePanel.add(nameEditPanel);
		namePanel.revalidate();
		namePanel.repaint();
	}
	
	private JTextField createSectionNameEdit() {
		final JTextField mainTitle = castSection.createMonitoredTextField(sectionName);
		mainTitle.getDocument().addDocumentListener(new DocumentListener() {
																				public void changedUpdate(DocumentEvent e) {
																					updateSectionName();
																				}
																				public void insertUpdate(DocumentEvent e) {
																					updateSectionName();
																				}
																				public void removeUpdate(DocumentEvent e) {
																					updateSectionName();
																				}
																				
																				private void updateSectionName() {
																					sectionName = mainTitle.getText();
																				}
														});
		return mainTitle;
	}
	
	private JTextField createShortNameEdit() {
		final JTextField bannerTitle = castSection.createMonitoredTextField(shortSectionName);
		bannerTitle.getDocument().addDocumentListener(new DocumentListener() {
																					public void changedUpdate(DocumentEvent e) {
																						updateShortName();
																					}
																					public void insertUpdate(DocumentEvent e) {
																						updateShortName();
																					}
																					public void removeUpdate(DocumentEvent e) {
																						updateShortName();
																					}
																					
																					private void updateShortName() {
																						String s = bannerTitle.getText();
																						shortSectionName = (s.length() == 0) ? null : s;
																					}
															});
		return bannerTitle;
	}
	
	private JPanel singleNamePanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		thePanel.setOpaque(false);
		
		thePanel.add("Center", createSectionNameEdit());
		
		
		return thePanel;
	}
	
	private JPanel doubleNamePanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 2));
		thePanel.setOpaque(false);
		
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new BorderLayout(10, 0));
			topPanel.setOpaque(false);
				JLabel topLabel = new JLabel("At top of section page:");
				topLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
			topPanel.add("West", topLabel);
			topPanel.add("Center", createSectionNameEdit());
		
		thePanel.add(topPanel);
		
			JPanel bottomPanel = new JPanel();
			bottomPanel.setLayout(new BorderLayout(10, 0));
			bottomPanel.setOpaque(false);
				JLabel bottomLabel = new JLabel("In banner & contents list:");
				bottomLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
			bottomPanel.add("West", bottomLabel);
			bottomPanel.add("Center", createShortNameEdit());
		
		thePanel.add(bottomPanel);
		
		return thePanel;
	}
	
}
