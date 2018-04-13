package formula;

import java.awt.*;
import java.util.*;


public class TextFormulaSequence extends FormulaPanel {
	static final private int kTextFormulaGap = 4;
	
	static protected String[] makeArray(String s) {
		String[] sArray = new String[1];
		sArray[0] = s;
		return sArray;
	}
	
	private int versionIndex = 0;		//	to allow units to change
	
	private Vector items = new Vector();
	private int horizStart[];
	
	public TextFormulaSequence(FormulaContext context) {
		super(context);
	}
	
	public void reinitialise() {
		Enumeration e = items.elements();
		while (e.hasMoreElements()) {
			Object item = e.nextElement();
			if (item instanceof FormulaPanel) {
				((FormulaPanel)item).reinitialise();
			}
		}
		
		super.reinitialise();
	}
	
	public void setVersionIndex(int versionIndex) {
		this.versionIndex = versionIndex;
			
		reinitialise();
		
		validate();
		repaint();
	}
	
	public void addItem(Object item) {
		if (item instanceof FormulaPanel) {
			FormulaPanel formula = (FormulaPanel)item;
			formula.setParent(this);
			add(formula);
		}
		items.addElement(item);
	}
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			layoutAscent = ascent;
			layoutDescent = descent;
			layoutWidth = 0;
			FontMetrics fm = g.getFontMetrics();
			
			horizStart = new int[items.size()];
			
			Enumeration e = items.elements();
			int i=0;
			while (e.hasMoreElements()) {
				horizStart[i] = layoutWidth;
				Object item = e.nextElement();
				if (item instanceof FormulaPanel) {
					FormulaPanel formula = (FormulaPanel)item;
					formula.initialise(formula.getGraphics());
					layoutAscent = Math.max(layoutAscent, formula.layoutAscent);
					layoutDescent = Math.max(layoutDescent, formula.layoutDescent);
					layoutWidth += formula.layoutWidth + kTextFormulaGap;
				}
				else if (item instanceof String[]) {
					String[] textArray = (String[])item;
					layoutWidth += fm.stringWidth(textArray[versionIndex]) + kTextFormulaGap;
				}
				else {
					String text = (String)item;
					layoutWidth += fm.stringWidth(text) + kTextFormulaGap;
				}
				i++;
			}
			layoutWidth -= kTextFormulaGap;
			return true;
		}
		else
			return false;
	}
	
	public void layoutContainer(Container parent) {
		initialise(getGraphics());
		int baseHorizStart = (getSize().width - layoutWidth) / 2;
		int baseline = (getSize().height + layoutAscent - layoutDescent) / 2;
		
		Enumeration e = items.elements();
		int i=0;
		while (e.hasMoreElements()) {
			Object item = e.nextElement();
			if (item instanceof FormulaPanel) {
				FormulaPanel formula = (FormulaPanel)item;
				formula.setBounds(baseHorizStart + horizStart[i], baseline - formula.layoutAscent, formula.layoutWidth,
															formula.layoutAscent + formula.layoutDescent);
			}
			i++;
		}
	}
	
	protected void paintAroundItems(Graphics g) {
		int baseHorizStart = (getSize().width - layoutWidth) / 2;
		int baseline = (getSize().height + layoutAscent - layoutDescent) / 2;
		
		Enumeration e = items.elements();
		int i=0;
		while (e.hasMoreElements()) {
			Object item = e.nextElement();
			if (item instanceof String[]) {
				String[] textArray = (String[])item;
				g.drawString(textArray[versionIndex], baseHorizStart + horizStart[i], baseline);
			}
			else if (item instanceof String) {
				String text = (String)item;
				g.drawString(text, baseHorizStart + horizStart[i], baseline);
			}
			i++;
		}
	}
	
	protected double evaluateFormula() {
		return Double.NaN;
	}
}