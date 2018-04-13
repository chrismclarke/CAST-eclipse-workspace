package utils;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import dataView.*;

public class XButton extends XCoreButton {
//	private JButton theButton;
	
	public XButton(String label, XApplet applet) {
		super(applet);
		swingButton = createSwingButton(label, applet);
		add(swingButton);
	}
	
	protected JButton createSwingButton(String label, XApplet applet) {
		int secondLineIndex = label.indexOf("\n");
		if (secondLineIndex >= 0)
			label = "<html><center>" + label.substring(0, secondLineIndex) + "<br>"
									+ label.substring(secondLineIndex + 1, label.length()) + "</center></html>";
		JButton theButton = new JButton(label);
		theButton.setOpaque(transparentButtons);
									//	only opaque with white background if OS would show background
		theButton.setFocusPainted(false);
		theButton.addActionListener(this);
		theButton.setFont(applet.getStandardBoldFont());
		return theButton;
	}
	
	@SuppressWarnings("deprecation")
	public void actionPerformed(ActionEvent e) {
//		postEvent(new Event(this, Event.ACTION_EVENT, swingButton.getText()));
		int modifiers = e.getModifiers();
		postEvent(new Event(this, 0, Event.ACTION_EVENT, 0, 0, 0, modifiers, swingButton.getText()));
	}
	
	public void setText(String s) {
		swingButton.setText(s);
	}
}