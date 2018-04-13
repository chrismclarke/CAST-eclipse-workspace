package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import exper2.*;


public class TwoFactorBalancedApplet extends XApplet {
	static final private String A_FACTOR_NAME_PARAM = "aFactorName";
	static final private String A_FACTOR_LEVELS_PARAM = "aFactorLevels";
	static final private String B_FACTOR_NAME_PARAM = "bFactorName";
	static final private String B_FACTOR_LEVELS_PARAM = "bFactorLevels";
	
	static final private Color kAFactorColor = new Color(0x000099);
	static final private Color kBFactorColor = new Color(0x990000);
	
	private int nALevels, nBLevels;
	private XNumberEditPanel aFactorEdit[], bFactorEdit[];
	
	private ReplicateCanvas repPanel;
	
	public void setupApplet() {
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridheight = gbc.gridwidth = 1;
			gbc.gridx = gbc.gridy = 0;
			gbc.insets = new Insets(0,0,0,0);
			gbc.ipadx = gbc.ipady = 0;
			gbc.weightx = gbc.weighty = 0.0;
			
			XPanel aLabelPanel = new InsetPanel(0, 0, getSize().width / 5, 0);
			aLabelPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				XLabel aLabel = new XLabel(getParameter(A_FACTOR_NAME_PARAM), XLabel.CENTER, this);
				aLabel.setFont(getBigBoldFont());
				aLabel.setForeground(kAFactorColor);
			aLabelPanel.add(aLabel);
		addLayoutComponent(aLabelPanel, gbl, gbc, 0, 2);
		
			XPanel bLabelPanel = new InsetPanel(0, 0, 0, getSize().height / 7);
			bLabelPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				XLabel bLabel = new XVertLabel(getParameter(B_FACTOR_NAME_PARAM), XLabel.CENTER, this);
				bLabel.setFont(getBigBoldFont());
				bLabel.setForeground(kBFactorColor);
			bLabelPanel.add(bLabel);
		addLayoutComponent(bLabelPanel, gbl, gbc, 2, 0);
		
		addLayoutComponent(aLevelsPanel(), gbl, gbc, 1, 2);
		
		addLayoutComponent(bLevelsPanel(), gbl, gbc, 2, 1);
		
			repPanel = new ReplicateCanvas(aFactorEdit, bFactorEdit);
			repPanel.setFont(getBigFont());
			gbc.weightx = gbc.weighty = 1.0;
		addLayoutComponent(repPanel, gbl, gbc, 2, 2);
	}
	
	private void addLayoutComponent(Component c, GridBagLayout gbl, GridBagConstraints gbc,
																																				int row, int col) {
		gbc.gridx = col;
		gbc.gridy = row;
		
		add(c);
		gbl.setConstraints(c, gbc);
	}
	
	private XPanel aLevelsPanel() {
		StringTokenizer st = new StringTokenizer(getParameter(A_FACTOR_LEVELS_PARAM), "#");
		nALevels = st.countTokens();
		aFactorEdit = new XNumberEditPanel[nALevels];
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new GridLayout(1, nALevels + 1));
		
		for (int i=0 ; i<nALevels ; i++) {
			XPanel levelPanel = new XPanel();
			levelPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
			
				aFactorEdit[i] = new XNumberEditPanel(null, "1", 3, this);
				aFactorEdit[i].setIntegerType(0, Integer.MAX_VALUE);
				aFactorEdit[i].setForeground(kAFactorColor);
			levelPanel.add(aFactorEdit[i]);
			
				String levelName = st.nextToken();
				XLabel levelLabel = new XLabel(levelName, XLabel.CENTER, this);
				levelLabel.setFont(getStandardBoldFont());
				levelLabel.setForeground(kAFactorColor);
			levelPanel.add(levelLabel);
				
			thePanel.add(levelPanel);
		}
		
			XPanel totalPanel = new XPanel();
			totalPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				XLabel totalLabel = new XLabel(translate("Total"), XLabel.CENTER, this);
				totalLabel.setFont(getStandardBoldFont());
				totalLabel.setForeground(kAFactorColor);
			totalPanel.add(totalLabel);
		
		thePanel.add(totalPanel);
		
		return thePanel;
	}
	
	private XPanel bLevelsPanel() {
		StringTokenizer st = new StringTokenizer(getParameter(B_FACTOR_LEVELS_PARAM), "#");
		nBLevels = st.countTokens();
		bFactorEdit = new XNumberEditPanel[nBLevels];
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new GridLayout(nBLevels + 1, 1));
		
		for (int i=0 ; i<nBLevels ; i++) {
			XPanel levelPanel = new XPanel();
			levelPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
			String levelName = st.nextToken();
			
			bFactorEdit[i] = new XNumberEditPanel(null, levelName, "1", 3, this);
			bFactorEdit[i].setIntegerType(0, Integer.MAX_VALUE);
			bFactorEdit[i].setLabelFont(getStandardBoldFont());
			bFactorEdit[i].setForeground(kBFactorColor);
			levelPanel.add(bFactorEdit[i]);
			
			thePanel.add(levelPanel);
		}
		
			XPanel totalPanel = new XPanel();
			totalPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				XLabel totalLabel = new XLabel(translate("Total"), XLabel.CENTER, this);
				totalLabel.setFont(getStandardBoldFont());
				totalLabel.setForeground(kBFactorColor);
			totalPanel.add(totalLabel);
		
		thePanel.add(totalPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		for (int i=0 ; i<aFactorEdit.length ; i++)
			if (target == aFactorEdit[i]) {
				repPanel.repaint();
				return true;
			}
		for (int i=0 ; i<bFactorEdit.length ; i++)
			if (target == bFactorEdit[i]) {
				repPanel.repaint();
				return true;
			}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}