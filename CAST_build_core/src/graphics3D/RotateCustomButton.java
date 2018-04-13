package graphics3D;

import java.awt.event.*;
import java.util.*;

import dataView.*;
import utils.*;

public class RotateCustomButton extends XButton {
	
	private double roundDensity, ofDensity;
	private Rotate3DView view;
	
	public RotateCustomButton(String label, double roundDensity, double ofDensity,
																				Rotate3DView view, XApplet applet) {
		super(label, applet);
		this.view = view;
		this.roundDensity = roundDensity;
		this.ofDensity = ofDensity;
	}
	
	public RotateCustomButton(String rotateParam, Rotate3DView view, XApplet applet) {
		super((new StringTokenizer(rotateParam, "#")).nextToken(), applet);
		this.view = view;
		StringTokenizer st = new StringTokenizer(rotateParam, "#");
		st.nextToken();
		this.roundDensity = Double.parseDouble(st.nextToken());
		this.ofDensity = Double.parseDouble(st.nextToken());
	}
	
	public void actionPerformed(ActionEvent e) {
		view.stopAutoRotation();
		view.animateRotateTo(roundDensity, ofDensity);
	}
}