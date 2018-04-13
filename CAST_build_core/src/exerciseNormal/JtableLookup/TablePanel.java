package exerciseNormal.JtableLookup;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;

import dataView.*;
import utils.*;
import exercise2.*;


public class TablePanel extends XPanel implements AdjustmentListener, StatusInterface {
	private DataSet data;
	private String zKey;
	
	private TableLayout theLayout;
	
	private TableView theTable;
	private TableHorizHeading hHeading;
	private TableVertHeading vHeading;
	private JScrollBar hScroll, vScroll;
	
	public TablePanel(DataSet data, String zKey, ExerciseApplet exerciseApplet) {
		this.data = data;
		this.zKey = zKey;
		Font headingFont = exerciseApplet.getStandardBoldFont();
		
		theLayout = new TableLayout();
		setLayout(theLayout);
		
		XLabel zLabel = new XLabel("z", XLabel.CENTER, exerciseApplet);
		zLabel.setFont(headingFont);
		add(TableLayout.TOP_LEFT, zLabel);
		
		hScroll = new JScrollBar(JScrollBar.HORIZONTAL);
		hScroll.addAdjustmentListener(this);
		add(TableLayout.HORIZ_SCROLL, hScroll);
		
		vScroll = new JScrollBar(JScrollBar.VERTICAL);
		vScroll.addAdjustmentListener(this);
		add(TableLayout.VERT_SCROLL, vScroll);
		
		theTable = new TableView(data, exerciseApplet, zKey, this);
		theTable.setFont(exerciseApplet.getStandardFont());
		theTable.lockBackground(Color.white);
		add(TableLayout.VALUES, theTable);
		
		hHeading = new TableHorizHeading(theTable, exerciseApplet);
		hHeading.setFont(headingFont);
		add(TableLayout.HORIZ_HEADING, hHeading);
		
		vHeading = new TableVertHeading(theTable, exerciseApplet);
		vHeading.setFont(headingFont);
		add(TableLayout.VERT_HEADING, vHeading);
		
		theTable.setHeadings(hHeading, vHeading);
	}
	
	public Insets insets() {
		return new Insets(3, 3, 3, 3);
	}
	
	public void adjustmentValueChanged(AdjustmentEvent e) {
		int newOrgX = -hScroll.getValue() * TableLayout.kPixelShift;
		int newOrgY = -vScroll.getValue() * TableLayout.kPixelShift;
		
//		System.out.println("hScroll: min = " + hScroll.getMinimum() + ", max = " + hScroll.getMaximum()
//													+ ", value = " + hScroll.getValue()+ ", visibleAmount = " + hScroll.getVisibleAmount());
//		System.out.println("vScroll: min = " + vScroll.getMinimum() + ", max = " + vScroll.getMaximum()
//													+ ", value = " + vScroll.getValue()+ ", visibleAmount = " + vScroll.getVisibleAmount());
//		System.out.println("horiz scroll to: newOrgX = " + newOrgX + ", newOrgY = " + newOrgY);
		
		theTable.setOrigin(newOrgX, newOrgY, hHeading, vHeading);
	}
	
	public void showAnswer(NumValue lowVal, NumValue highVal) {
		if (lowVal == null)
			showAnswer(highVal, highVal);
		else if (highVal == null)
			showAnswer(lowVal, lowVal);
		else if (lowVal.toDouble() > highVal.toDouble())
			showAnswer(highVal, lowVal);
		else{
			data.setSelection(zKey, lowVal.toDouble(), highVal.toDouble());
			scrollToSelection();
		}
	}
	
	public String getStatus() {
		DistnVariable zVar = (DistnVariable)data.getVariable(zKey);
			double lowVal = zVar.getMinSelection();
			if (lowVal == Double.NEGATIVE_INFINITY)
				lowVal = -6;
			double highVal = zVar.getMaxSelection();
			if (highVal == Double.POSITIVE_INFINITY)
				highVal = 6;
		NumValue lowSelection = new NumValue(lowVal, 2);
		NumValue highSelection = new NumValue(highVal, 2);
		return lowSelection + " " + highSelection;
	}
	
	public void setStatus(String status) {
		StringTokenizer st = new StringTokenizer(status);
		NumValue lowSel = new NumValue(st.nextToken());
		if (lowSel.toDouble() < -5)
			lowSel.setValue(Double.NEGATIVE_INFINITY);
		NumValue highSel = new NumValue(st.nextToken());
		if (highSel.toDouble() > 5)
			highSel.setValue(Double.POSITIVE_INFINITY);
		showAnswer(lowSel, highSel);
	}
	
	private void scaleRectForScroll(Rectangle r) {
		int left = r.x / TableLayout.kPixelShift;
		int top = r.y / TableLayout.kPixelShift;
		int right = (r.x + r.width) / TableLayout.kPixelShift;
		int bottom = (r.y + r.height) / TableLayout.kPixelShift;
		r.setBounds(left, top, (right - left), (bottom - top));
	}
	
	public void scrollToSelection() {
		Rectangle minRect = theTable.findTableRectangle(theTable.minSelectedRowCol());
		Rectangle maxRect = theTable.findTableRectangle(theTable.maxSelectedRowCol());
		
		Rectangle unionRect = (minRect == null) ? maxRect
									: (maxRect == null) ? minRect
									: minRect.union(maxRect);
//		System.out.println("minRect = " + minRect + ", maxRect = " + maxRect + ", unionRect = " + unionRect);
		if (unionRect != null) {
			scaleRectForScroll(unionRect);
//			System.out.println("scaled unionRect = " + unionRect);
//			System.out.println("vScroll.value = " + vScroll.getValue() + ", vScroll.visible = " + vScroll.getVisibleAmount());
			
			if (unionRect.y < vScroll.getValue() || unionRect.height > vScroll.getVisibleAmount())
				vScroll.setValue(unionRect.y);
			else if (unionRect.y + unionRect.height > vScroll.getValue() + vScroll.getVisibleAmount())
				vScroll.setValue(unionRect.y + unionRect.height - vScroll.getVisibleAmount());
			else
				vHeading.repaint();
			
			if (unionRect.x < hScroll.getValue() || unionRect.width > hScroll.getVisibleAmount())
				hScroll.setValue(unionRect.x);
			else if (unionRect.x + unionRect.width > hScroll.getValue() + hScroll.getVisibleAmount())
				hScroll.setValue(unionRect.x + unionRect.width - hScroll.getVisibleAmount());
			else
				hHeading.repaint();
		}
	}
	
	public double getRoundingError(NumValue lowLimit, NumValue highLimit, double eps) {
		double maxError = 0.0;
		if (lowLimit != null)
			maxError += theTable.getRoundingError(lowLimit.toDouble(), eps);
		if (highLimit != null)
			maxError += theTable.getRoundingError(highLimit.toDouble(), eps);
		return maxError;
	}
	
	public double inverseError(double prob) {
		return theTable.inverseError(prob);
	}
	
	public double[] correctZBounds(double prob, boolean interpolate, double slop) {
		return theTable.correctZBounds(prob, interpolate, slop);
	}
}