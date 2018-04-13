package utils;

import java.awt.*;


public class FlexGridLayout extends GridBagLayout {
	private int hGap, vGap;
	private int expandX, expandY;
	private int noOfCols;
	
	private GridBagConstraints constraints;
	
	public FlexGridLayout(int noOfCols, int hGap, int vGap, int expandX, int expandY) {
		this.noOfCols = noOfCols;
		this.hGap = hGap;
		this.vGap = vGap;
		this.expandX = expandX;
		this.expandY = expandY;
		
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(0,0,0,0);
		constraints.gridheight = constraints.gridwidth = 1;
		constraints.ipadx = constraints.ipady = 0;
		
		constraints.fill = GridBagConstraints.BOTH;
		
		constraints.anchor = GridBagConstraints.CENTER;
	}
	
	public FlexGridLayout(int noOfCols, int hGap, int vGap) {
		this(noOfCols, hGap, vGap, -1, -1);
	}

	public void addLayoutComponent(Component comp, Object dummy) {
		if (constraints.gridx == noOfCols) {
			constraints.gridx = 0;
			constraints.gridy ++;
		}
		
		constraints.weightx = (constraints.gridx == expandX) ? 1.0 : 0.0;
		constraints.weighty = (constraints.gridy == expandY) ? 1.0 : 0.0;
		
		constraints.insets.left = (constraints.gridx == 0) ? 0 : hGap;
		constraints.insets.top = (constraints.gridy == 0) ? 0 : vGap;
		
		super.addLayoutComponent(comp, constraints);
		
		constraints.gridx ++;
	}

	public void addLayoutComponent(String name, Component comp) {
		addLayoutComponent(comp, null);
	}
}