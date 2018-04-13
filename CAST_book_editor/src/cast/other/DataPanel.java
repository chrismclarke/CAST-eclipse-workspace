package cast.other;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;


public class DataPanel extends JPanel {
	static final private Color kTitleBackground = new Color(0xEEEEEE);
	static final private Color kUnusedTitleColor = new Color(0x999999);
	
	private JCheckBox selectedCheck = null;
	private DataReference theData;
	private JPanel mainPanel;
	
	public DataPanel(final DataReference theData) {
		this.theData = theData;
		int nApplets = theData.noOfApplets();
		
		setLayout(new BorderLayout(0,0));
		
		Border underlineBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK);
		setBorder(underlineBorder);
		
		JPanel titlePanel = new JPanel();
			titlePanel.setBackground(kTitleBackground);
			titlePanel.setLayout(new BorderLayout(0, 0));
			Border spacingBorder = BorderFactory.createEmptyBorder(2, 10, 2, 5);
			titlePanel.setBorder(spacingBorder);
		
				String titleString = theData.getName();
				if (nApplets == 0)
					titleString += " (no data)";
				JLabel title = new JLabel(titleString, JLabel.LEFT);
				if (nApplets == 0)
					title.setForeground(kUnusedTitleColor);
				title.setFont(new Font("SansSerif", Font.BOLD, 16));
			titlePanel.add("Center", title);
		
			if (nApplets > 0) {
				selectedCheck = new JCheckBox();
				selectedCheck.setSelected(theData.isSelectedForExport());
				selectedCheck.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						theData.selectForExport(selectedCheck.isSelected());
						mainPanel.setVisible(selectedCheck.isSelected());
					}
				});
				titlePanel.add("West", selectedCheck);
			}
		add("North", titlePanel);
		
		if (nApplets > 0) {
			mainPanel = new JPanel();
			mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
			mainPanel.setBackground(Color.white);
			if (nApplets == 1) {
				Border mainSpacingBorder = BorderFactory.createMatteBorder(2, 10, 2, 5, Color.white);
				mainPanel.setBorder(mainSpacingBorder);
				PageReference pageWithData = theData.getBestPage();
				String pageTitle = getPageName(pageWithData);
				mainPanel.add(new JLabel(pageTitle, JLabel.LEFT));
			}
			else {
				final JComboBox pageChoicePopup = new JComboBox();
				final JComboBox appletChoicePopup = new JComboBox();
				
				final PageReference[] thePages = theData.getPages();		//		only includes pages with data applets
				for (int i=0 ; i<thePages.length ; i++)
					pageChoicePopup.addItem(getPageName(thePages[i]));
				pageChoicePopup.setSelectedIndex(theData.getBestPageIndex());
				
				pageChoicePopup.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
		        int selectedIndex = pageChoicePopup.getSelectedIndex();
		        theData.setBestApplet(thePages[selectedIndex], 0);
		        updateAppletIndexChoices(thePages[selectedIndex], appletChoicePopup);
			    }
				});
				mainPanel.add(new JLabel("page:"));
				mainPanel.add(pageChoicePopup);
				
				PageReference bestPage = theData.getBestPage();
				updateAppletIndexChoices(bestPage, appletChoicePopup);
				
				appletChoicePopup.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
		        int pageIndex = pageChoicePopup.getSelectedIndex();
		        int appletChoice = appletChoicePopup.getSelectedIndex();
		        theData.setBestApplet(thePages[pageIndex], appletChoice);
			    }
				});
				mainPanel.add(new JLabel("applet:"));
				mainPanel.add(appletChoicePopup);
			}
			add("Center", mainPanel);
			mainPanel.setVisible(selectedCheck.isSelected());
		}
	}
	
	private void updateAppletIndexChoices(PageReference p, JComboBox appletChoicePopup) {
		appletChoicePopup.removeAllItems();
		for (int i=0 ; i<p.noOfApplets() ; i++)
			appletChoicePopup.addItem(String.valueOf(i+1));
		appletChoicePopup.setSelectedIndex(theData.getBestAppletInPage());
	}
	
	private String getPageName(PageReference p) {
		return p.getPageName() + "(" + p.getDir() + "/" + p.getFilePrefix() + ")";
	}
	
	public boolean isDataSelected() {
		return selectedCheck != null && selectedCheck.isSelected();
	}
}
