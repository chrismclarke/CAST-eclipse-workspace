package pageStructure;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.text.*;

import utils.*;
import ebook.*;
import ebookStructure.*;


public class SectionView extends JPanel {
	static final private int kBaseSectionTitleFontSize = 24;
	static final private int kBasePageNameFontSize = 16;
	static final private int kBasePageNoteFontSize = 18;
	
	static private String kPageNoteFontName = "Comic Sans MS";
	
	static final private Color kPageNameColor = new Color(0x0000DD);
	static final private Color kNoteColor = new Color(0xCC0000);
	
	private int sectionTitleFontSize, pageNameFontSize, pageNoteFontSize;
	private Font titleFont, pageNameFont, pageDescriptionFont, pageNoteFont;
	
	
	public SectionView(DomSection theSection, BookFrame theBookFrame) {
		initialiseFonts();
		
		setOpaque(false);
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		setBorder(BorderFactory.createEmptyBorder(15, 0, 20, 0));
		
			String sectionTitle = theSection.getLongName();
			File structureDir = theBookFrame.getEbook().getStructureDir();
		add(getTitlePanel(sectionTitle, structureDir));
		
			String sectionOverview = theSection.getSectionOverview();
		if (sectionOverview != null) {
			JTextPane overview = new JTextPane();
			overview.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
			overview.setOpaque(false);
			overview.setContentType("text/html");
			sectionOverview = sectionOverview.replace("<p>", "<p style='margin-top:8px; margin-bottom:8px;'>");
			overview.setText("<html><body style='margin-top:0px; margin-bottom:0px; margin-left:0px; background-color:#FFFFFF;'>" + sectionOverview + "</body></html>");
			overview.setFont(pageDescriptionFont);
			overview.setSelectionStart(0);
			overview.setSelectionEnd(0);
					MutableAttributeSet set = new SimpleAttributeSet(overview.getParagraphAttributes());
					StyleConstants.setLineSpacing(set, 0.1f);
			overview.setParagraphAttributes(set, true);
			overview.setEditable(false);
		
			add(overview);
		
		}
		
		for (int i=0 ; i<theSection.noOfChildren() ; i++)
			add(getPageDescription((DomPage)theSection.getChild(i), theBookFrame));
		
		add(new NextButtonDrawer(theSection, theBookFrame).createPanel());
	}
	
	private void initialiseFonts() {
		sectionTitleFontSize = CoreDrawer.scaledSize(kBaseSectionTitleFontSize);
		pageNameFontSize = CoreDrawer.scaledSize(kBasePageNameFontSize);
		pageNoteFontSize = CoreDrawer.scaledSize(kBasePageNoteFontSize);
		
		titleFont = new Font(CoreDrawer.kSerifFontName, Font.BOLD, sectionTitleFontSize);
		
		pageNameFont = new Font(CoreDrawer.kSerifFontName, Font.BOLD, pageNameFontSize);
			Map attributes = pageNameFont.getAttributes();
			attributes.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_TWO_PIXEL);
			pageNameFont = pageNameFont.deriveFont(attributes);
		
		pageDescriptionFont = new Font(CoreDrawer.kSerifFontName, Font.PLAIN, pageNameFontSize);
		
