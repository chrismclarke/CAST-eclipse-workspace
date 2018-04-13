package pageStructure;

import java.awt.*;

import javax.swing.*;

import ebook.*;


public class LatexDrawer extends CoreDrawer {
	private String theLatex;
	
	public LatexDrawer(String theLatex) {
		this.theLatex = theLatex;
		this.theLatex = theLatex.replaceAll("\\&amp;", "&");
//		System.out.println(theLatex + "\n");
	}
	
	
	public JPanel createPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		thePanel.setOpaque(false);
//		thePanel.setOpaque(true);
//		thePanel.setBackground(Color.yellow);
			
		thePanel.add("Center", new LatexView(theLatex));
		
		return thePanel;
	}
}
