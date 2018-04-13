package pageStructure;

import java.awt.*;
import java.util.regex.*;
import java.net.*;

import javax.swing.*;
import javax.swing.text.html.*;
import javax.swing.event.*;

import ebook.*;


public class SimpleHtmlDrawer extends CoreDrawer {
//	static final private String kAppletPattern = "(.*?)<applet\\s+([^>]+)>(.*?)</applet>(.*)";
//	static final private String kAppletNamePattern = "code=\"(.*?).class\"";
//	static final private String kAppletWidthPattern = "width=\"(\\d*)\"";
//	static final private String kAppletHeightPattern = "height=\"(\\d*)\"";
//	static final private String kReferenceNamePattern = "name=\"(.*?)\"";
//	static final private String kAppletParamPattern = "<param\\s+name=\"(.*?)\"\\s+value=\"(.*?)\">";
	
	static final private String kImgPattern = "(<img.*?src=\")([^\"]*).(gif|png|jpeg)([^>]*>)";
//	static final private String kImgPngPattern = "(<img.*?src=\")([^\"]*).png([^>]*>)";
//	static final private String kImgJpegPattern = "(<img.*?src=\")([^\"]*).jpeg([^>]*>)";
	
	static final private String kFloatPattern = "<div style=\"float:.*?>(.*?)</div>";
	
//	static final private String kDisplayTexPattern = "\\s*\\\\\\[(.*?)\\s*\\\\\\]";
	static final private String kInlineTexPattern = "(\\\\\\(.*?\\s*\\\\\\))";
	
