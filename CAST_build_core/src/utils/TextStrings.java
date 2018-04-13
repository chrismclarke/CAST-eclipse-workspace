package utils;

import java.util.*;

import dataView.*;


public class TextStrings {
	private Locale currentLocale = null;
	
	private ResourceBundle buttonBundle = null;
	private ResourceBundle termsBundle = null;
	private ResourceBundle graphicsBundle = null;
	
	private Hashtable termTable = null;
	
	public TextStrings(String language, XApplet applet) {
		if (language == null)
			currentLocale = new Locale("en", "UK", "");
		else {
			StringTokenizer st = new StringTokenizer(language);
			String lang = st.nextToken();
			String country = st.hasMoreTokens() ? st.nextToken() : "";
			currentLocale = new Locale(lang, country, "");
		}
		
		String customText = applet.getParameter(XApplet.CUSTOM_TEXT_PARAM);
		if (customText != null) {
			StringTokenizer st = new StringTokenizer(customText, "#");
			termTable = new Hashtable();
			while (st.hasMoreTokens()) {
				StringTokenizer st2 = new StringTokenizer(st.nextToken(), "=");
				termTable.put(st2.nextToken(), st2.nextToken());
			}
		}
	}
	
	public String translate(String key) {
		if (termTable != null) {
			String translatedString = (String)termTable.get(key);
			if (translatedString != null)
				return translatedString;
//				return "z" + translatedString.substring(1);
		}
		return getStringFromBundle(key);
//		return "z" + getStringFromBundle(key).substring(1);
	}
	
	public String getStringFromBundle(String key) {
		buttonBundle = createBundle("Buttons", buttonBundle);
		if (buttonBundle != null) {
			String s = getStringFromBundle(key, buttonBundle);
			if (s != null)
				return s;
		}
		
		termsBundle = createBundle("Terms", termsBundle);
		if (termsBundle != null) {
			String s = getStringFromBundle(key, termsBundle);
			if (s != null)
				return s;
		}
		
		graphicsBundle = createBundle("Graphics", graphicsBundle);
		if (graphicsBundle != null) {
			String s = getStringFromBundle(key, graphicsBundle);
			if (s != null)
				return s;
		}
		
		System.out.println("Error: cannot find term \"" + key + "\" in resource bundles");
		throw new RuntimeException("Error: cannot find term \"" + key + "\" in resource bundles");
		
//		return key;
	}
	
	private ResourceBundle createBundle(String bundleName, ResourceBundle bundle) {
		if (bundle != null)
			return bundle;
		else
			try {
//				return ResourceBundle.getBundle("textBundles/" + bundleName, currentLocale);
				return Utf8ResourceBundle.getBundle("textBundles/" + bundleName, currentLocale);
			} catch (MissingResourceException e) {
				System.out.println("Error: cannot create ResourceBundle \"" + bundleName + "\"");
				return null;
			}
	}
	
	private String replaceBlanks(String key) {
		StringBuffer sb = new StringBuffer(key);
		for (int i=0 ; i<sb.length() ; i++)
			if (sb.charAt(i) == ' ')
				sb.setCharAt(i, '#');
		
		return sb.toString();
	}
	
	private String getStringFromBundle(String key, ResourceBundle bundle) {
		key = replaceBlanks(key);
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return null;
		}
	}
}