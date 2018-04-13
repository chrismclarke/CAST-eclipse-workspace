package cast.sectionEditor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import cast.bookManager.*;
import cast.utils.*;
import cast.bookEditor.*;
import cast.pageEditor.*;


public class SectionEditor extends JFrame {
//	static final private Color kLineColor = new Color(0x330099);
	static final private Color kEditButtonBackground = new Color(0xAAAADD);
	
	
	private CastEbook castEbook;
	private CastSection castSection;
	
	private SectionHeader sectionHeaderPanel;
	private SectionContents sectionContentsPanel;
//	private PagePanel[] pagePanel;
	
	private JPanel editInfoPanel;
	
	public SectionEditor(String dir, String filePrefix, final CastEbook castEbook, final SectionTitle section) {
		super("Edit section " + dir + "/" + filePrefix);
		setPreferredSize(new Dimension(650, 800));
		BookEditor.offsetFrameFromParent(this, section);
		
		this.castEbook = castEbook;
		castSection = new CastSection(dir, filePrefix, castEbook);
		
		setLayout(new BorderLayout(0, 0));
		
		int startDisplayType;
		if (castEbook.canChangeStructure() && castSection.canEditSection()) {
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
			topPanel.setBackground(SectionHeader.kHeaderBackground);
			
			topPanel.add(editButtonPanel());
			
				sectionHeaderPanel = new SectionHeader(castSection, SectionHeader.EDIT_NOTHING);
			topPanel.add(sectionHeaderPanel);
			
				editInfoPanel = createEditInfoPanel();
				resetEditInfoPanel(true);
			topPanel.add(editInfoPanel);
			
			startDisplayType = SectionContents.DRAG_DROP_DISPLAY;
			
			add("North", topPanel);
		}
		else if (castSection.canEditSection()) {
			sectionHeaderPanel = new SectionHeader(castSection, SectionHeader.EDIT_TEXT);
			startDisplayType = SectionContents.EDIT_DISPLAY;
			add("North", sectionHeaderPanel);
		}
		else {
			sectionHeaderPanel = new SectionHeader(castSection, SectionHeader.EDIT_NOTHING);
			startDisplayType = SectionContents.DRAG_DROP_DISPLAY;
			add("North", sectionHeaderPanel);
		}
			
			sectionContentsPanel = new SectionContents(castSection, startDisplayType);
			JScrollPane scrollPane = new JScrollPane(sectionContentsPanel);
			scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.gray));
		add("Center", scrollPane);
		
		if (castEbook.canEditBook() && castEbook.canOnlyTranslate() && !"en".equals(castEbook.getLanguage())) {
			JPanel pagesPanel = new JPanel();
			pagesPanel.setBackground(SectionHeader.kHeaderBackground);
			pagesPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			
				final JButton pagesButton = new JButton("Translate pages");
				pagesButton.addActionListener(new ActionListener() {
																			public void actionPerformed(ActionEvent e) {
																				File[] sectionFiles = getSectionFiles();
																				new PageEditFrame(sectionFiles, pagesButton);
																				pagesButton.setEnabled(false);
																			}
																	});
			pagesPanel.add(pagesButton);
		
			add("South", pagesPanel);
		}
		
		addWindowListener( new WindowAdapter() {
									public void windowOpened( WindowEvent e ){
										sectionContentsPanel.requestFocus();
									}
	
									public void windowClosed(WindowEvent e) {
										section.enableEditButton();
										section.updateSectionName(sectionHeaderPanel.getSectionName());
									}
	
									public void windowClosing(WindowEvent e) {
										if (castSection.domHasChanged()) {
											Object[] options = {"Save", "Don't save", "Cancel"};
											int result = JOptionPane.showOptionDialog(SectionEditor.this, "Save changes to section?", "Save?",
																	JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[2]);

											switch (result) {
												case JOptionPane.YES_OPTION:
													updateDom();
													castSection.saveDom();			//	then dispose()
												case JOptionPane.NO_OPTION:
													dispose();
													break;
												case JOptionPane.CANCEL_OPTION:
												default:
													break;
											}
										}
										else
											dispose();
									}
								} ); 
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
	}
	
	private JPanel editButtonPanel() {
		class RadioListener implements ActionListener {
			private int radioIndex;
			
			public RadioListener(int radioIndex) {
				this.radioIndex = radioIndex;
			}
			
			public void actionPerformed(ActionEvent e) {
				updateDom();
				sectionHeaderPanel.relayout(radioIndex == 0 ? SectionHeader.EDIT_NOTHING : SectionHeader.EDIT_FULL);
				SectionContents.select(null);
				sectionContentsPanel.setDisplayType(radioIndex == 0
										? SectionContents.DRAG_DROP_DISPLAY : SectionContents.EDIT_DISPLAY);
				
				
				resetEditInfoPanel(radioIndex == 0);
			}
		}
		
		JPanel thePanel = new JPanel() {
																		public Insets getInsets() {
																			return new Insets(2, 0, 3, 0);
																		}
																};
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
		thePanel.setBackground(kEditButtonBackground);
		
			ButtonGroup bGroup = new ButtonGroup();
			
			JRadioButton dragDropButton = new JRadioButton("Change section structure");
			dragDropButton.addActionListener(new RadioListener(SectionContents.DRAG_DROP_DISPLAY));
			dragDropButton.setSelected(true);
			bGroup.add(dragDropButton);
		thePanel.add(dragDropButton);
			
			JRadioButton editButton = new JRadioButton("Edit text");
			editButton.addActionListener(new RadioListener(SectionContents.EDIT_DISPLAY));
			bGroup.add(editButton);
		thePanel.add(editButton);
		
		return thePanel;
	}
	
	private JPanel createEditInfoPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		thePanel.setBackground(kEditButtonBackground);
		return thePanel;
	}
	
	private void resetEditInfoPanel(boolean changeStructure) {
		editInfoPanel.removeAll();
		
		if (changeStructure) {
			JPanel innerPanel = new JPanel() {
																			public Insets getInsets() {
																				return new Insets(4, 10, 4, 2);
																			}
																	};
				innerPanel.setLayout(new BorderLayout(20, 0));
				innerPanel.setOpaque(false);
				
					TextBox dragDropInfo = new TextBox("Drag and drop pages from this or another section.\n"
																					+ "Drag the \"New page\" label on the right to add a page; "
																					+ "right-click on a page for a command to delete it.", 3);
				innerPanel.add("Center", dragDropInfo);
			
					JPanel newPanel = new JPanel();
					newPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
					newPanel.setOpaque(false);
					newPanel.add(new NewPagePanel());
				innerPanel.add("East", newPanel);
			
			editInfoPanel.add("Center", innerPanel);
		}
		editInfoPanel.revalidate();
		editInfoPanel.repaint();
	}
	
	private void updateDom() {
		Dom2Section sectionDom = castSection.getDomSection();
		sectionDom.updateDom(sectionHeaderPanel);
		
		int nPages = sectionDom.noOfChildren();
		for (int i=0 ; i<nPages ; i++) {
			CorePagePanel pagePanel = (CorePagePanel)sectionContentsPanel.getComponent(i);
			pagePanel.updatePageDom();
		}
	}
	
	private File[] getSectionFiles() {
		ArrayList<File> theFiles = new ArrayList<File>();
		
		Dom2Section domSection = castSection.getDomSection();
		int nChildren = domSection.noOfChildren();
		
		for (int i=0 ; i<nChildren ; i++) {
			Dom2Page domPage = domSection.getChild(i);
			String dir = domPage.getDirFromXml();
			String filePrefix = domPage.getFilePrefixFromXml();
			File page = castEbook.getPageHtmlFile(dir, filePrefix);
			
			theFiles.add(page);
		}		
		
		File[] fileArray = new File[theFiles.size()];
		for (int i=0 ; i<fileArray.length ; i++)
			fileArray[i] = theFiles.get(i);
		return fileArray;
	}
}
