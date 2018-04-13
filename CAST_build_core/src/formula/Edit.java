package formula;

import java.awt.*;

import dataView.*;
import utils.*;


public class Edit extends FormulaPanel {
	private XNumberEditPanel editField;
	
	public Edit(String startText, int columns, FormulaContext context) {
		super(context);
		
		editField = new XNumberEditPanel(null, startText, columns, context.getApplet());
//		editField.lockBackground(Color.white);
		editField.setForeground(context.getColor());
		editField.setFont(context.getFont());
//		editField.setTextListener(this);
		add(editField);
	}
	
	public void setValue(NumValue x) {
		editField.setDoubleValue(x);
	}
	
	public void setValue(int x) {
		editField.setIntegerValue(x);
	}
	
	public void setIntegerType() {
		editField.setIntegerType();
	}
	
	public NumValue getValue() {
		return editField.getNumValue();
	}
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			Dimension prefEditSize = editField.getPreferredSize();
			
			layoutWidth = prefEditSize.width;
			int layoutHeight = prefEditSize.height;
			
			FontMetrics fm = g.getFontMetrics();
			int ascent = fm.getAscent();
			int descent = fm.getDescent();
			int border = (layoutHeight - ascent - descent) / 2;
			layoutAscent = ascent + border;
			layoutDescent = layoutHeight - layoutAscent;
			return true;
		}
		else
			return false;
	}
	
	public void setColumns(int noOfCols) {
		editField.setColumns(noOfCols);
		reinitialise();
		editField.revalidate();
	}
	
	public void layoutContainer(Container parent) {
		initialise(getGraphics());
		editField.setBounds(0, 0, layoutWidth, getSize().height);
	}
	
	protected void paintAroundItems(Graphics g) {
	}
	
	protected double evaluateFormula() {
		return editField.getDoubleValue();
	}
	
	public void revalidate() {
		if (editField != null)
			editField.revalidate();
	}
}