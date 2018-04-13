package cast.bookEditor;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import cast.bookManager.*;
import cast.sectionEditor.*;


public class SectionTitle extends ElementTitle {
	
	static final private Color kSelectedBackground = new Color(0xBBBBFF);
	static final private Color kGreyColor = ChapterTitle.kGreyColor;
//	static final private Color kLockedColor = new Color(0x550000);
	
	private CastEbook castEbook;
	private int sectionIndex;
	
	private JButton editButton;
	private boolean showingButtons = false;
	
	private JPanel buttonPanel;
	private JLabel fileLocation;
	
	private JMenuItem copyItem = null, editItem = null, lookItem = null;
	
	public SectionTitle(final DomSection domSection, final CastEbook castEbook, int sectionIndex) {
		super(domSection);
		this.castEbook = castEbook;
		this.sectionIndex = sectionIndex;
		
		setLayout(new BorderLayout(0, 0));
		setOpaque(false);
		
		String decodedTitle = domSection.getSectionName();
		title = new JLabel(sectionIndex + ". " + decodedTitle);
		title.setFont(new Font("SansSerif", Font.PLAIN, 12));
		add("Center", title);
		
		boolean onlyTranslate = castEbook.canEditBook() && !castEbook.canChangeStructure();
		
		if (onlyTranslate) {
			addMouseListener(new MouseAdapter() {
																	public void mouseClicked(MouseEvent event) {  
																		toggleButtons();
																	}
														});
		}
		else {
			fileLocation = new JLabel("");
			setFileLocation(domSection);
			fileLocation.setFont(new Font("SansSerif", Font.ITALIC, 11));
			fileLocation.setForeground(kGreyColor);
			
			add("East", fileLocation);
			
			if (!CastEbook.isPreface(domSection.getFilePrefix())) {
				
				menu = new JPopupMenu();
				if (castEbook.canEditBook()) {
					copyItem = new JMenuItem("Replace with Editable Copy...");
					copyItem.addActionListener(new ActionListener() {
																			public void actionPerformed(ActionEvent e) {
																				if (domSection.createCopyInEbook(SectionTitle.this)) {
																					menu.remove(lookItem);
																					menu.remove(copyItem);
																					menu.add(editItem, 0);
																					setFileLocation(domSection);
																				}
																			}
															});

					lookItem = new JMenuItem("Look");
					lookItem.addActionListener(new EditActionListener(domSection));

					editItem = new JMenuItem("Edit");
					editItem.addActionListener(new EditActionListener(domSection));
					
					if (domSection.getDir().equals(castEbook.getHomeDirName()))
						menu.add(editItem);
					else {
						menu.add(lookItem);
						menu.add(copyItem);
					}
					
					JMenuItem deleteItem = new JMenuItem("Delete");
					menu.add(deleteItem);
					deleteItem.addActionListener(new ActionListener() {
																	public void actionPerformed(ActionEvent e) {
																		int result = JOptionPane.showConfirmDialog(SectionTitle.this, "Are you sure that you want to delete this section?",
																								"Delete Section?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
																		
																		if (result == JOptionPane.OK_OPTION)
																			deleteSelf(castEbook);
																	}
													});
				}
				else {
					lookItem = new JMenuItem("Look");
					lookItem.addActionListener(new EditActionListener(domSection));
					menu.add(lookItem);
				}
				
				setMenuDragMouseListener(castEbook);
			}
		}
		
		setTitleForeground(Color.black, kGreyColor, castEbook);
	}
	
	private class EditActionListener implements ActionListener {
		private DomSection domSection;
		
		EditActionListener(DomSection domSection) {
			this.domSection = domSection;
		}
		
		public void actionPerformed(ActionEvent e) {
			SectionEditor editFrame = new SectionEditor(domSection.getDir(),
																domSection.getFilePrefix(), castEbook, SectionTitle.this);
			editFrame.pack();
			editFrame.setVisible(true);
			editFrame.toFront();
		}
	}
	
	public void updateSectionName(String newSectionName) {
		title.setText(sectionIndex + ". " + newSectionName);
	}
	
	private void toggleButtons() {
		if (showingButtons) {
			remove(buttonPanel);
//			setBackground(Color.white);
			setOpaque(false);
		}
		else {
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			buttonPanel.setBackground(kSelectedBackground);
			
			final DomSection domSection = (DomSection)getDomElement();
			
				String buttonName = domSection.getDir().equals(castEbook.getHomeDirName()) ? "Edit section" : "Look";
				
				editButton = new JButton(buttonName);
				editButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								SectionEditor editFrame = new SectionEditor(domSection.getDir(), domSection.getFilePrefix(), castEbook, SectionTitle.this);
								editFrame.pack();
								editFrame.setVisible(true);
								editFrame.toFront();
								
								editButton.setEnabled(false);
							}
						});
			buttonPanel.add(editButton);
			
			add("East", buttonPanel);
			setOpaque(true);
			setBackground(kSelectedBackground);
		}
		showingButtons = !showingButtons;
		
		revalidate();
		repaint();
	}
	
	public Insets getInsets() {
		return new Insets(0, 40, 0, 0);
	}
	
	public void enableEditButton() {
		if (editButton != null)
			editButton.setEnabled(true);
	}
	
	private void setFileLocation(DomSection domSection) {
		if (fileLocation == null)
			return;
		String dir = domSection.getDir();
		String filePrefix = domSection.getFilePrefix();
		if (!castEbook.canChangeStructure())
			fileLocation.setText("");
		else if (dir.equals(castEbook.getHomeDirName()))
			fileLocation.setText(filePrefix + "    ");
		else
			fileLocation.setText(filePrefix + " (in " + dir + ")    ");
	}
}
