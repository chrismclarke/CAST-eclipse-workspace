package valueList;

import java.awt.*;
//import javax.swing.*;

import dataView.*;
import axis.*;


public class DummyDataView extends DataView {
//	static final private String DUMMY_DATA_VIEW = "DummyDataView";
	
	private ValueView valueView;
	
	public DummyDataView(DataSet theData, XApplet applet, ValueView valueView) {
		super(theData, applet, null);
		this.valueView = valueView;
	}
	
	public void paintView(Graphics g) {
	}
	
	public void repaint() {		//	acts as a proxy for the valueView -- is passed to DragValAxis
														//	instead of the ValueView itself
		if (valueView != null)
			valueView.redrawAll();
	}

//-----------------------------------------------------------------------------------
												//	makes methods public so that ValueView can access them
	public CoreVariable getVariable(String key) {
		return super.getVariable(key);
	}
	
	public NumVariable getNumVariable() {
		return super.getNumVariable();
	}
	
	public CatVariable getCatVariable() {
		return super.getCatVariable();
	}
	
	public Flags getSelection() {
		return super.getSelection();
	}
	
	public DataSet getData() {
		return super.getData();
	}

//-----------------------------------------------------------------------------------

	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		return null;
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeSelection(Graphics g) {
		valueView.redrawValue();
	}
		
	protected void doChangeValue(Graphics g, int index) {
		valueView.redrawValue();
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		valueView.redrawAll();
	}
	
	protected void doTransformView(Graphics g, NumCatAxis theAxis) {
		valueView.redrawValue();
	}
	
	protected void doAddValues(Graphics g, int noOfValues) {
		valueView.redrawValue();
	}
}