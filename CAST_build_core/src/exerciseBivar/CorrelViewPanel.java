package exerciseBivar;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import utils.*;

import corr.*;


public class CorrelViewPanel extends InsetPanel {
								//		CorrelationView must be packaged in heavyweight panel
								//		so it can be dragged on top of other components

	private CorrelationView rView;
	
	public CorrelViewPanel(DataSet data, String startXKey, String startYKey, XApplet applet) {
		super(2, 2);
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			rView = new CorrelationView(data, startXKey, startYKey, CorrelationView.NO_FORMULA, applet);
			rView.setEnabled(false);
		add(rView);
	}
	
	public void changeVariables(String yKey, String xKey) {
		rView.changeVariables(yKey, xKey);
	}
	
	public void addMouseListener(MouseListener l) {
		super.addMouseListener(l);
		rView.addMouseListener(l);
	}
	
	public void addMouseMotionListener(MouseMotionListener l) {
		super.addMouseMotionListener(l);
		rView.addMouseMotionListener(l);
	}
}