		pageNoteFont = new Font("Arial", Font.BOLD, sectionTitleFontSize);
			GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
			String[] fontNames = e.getAvailableFontFamilyNames(); // Get the fonts
			for (String f : fontNames) {
				if (f.equals(kPageNoteFontName)) {
					pageNoteFont = new Font(kPageNoteFontName, Font.BOLD, pageNoteFontSize);
					break;
				}
			}
	}
	
	
	private JPanel getTitlePanel(String titleString, File structureDir) {
		JPanel thePanel = new JPanel();
		thePanel.setOpaque(false);
		
		GridBagLayout gbl = new GridBagLayout();
		thePanel.setLayout(gbl);
		thePanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 10, 0));
		
			GridBagConstraints starC = new GridBagConstraints();
			starC.anchor = GridBagConstraints.CENTER;
			starC.fill = GridBagConstraints.NONE;
			starC.gridheight = 1;
			starC.gridwidth = 1;
			starC.gridx = 0;
			starC.gridy = 0;
			starC.insets = new Insets(0,0,0,5);
			starC.ipadx = starC.ipady = 0;
			starC.weightx = 0.0;
			starC.weighty = 0.0;
			
		File starFile = new File(structureDir, "images/star.png");
		UiImage starImage = new UiImage(starFile, true);
		starImage.setPageScaling(true);
		thePanel.add(starImage);
			gbl.setConstraints(starImage, starC);
		
			GridBagConstraints titleC = new GridBagConstraints();
			titleC.anchor = GridBagConstraints.WEST;
			titleC.fill = GridBagConstraints.NONE;
			titleC.gridheight = 1;
			titleC.gridwidth = 1;
			titleC.gridx = 1;
			titleC.gridy = 0;
			titleC.insets = new Insets(0,2,0,0);
			titleC.ipadx = titleC.ipady = 0;
			titleC.weightx = 1.0;
			titleC.weighty = 0.0;
			
		JLabel titleLabel = new JLabel(titleString, JLabel.LEFT);
		titleLabel.setFont(titleFont);
		titleLabel.setForeground(Color.red);
		titleLabel.setOpaque(false);
		thePanel.add(titleLabel);
			gbl.setConstraints(titleLabel, titleC);
		
			GridBagConstraints lineC = new GridBagConstraints();
			lineC.anchor = GridBagConstraints.WEST;
			lineC.fill = GridBagConstraints.NONE;
			lineC.gridheight = 1;
			lineC.gridwidth = 2;
			lineC.gridx = 0;
			lineC.gridy = 1;
			lineC.insets = new Insets(2,0,0,0);
			lineC.ipadx = lineC.ipady = 0;
			lineC.weightx = 1.0;
			lineC.weighty = 0.0;
			
		File lineFile = new File(structureDir, "images/sectionUnderscore.png");
		UiImage underlineImage = new UiImage(lineFile, false);
		underlineImage.setPageScaling(true);
		thePanel.add(underlineImage);
			gbl.setConstraints(underlineImage, lineC);
		
		return thePanel;
	}
	
	
	
	private JPanel getPageDescription(final DomPage thePage, final BookFrame theBookFrame) {
		JPanel thePanel = new JPanel();
		thePanel.setOpaque(false);
		thePanel.setBorder(BorderFactory.createEmptyBorder(8, 40, 0, 0));
		
		String pageTitle = thePage.getIndex() + ". " + thePage.getName();
		String pageDescription = thePage.getPageDescription();
		
		ToolTipManager.sharedInstance().setInitialDelay(2000);
		ToolTipManager.sharedInstance().setDismissDelay(10000);
		
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		
			JPanel titlePanel = new JPanel();
			titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 0));
			titlePanel.setOpaque(false);
				JLabel titleLabel = new JLabel(pageTitle, JLabel.LEFT);
				titleLabel.setFont(pageNameFont);
				titleLabel.setForeground(kPageNameColor);
				titleLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				titleLabel.addMouseListener(new MouseAdapter() {  
												public void mouseClicked(MouseEvent e) {
													theBookFrame.showPage(thePage, BookFrame.FROM_PAGE);
												}  
										});
				titlePanel.setToolTipText("(" + thePage.getDirStrings()[DomPage.FULL_VERSION] + "/" + thePage.getFilePrefixStrings()[DomPage.FULL_VERSION] + ")");
			titlePanel.add(titleLabel);
			
			String pageNote = thePage.getSectionNote();
			if (pageNote != null) {
				JLabel noteLabel = new JLabel(pageNote, JLabel.LEFT);
				noteLabel.setFont(pageNoteFont);
				noteLabel.setForeground(kNoteColor);
				titlePanel.add(noteLabel);
			}
			
		thePanel.add(titlePanel);
		
			JTextPane description = new JTextPane();
			description.setBorder(BorderFactory.createEmptyBorder(2, 40, 0, 0));
			description.setOpaque(false);
			description.setText(pageDescription);
			description.setFont(pageDescriptionFont);
			description.setSelectionStart(0);
			description.setSelectionEnd(0);
					MutableAttributeSet set = new SimpleAttributeSet(description.getParagraphAttributes());
					StyleConstants.setLineSpacing(set, 0.1f);
			description.setParagraphAttributes(set, true);
			description.setEditable(false);
		
		thePanel.add(description);
		
		return thePanel;
	}
}