	static final private String kShowNamedPage = "showNamedPage";
	static final private String kOpenBook = "openEbook";
	
	
	static private void openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	static private void openWebpage(URL url) {
		try {
			openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	
	private String htmlString;
	private StyleSheet theStyleSheet;
	private BookFrame theBookFrame;
	
	public SimpleHtmlDrawer(String htmlString, String dirString, StyleSheet theStyleSheet,
																																			BookFrame theBookFrame) {
		this.theStyleSheet = theStyleSheet;
		this.theBookFrame = theBookFrame;
		this.htmlString = convertHtml(htmlString, dirString);
	}
	
	
	public JPanel createPanel() {
		Color bgColor = getBackgroundColor(theStyleSheet);
		Color fgColor = getForegroundColor(theStyleSheet);
		HTMLEditorKit htmlEditorKit = new AppletHTMLEditorKit(bgColor, fgColor);
		htmlEditorKit.setStyleSheet(theStyleSheet);
		
		JPanel thePanel = new JPanel() {
													public void paintComponent(Graphics g) {
														Graphics2D g2 = (Graphics2D)g;
														RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
																																						 RenderingHints.VALUE_ANTIALIAS_ON);
														g2.setRenderingHints(rh);
														super.paintComponent(g);
													}
												};
		thePanel.setLayout(new BorderLayout(0, 0));
		thePanel.setOpaque(false);
		
			JTextPane pageContent = new JTextPane();
			pageContent.setEditorKit(htmlEditorKit);
			pageContent.setOpaque(false);
			
			pageContent.setText("<html><head><title>CAST page</title></head><body>" + htmlString + "</body></html>");
			pageContent.setSelectionStart(0);
			pageContent.setSelectionEnd(0);
			pageContent.setEditable(false);
			pageContent.addHyperlinkListener(new HTMLListener());
		
		thePanel.add("Center", pageContent);
		
		return thePanel;
	}
	
 
	private class HTMLListener implements HyperlinkListener {
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				String theLink = e.getDescription();
				if (theLink.startsWith("http://"))
					openWebpage(e.getURL());
				else {
					int i1 = theLink.indexOf(kShowNamedPage);
					if (i1 >= 0) {
						String filePrefix = theLink.substring(i1 + kShowNamedPage.length());
						filePrefix = filePrefix.replaceAll("['\" \\(\\)]", "");
						theBookFrame.showNamedPage(filePrefix);
					}
					else {
						int i2 = theLink.indexOf(kOpenBook);
						if (i2 >= 0) {
							String bookName = theLink.substring(i2 + kOpenBook.length());
							bookName = bookName.replaceAll("['\" \\(\\)]", "");
							AppletProgram.openBook(bookName, theBookFrame.getEbook().getCoreDir(), null);
						}
					}

				}
			}
		}
	}
	
	
	
	private String convertHtml(String htmlString, String dirString) {
/*
		while (true) {
			Pattern appletPattern = Pattern.compile(kAppletPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher appletMatcher = appletPattern.matcher(htmlString);
			if (!appletMatcher.find())
				break;
			String startHtml = appletMatcher.group(1);
			String appletArguments = appletMatcher.group(2);
			String appletParams = appletMatcher.group(3);
			String endHtml = appletMatcher.group(4);
			
			htmlString = startHtml + replacementAppletTag(appletArguments, appletParams) + endHtml;
		}
*/
		
		Pattern imgPattern = Pattern.compile(kImgPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher imgMatcher = imgPattern.matcher(htmlString);
		htmlString = imgMatcher.replaceAll("$1" + coreDir + "/" + dirString + "/$2.$3$4");			//	to fix relative adresses of images since they must be relative to Java jar file not HTML file
		
//		htmlString = imgMatcher.replaceAll("$1../" + dirString + "/$2.svg$3");		//	it cannot render SVG images natively

		Pattern floatPattern = Pattern.compile(kFloatPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher floatMatcher = floatPattern.matcher(htmlString);
		htmlString = floatMatcher.replaceAll("<p class=\"eqn\">$1</p>");			//	since floating divs are not supported
		
		htmlString = htmlString.replaceAll("class=\"exampleHeading\"", "class=\"heading\"");		//	for headings inside "example" divs
		
//		Pattern displayTexPattern = Pattern.compile(kDisplayTexPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
//		Matcher displayTexMatcher = displayTexPattern.matcher(htmlString);
//		htmlString = displayTexMatcher.replaceAll("<div class='centred'><displaytex>$1</displaytex></div>");
		
		Pattern inlineTexPattern = Pattern.compile(kInlineTexPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher inlineTexMatcher = inlineTexPattern.matcher(htmlString);
//		htmlString = inlineTexMatcher.replaceAll("<inlinetex value=\"$1\">");
		htmlString = inlineTexMatcher.replaceAll("<img src=\"$1\">");
		
//		System.out.println(htmlString);

		htmlString = "<html><head><title>CAST page</title></head><body>" + htmlString + "</body></html>";
		return htmlString;
	}
	
/*
	private String replacementAppletTag(String appletArguments, String appletParams) {
			Pattern appletNamePattern = Pattern.compile(kAppletNamePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher appletNameMatcher = appletNamePattern.matcher(appletArguments);
			appletNameMatcher.find();
		@SuppressWarnings("unused")
		String appletName = appletNameMatcher.group(1);
		
			Pattern appletWidthPattern = Pattern.compile(kAppletWidthPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher appletWidthMatcher = appletWidthPattern.matcher(appletArguments);
			appletWidthMatcher.find();
		String appletWidth = appletWidthMatcher.group(1);
		
			Pattern appletHeightPattern = Pattern.compile(kAppletHeightPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher appletHeightMatcher = appletHeightPattern.matcher(appletArguments);
			appletHeightMatcher.find();
		String appletHeight = appletHeightMatcher.group(1);
		
//		String parameters = "appletName=\"" + appletName + "\" width=\"" + appletWidth + "\" height=\"" + appletHeight + "\"";
		String parameters = "width=\"" + appletWidth + "\" height=\"" + appletHeight + "\"";
		
			Pattern referenceNamePattern = Pattern.compile(kReferenceNamePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher referenceNameMatcher = referenceNamePattern.matcher(appletArguments);
		if (referenceNameMatcher.find())
			parameters += " name=\"" + referenceNameMatcher.group(1) + "\"";
		
			Pattern appletParamPattern = Pattern.compile(kAppletParamPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher appletParamMatcher = appletParamPattern.matcher(appletParams);
			while (appletParamMatcher.find()) {
				String paramName = appletParamMatcher.group(1);
				String paramValue = appletParamMatcher.group(2);
				if (!paramName.equals("backgroundColor"))
					parameters += " " + paramName + "=\"" + paramValue + "\"";
			}
		String backgroundColorString = getBackgroundColorString(theStyleSheet);
		String tag = "<castapplet>" + "backgroundColor=\"" + backgroundColorString + "\" " + parameters + "</castapplet>";
		return tag;
	}
*/
}
