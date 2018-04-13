package dataView;

import java.applet.*;
import java.awt.*;


@SuppressWarnings("deprecation")
public class CastApplet extends Applet {
	private XApplet xApplet = null;
	
	public void init() {
		setLayout(new BorderLayout(0, 0));
		add("Center", createComponent());
	}
	
	private Component createComponent() {
		String appletName = getParameter("appletName");
		try {
			Class appletClass = Class.forName(appletName);
			xApplet = (XApplet)appletClass.newInstance();
			xApplet.setAppletParent(this);
			xApplet.init();
		} catch (InstantiationException e) {
			System.out.println(e);
		} catch (IllegalAccessException e) {
			System.out.println(e);
		} catch (ClassNotFoundException e) {
			System.out.println(e);
		} catch (NumberFormatException e) {
		}
		return xApplet;
	}
	
	public XApplet getXApplet() {
		return xApplet;
	}
}