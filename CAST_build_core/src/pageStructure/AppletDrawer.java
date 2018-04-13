package pageStructure;

import java.awt.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.text.html.*;

import dataView.*;


public class AppletDrawer extends CoreDrawer {
	static final private String kAppletParamPattern = "<param\\s+name=\"(.*?)\"\\s+value=\"(.*?)\">";
	
	private Map namedApplets;
	private HashMap<String,String> parameterMap;
	private int appletWidth;
	
	public AppletDrawer(String htmlString, StyleSheet theStyleSheet, Map namedApplets) {
																					//	the htmlString only contains <param> tags
		this.namedApplets = namedApplets;
		parameterMap = new HashMap();
		
		Pattern appletParamPattern = Pattern.compile(kAppletParamPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher appletParamMatcher = appletParamPattern.matcher(htmlString);
		while (appletParamMatcher.find()) {
			String paramName = appletParamMatcher.group(1);
			String paramValue = appletParamMatcher.group(2);
			if (!paramName.equals("backgroundColor"))
				parameterMap.put(paramName, paramValue);
		}
		String backgroundColorString = getBackgroundColorString(theStyleSheet);
		parameterMap.put("backgroundColor", backgroundColorString);
	}
	
	
	public JPanel createPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		thePanel.setOpaque(false);
		
			XApplet applet = null;
			String appletName = parameterMap.get("appletName");
			String widthString = parameterMap.get("width");
			String heightString = parameterMap.get("height");
			String referenceNameString = parameterMap.get("name");
			String fontSizeNameString = parameterMap.get("fontSize");
			try {
				Class appletClass = Class.forName(appletName);
//				try {
					applet = (XApplet)appletClass.getDeclaredConstructor().newInstance();
//				} catch (Exception e) {
//				}
				applet.setParameters(parameterMap);
				
				int w = Integer.parseInt(widthString);
				int h = Integer.parseInt(heightString);
				if (fontSizeNameString == null || !fontSizeNameString.equals("big"))
					if (w != scaledSize(w)) {
						w = scaledSize(w);
						h = scaledSize(h);
						parameterMap.put("fontSize", "big");
					}
				
				applet.setPreferredSize(new Dimension(w, h));
				applet.setMinimumSize(new Dimension(w, h));
				applet.setMaximumSize(new Dimension(w, h));
				appletWidth = w;
				
				if (namedApplets != null) {
					if (referenceNameString != null)
						namedApplets.put(referenceNameString, applet);
					
					applet.setNamedApplets(namedApplets);		//	to allow them to refer to other applets on page
				}
				
				applet.init();
			} catch (InstantiationException e) {
				System.out.println(e);
			} catch (ReflectiveOperationException e) {
				System.out.println(e);
//			} catch (IllegalAccessException e) {
//				System.out.println(e);
//			} catch (ClassNotFoundException e) {
//				System.out.println(e);
			} catch (NumberFormatException e) {
			}
		
		thePanel.add(applet);
		return thePanel;
	}
	
	
	public int getMinimumWidth() {
		return appletWidth;
	}
}
