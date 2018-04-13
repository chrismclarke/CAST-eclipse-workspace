package dataView;

import java.awt.*;

import utils.*;

public class MultipleDataView extends DataView {
									//	Allows several DataViews to be added to AxisLayout (with a line between)
	static final public int HORIZONTAL = EqualSpacingLayout.HORIZONTAL;
	static final public int VERTICAL = EqualSpacingLayout.VERTICAL;
	
	static final private DataView[] dataViewArray(DataView view1, DataView view2) {
		DataView[] views = {view1, view2};
		return views;
	}
	
	static final private DataView[] dataViewArray(DataView view1, DataView view2, DataView view3) {
		DataView[] views = {view1, view2, view3};
		return views;
	}
	
	private int orientation;
	
	public MultipleDataView(DataSet theData, XApplet applet, DataView topView,
																																	DataView bottomView) {
		this(theData, applet, topView, bottomView, VERTICAL);
	}
	
	public MultipleDataView(DataSet theData, XApplet applet, DataView topView,
																								DataView middleView, DataView bottomView) {
		this(theData, applet, topView, middleView, bottomView, VERTICAL);
	}
	
	public MultipleDataView(DataSet theData, XApplet applet, DataView[] views) {
		this(theData, applet, views, VERTICAL);
	}

	public MultipleDataView(DataSet theData, XApplet applet, DataView topView,
																									DataView bottomView, int orientation) {
		this(theData, applet, dataViewArray(topView, bottomView), orientation);
	}

	public MultipleDataView(DataSet theData, XApplet applet, DataView topView,
															DataView middleView, DataView bottomView, int orientation) {
		this(theData, applet, dataViewArray(topView, middleView, bottomView), orientation);
	}

/*
	public MultipleDataView(DataSet theData, XApplet applet, DataView topView,
																									DataView bottomView, int orientation) {
		super(theData, applet, null);
		
		this.orientation = orientation;
		setLayout(new EqualSpacingLayout(orientation, 1));
		
		add(topView);
		add(bottomView);
		
		Insets topBorder = topView.getViewBorder();
		Insets bottomBorder = bottomView.getViewBorder();
		if (orientation == VERTICAL)
			setViewBorder(new Insets(topBorder.top, Math.max(topBorder.left, bottomBorder.left),
												bottomBorder.bottom, Math.max(topBorder.right, bottomBorder.right)));
		else
			setViewBorder(new Insets(Math.max(topBorder.top, bottomBorder.top), topBorder.left,
												Math.max(topBorder.bottom, bottomBorder.bottom), bottomBorder.right));
	}
	
	public MultipleDataView(DataSet theData, XApplet applet, DataView topView,
															DataView middleView, DataView bottomView, int orientation) {
		super(theData, applet, null);
		
		this.orientation = orientation;
		setLayout(new EqualSpacingLayout(orientation, 1));
		
		add(topView);
		add(middleView);
		add(bottomView);
		
		Insets topBorder = topView.getViewBorder();
		Insets middleBorder = middleView.getViewBorder();
		Insets bottomBorder = bottomView.getViewBorder();
		if (orientation == VERTICAL)
			setViewBorder(new Insets(topBorder.top, Math.max(topBorder.left, Math.max(middleBorder.left, bottomBorder.left)),
											bottomBorder.bottom, Math.max(topBorder.right, Math.max(middleBorder.right, bottomBorder.right))));
		else
			setViewBorder(new Insets(Math.max(topBorder.top, Math.max(middleBorder.top, bottomBorder.top)), topBorder.left,
											Math.max(topBorder.bottom, Math.max(middleBorder.bottom, bottomBorder.bottom)), bottomBorder.right));
	}
*/
	
	public MultipleDataView(DataSet theData, XApplet applet, DataView[] views, int orientation) {
		super(theData, applet, null);
		
		this.orientation = orientation;
		setLayout(new EqualSpacingLayout(orientation, 1));
		
		for (int i=0 ; i<views.length ; i++)
			add(views[i]);
		
		Insets firstBorder = views[0].getViewBorder();
		Insets lastBorder = views[views.length - 1].getViewBorder();
		if (orientation == VERTICAL) {
			int leftBorder = 0;
			int rightBorder = 0;
			for (int i=0 ; i<views.length ; i++) {
				leftBorder = Math.max(leftBorder, views[i].getViewBorder().left);
				rightBorder = Math.max(rightBorder, views[i].getViewBorder().right);
			}
			setViewBorder(new Insets(firstBorder.top, leftBorder, lastBorder.bottom, rightBorder));
		}
		else {
			int topBorder = 0;
			int bottomBorder = 0;
			for (int i=0 ; i<views.length ; i++) {
				topBorder = Math.max(topBorder, views[i].getViewBorder().top);
				bottomBorder = Math.max(bottomBorder, views[i].getViewBorder().bottom);
			}
			setViewBorder(new Insets(topBorder, firstBorder.left, bottomBorder, lastBorder.right));
		}
	}
	
	public void paintView(Graphics g) {
		Component components[] = getComponents();
		for (int i=0 ; i<components.length - 1 ; i++)
			if (orientation == VERTICAL) {
				int bottom = components[i].getY() + components[i].getHeight();
				g.drawLine(0, bottom, getSize().width, bottom);
			}
			else {
				int right = components[i].getX() + components[i].getWidth();
				g.drawLine(right, 0, right, getSize().height);
			}
	}
	
//--------------------------------------------------------

													//		the two sub-DataView's get their own doChangeVariable() calls
	
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