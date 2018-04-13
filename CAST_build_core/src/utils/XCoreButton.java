package utils;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import dataView.*;


abstract public class XCoreButton extends XPanel implements ActionListener {
	
		static protected boolean transparentButtons = false;
//	static protected boolean transparentButtons = XApplet.osType == XApplet.OS_XP
//																									|| XApplet.osType == XApplet.OS_OTHER;
							//		on Windows XP, buttons are transparent and background shows through
							//		on Vista, icon buttons (at least) don't seem to need white background
	
	protected JButton swingButton;
	
	private boolean initialised = false;
	
	public XCoreButton(XApplet applet) {
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
	}
	
	abstract public void actionPerformed(ActionEvent e);
	
	public boolean isEnabled() {
		return swingButton.isEnabled();
	}
	
	public void enable() {
		swingButton.setEnabled(true);
	}
	
	public void disable() {
		swingButton.setEnabled(false);
	}
	
	private void initialiseBackgrounds() {
		if (!initialised) {
//			Color bg = getParent().getBackground();
//			lockBackground(bg);
			if (transparentButtons)			//	only opaque with white background if OS would show background
				swingButton.setBackground(Color.white);
			
			initialised = true;
		}
	}
	
	protected void paintComponent(Graphics g) {
		initialiseBackgrounds();
		super.paintComponent(g);
	}
	
	protected void paintChildren(Graphics g) {
		initialiseBackgrounds();
		super.paintChildren(g);
	}
}