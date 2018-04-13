package dataView;

import java.awt.*;
import javax.swing.*;

public class XPanel extends JPanel {
	protected boolean backgroundLocked = false;
	private XApplet applet = null;
	
	public XPanel() {
	}
	
	protected XApplet getApplet() {
		if (applet == null) {
			Container c = this;
			do {
				c = c.getParent();
			} while (!(c instanceof XApplet) && c != null);
			
			if (c instanceof XApplet)
				applet = (XApplet)c;
			else {
				c = this;
				System.out.println("Runtime error: XPanel is not contained in an XApplet.");
				System.out.println("Hierarchy:");
				while (c != null) {
					System.out.println("  " + c.getClass().getName());
					c = c.getParent();
				}
				throw new RuntimeException((String)null);
			}
		}
		return applet;
	}
	
	public void setBackground(Color c) {
		if (!backgroundLocked) {
			super.setBackground(c);
			for (int i=0 ; i<getComponentCount() ; i++) {
				Component comp = getComponent(i);
				if (comp instanceof XPanel)
					((XPanel)comp).setBackground(c);
			}
		}
	}
	
	public void lockBackground(Color c) {
		backgroundLocked = false;
		setBackground(c);
		backgroundLocked = true;
	}
	
	public Component add(Component comp) {
		super.add(comp);
		if (comp instanceof XPanel)
			((XPanel)comp).setBackground(getBackground());
		return comp;
	}
	
	public Component add(String name, Component comp) {
		super.add(name, comp);
		if (comp instanceof XPanel)
			((XPanel)comp).setBackground(getBackground());
		return comp;
	}
	
	public void add(Component comp, Object constraints) {
		super.add(comp, constraints);
		if (comp instanceof XPanel)
			((XPanel)comp).setBackground(getBackground());
	}
}