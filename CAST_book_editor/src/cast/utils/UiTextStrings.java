package cast.utils;

import java.util.*;
import java.io.*;
import java.net.*;


public class UiTextStrings {
	private File coreDir;
	private Locale currentLocale = null;
	
	private ResourceBundle uiBundle = null;
	
	public UiTextStrings(String language, File coreDir) {
		this.coreDir = coreDir;
		if (language == null)
			currentLocale = new Locale("en", "UK", "");
		else {
			StringTokenizer st = new StringTokenizer(language);
			String lang = st.nextToken();
			String country = st.hasMoreTokens() ? st.nextToken() : "";
			currentLocale = new Locale(lang, country, "");
		}
	}
	
	public String translate(String key) {
		uiBundle = createBundle("StartTerms", uiBundle);
		if (uiBundle != null) {
			String s = getStringFromBundle(key, uiBundle);
			if (s != null)
				return s;
		}
		
//		System.out.println("Error: cannot find term \"" + key + "\" in resource bundles");
//		throw new RuntimeException("Error: cannot find term \"" + key + "\" in resource bundle");
		
		return key;
	}
	
	private ResourceBundle createBundle(String bundleName, ResourceBundle bundle) {
		if (bundle != null)
			return bundle;
		else
			try {
				File bundleDir = new File(coreDir, "java/uiBundles");
				URL[] urls = {bundleDir.toURI().toURL()};
				ClassLoader loader = new URLClassLoader(urls);
				
				return Utf8ResourceBundle.getBundle(bundleName, currentLocale, loader);
			} catch (MissingResourceException e) {
				System.out.println("Error: cannot create ResourceBundle \"" + bundleName + "\"");
				return null;
			} catch (MalformedURLException e) {
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