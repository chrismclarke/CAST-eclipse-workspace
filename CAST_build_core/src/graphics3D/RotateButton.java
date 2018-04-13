package graphics3D;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import dataView.*;
import utils.*;

public class RotateButton extends XCoreButton {
	static final public int YX_ROTATE = 0;
	static final public int YZ_ROTATE = 1;
	static final public int XZ_ROTATE = 2;
	static final public int XYZ_ROTATE = 3;
	static final public int YXD_ROTATE = 4;
	static final public int YX2_ROTATE = 5;
	static final public int DY_ROTATE = 6;
	static final public int DX_ROTATE = 7;
	
	static final public int QUAD_YX_ROTATE = 8;		//	for quadratic where z=x and x=x2
	static final public int QUAD_XYX2_ROTATE = 9;
	static final public int QUAD_XX2_ROTATE = 10;
	
	static final public int XZ_BLANK_ROTATE = 11;
	static final public int XYZ_BLANK_ROTATE = 12;
	static final public int YZ_BLANK_ROTATE = 13;
	
	static final public int HORIZONTAL = 0;
	static final public int VERTICAL = 1;
	
	static public XPanel createRotationPanel(Rotate3DView theView, XApplet applet,
																	int orientation) {
		XPanel thePanel = new XPanel();
		if (orientation == HORIZONTAL)
			thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		else
			thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		thePanel.add(new RotateButton(RotateButton.YX_ROTATE, theView, applet));
		thePanel.add(new RotateButton(RotateButton.YZ_ROTATE, theView, applet));
		thePanel.add(new RotateButton(RotateButton.XZ_ROTATE, theView, applet));
		thePanel.add(new RotateButton(RotateButton.XYZ_ROTATE, theView, applet));
		
		return thePanel;
	}
	
	static public XPanel createRotationPanel(Rotate3DView theView, XApplet applet) {
		return createRotationPanel(theView, applet, HORIZONTAL);
	}
	
	static public XPanel create2DRotationPanel(Rotate3DView theView, XApplet applet,
																	int orientation) {
		XPanel thePanel = new XPanel();
		if (orientation == HORIZONTAL)
			thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		else
			thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		thePanel.add(new RotateButton(RotateButton.YX2_ROTATE, theView, applet));
		thePanel.add(new RotateButton(RotateButton.YXD_ROTATE, theView, applet));
		
		return thePanel;
	}
	
	static public XPanel create2DYRotationPanel(Rotate3DView theView, XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.add(new RotateButton(RotateButton.DY_ROTATE, theView, applet));
		thePanel.add(new RotateButton(RotateButton.YXD_ROTATE, theView, applet));
		
		return thePanel;
	}
	
	static public XPanel createXYDRotationPanel(Rotate3DView theView, XApplet applet,
																	int orientation) {
		XPanel thePanel = new XPanel();
		if (orientation == HORIZONTAL)
			thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		else
			thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		thePanel.add(new RotateButton(RotateButton.DY_ROTATE, theView, applet));
		thePanel.add(new RotateButton(RotateButton.DX_ROTATE, theView, applet));
		thePanel.add(new RotateButton(RotateButton.YXD_ROTATE, theView, applet));
		
		return thePanel;
	}
	
	static public XPanel createXYDRotationPanel(Rotate3DView theView, XApplet applet) {
		return createXYDRotationPanel(theView, applet, HORIZONTAL);
	}
	
	static public XPanel createQuadXYRotationPanel(Rotate3DView theView, XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		
		thePanel.add(new RotateButton(RotateButton.QUAD_YX_ROTATE, theView, applet));
		thePanel.add(new RotateButton(RotateButton.QUAD_XX2_ROTATE, theView, applet));
		thePanel.add(new RotateButton(RotateButton.QUAD_XYX2_ROTATE, theView, applet));
		
		return thePanel;
	}
	
	private int rotateType;
	private Rotate3DView view;
	
	public RotateButton(int rotateType, Rotate3DView view, XApplet applet) {
		super(applet);
		this.view = view;
		this.rotateType = rotateType;
		
		swingButton = createSwingButton(rotateType);
		add(swingButton);
	}
	
	protected JButton createSwingButton(int rotateType) {
		JButton theButton = new JButton(new RotateIcon(rotateType, RotateIcon.STANDARD));
		theButton.setSelectedIcon(new RotateIcon(rotateType, RotateIcon.BOLD));
		theButton.setDisabledIcon(new RotateIcon(rotateType, RotateIcon.DIM));
		theButton.setOpaque(transparentButtons);
									//	only opaque with white background if OS would show background
		theButton.setFocusPainted(false);
		theButton.addActionListener(this);
		return theButton;
	}
	
	public void actionPerformed(ActionEvent e) {
		view.stopAutoRotation();
		switch (rotateType) {
			case YX_ROTATE:
			case DY_ROTATE:
//				view.rotateTo(0, 0);
				view.animateRotateTo(0, 0);
				break;
			case YZ_ROTATE:
			case DX_ROTATE:
			case QUAD_YX_ROTATE:
			case YZ_BLANK_ROTATE:
//				view.rotateTo(90, 0);
				view.animateRotateTo(90, 0);
				break;
			case XZ_ROTATE:
			case YX2_ROTATE:
			case QUAD_XX2_ROTATE:
			case XZ_BLANK_ROTATE:
//				view.rotateTo(90, 90);
				view.animateRotateTo(90, 90);
				break;
			case XYZ_ROTATE:
			case YXD_ROTATE:
			case QUAD_XYX2_ROTATE:
			case XYZ_BLANK_ROTATE:
//				view.rotateTo(30, 30);
				view.animateRotateTo(30, 30);
				break;
		}
	}
}