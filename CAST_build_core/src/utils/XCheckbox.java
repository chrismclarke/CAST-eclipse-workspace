package utils;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import dataView.*;


public class XCheckbox extends XPanel implements ActionListener, StatusInterface {
	private JCheckBox swingCheck;
	
//	private boolean initialised = false;
	
	public XCheckbox(String label, XApplet applet) {
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
//		setOpaque(false);
			swingCheck = new JCheckBox(label);
			swingCheck.setOpaque(false);
			swingCheck.setFocusPainted(false);
			swingCheck.addActionListener(this);
			swingCheck.setFont(applet.getStandardBoldFont());
		add(swingCheck);
	}
	
	@SuppressWarnings("deprecation")
	public void actionPerformed(ActionEvent e) {
		postEvent(new Event(this, Event.ACTION_EVENT, swingCheck.getText()));
	}
	
	public boolean getState() {
		return swingCheck.isSelected();
	}
	
	public void setState(boolean checked) {
		swingCheck.setSelected(checked);
	}
	
	public String getStatus() {
		return getState() ? "true" : "false";
	}
	
	public void setStatus(String status) {
		setState(status.equals("true"));
	}
	
	public boolean isEnabled() {
		return swingCheck.isEnabled();
	}
	
	public void enable() {
		swingCheck.setEnabled(true);
	}
	
	public void disable() {
		swingCheck.setEnabled(false);
	}
	
	public void setEnabled(boolean enabled) {
		if (enabled)
			enable();
		else
			disable();
	}
	
	public void setFont(Font f) {
		super.setFont(f);
		if (swingCheck != null)
			swingCheck.setFont(f);
	}
	
/*
	protected void paintComponent(Graphics g) {
		if (!initialised) {
			Color bg = getParent().getBackground();
			lockBackground(bg);
			initialised = true;
		}
		super.paintComponent(g);
	}
*/
}