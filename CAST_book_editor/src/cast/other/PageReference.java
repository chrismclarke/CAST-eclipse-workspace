package cast.other;

import java.io.*;
import java.util.regex.*;


import cast.utils.*;


public class PageReference {
	static final private String kAppletPattern = "<applet(.*?)</applet>";
	static final private String kVarNamePattern = "[\\S\\s]*?name=\"varName\"[\\S\\s]*name=\"values\"[\\S\\s]*";
	static final private String kVarNamePattern2 = "[\\S\\s]*?name=\"([^\"]*V)arName\"[\\S\\s]*name=\"\\1alues\"[\\S\\s]*";		//		Same prefix for VarName and Values params
	static final private String kVarNamePattern3 = "[\\S\\s]*?name=\"([^\"]*)Name\"[\\S\\s]*name=\"\\1Values\"[\\S\\s]*";		//		Same prefix for Name and Values params
	
	
	private String pageName, dir, filePrefix;
	private String[] appletString;

	public PageReference(String pageName, String dir, String filePrefix, File coreDir) {
		this.pageName = pageName;
		this.dir = dir;
		this.filePrefix = filePrefix;

		File htmlDir = new File(coreDir, dir);
		File htmlFile = new File(htmlDir, filePrefix + ".html");
		String htmlString = HtmlHelper.getFileAsString(htmlFile);

		Pattern appletPattern = Pattern.compile(kAppletPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher appletMatcher = appletPattern.matcher(htmlString);
		int nApplets = 0;
		while (appletMatcher.find()) {
			if (appletHasData(appletMatcher.group()))
				nApplets ++;
		}

		if (nApplets > 0) {
			appletString = new String[nApplets];
			appletMatcher = appletPattern.matcher(htmlString);
			nApplets = 0;
			while (appletMatcher.find()) {
				String theApplet = appletMatcher.group();		//		complete applet string
				if (appletHasData(theApplet))
					appletString[nApplets ++] = theApplet;
			}
		}
	}

	public boolean appletHasData(String theApplet) {
		boolean hasData = theApplet.matches(kVarNamePattern) || theApplet.matches(kVarNamePattern2) || theApplet.matches(kVarNamePattern3);
		return hasData;
	}

	public boolean matches(String dir, String filePrefix) {
		return dir.equals(this.dir) && filePrefix.equals(this.filePrefix);
	}

	public int noOfApplets() {
		return (appletString == null) ? 0 : appletString.length;
	}
	
	public String getPageName() {
		return pageName;
	}
	
	public String getDir() {
		return dir;
	}
	
	public String getFilePrefix() {
		return filePrefix;
	}
	
	public String getApplet(int index) {
		return appletString[index];
	}
}
