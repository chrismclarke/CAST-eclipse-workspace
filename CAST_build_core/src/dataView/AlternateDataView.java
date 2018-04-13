package dataView;

import java.awt.*;

public class AlternateDataView extends DataView {
									//	Allows DataViews to be added in a CardLayout
	private CardLayout theCardLayout;
	
	public AlternateDataView(DataSet theData, XApplet applet, Insets insets) {
		super(theData, applet, insets);			//	all DataViews must use same Insets
		
		theCardLayout = new CardLayout();
		setLayout(theCardLayout);
	}
	
	public void showView(String viewName) {
		theCardLayout.show(this, viewName);
	}
	
	public void addDataView(DataView theView, String viewName) {
		theView.setInCardLayout(true);
		add(viewName, theView);
	}
	
	
	public void paintView(Graphics g) {
	}
	
//--------------------------------------------------------

													//		the sub-DataView's get their own doChangeVariable() calls
	
	protected void doChangeVariable(Graphics g, String key) {
	}
	
	protected void doChangeSelection(Graphics g) {
	}
	
	protected void doChangeValue(Graphics g, int index) {
	}
	
	protected void doAddValues(Graphics g, int noOfValues) {
	}
	
//--------------------------------------------------------

	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}