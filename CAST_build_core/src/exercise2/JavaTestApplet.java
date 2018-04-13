package exercise2;

import java.awt.*;

import dataView.*;


public class JavaTestApplet extends XApplet {
	
	public void setupApplet() {
		setBackground(new Color(0x0066FF));
		
		setLayout(new BorderLayout(0, 0));
		
		JavaTestCanvas greenRect = new JavaTestCanvas(this);
		greenRect.setFont(getStandardBoldFont());
		add("Center", greenRect);
	}
}