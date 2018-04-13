package cast.pageEditor;

import java.awt.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;

import cast.utils.*;


public class HtmlContainerElement extends CoreHtmlElement {
	static final private Color kDiagramColor = new Color(0xFFCC99);
	
	private String remainingHtml;
	
	public HtmlContainerElement(String htmlString, JPanel parent, boolean isSystemAdviceFile) {
		remainingHtml = htmlString;
		
		Pattern thePattern = Pattern.compile("(.*<title>)([^>]*)(</title>.*)", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher theMatcher = thePattern.matcher(remainingHtml);
		if (theMatcher.find()) {
			insertElement(new HtmlStringElement(theMatcher.group(1)));
			insertElement(new HtmlTitleElement(theMatcher.group(2), parent, "Title:"));
			
			remainingHtml = theMatcher.group(3);
		}
		
		thePattern = Pattern.compile("(^.*addChapterHeading\\(')([^']*)('\\).*$)", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		theMatcher = thePattern.matcher(remainingHtml);
		if (theMatcher.find()) {
			insertElement(new HtmlStringElement(theMatcher.group(1)));
			insertElement(new HtmlTitleElement(theMatcher.group(2), parent, "Heading:"));
			
			remainingHtml = theMatcher.group(3);
		}
		
		while (true) {
			int firstDivIndex = remainingHtml.indexOf("<div");
			if (firstDivIndex < 0) {
				addSimpleElements(remainingHtml, parent, isSystemAdviceFile);
				break;
			}
			
			int endOfDivTag = remainingHtml.indexOf(">", firstDivIndex) + 1;
			String divTag = remainingHtml.substring(firstDivIndex, endOfDivTag);
			
			String initialHtml = remainingHtml.substring(0, endOfDivTag);
			addSimpleElements(initialHtml, parent, isSystemAdviceFile);
			remainingHtml = remainingHtml.substring(endOfDivTag);
			
			int endDivIndex = findNestedEndIndex(remainingHtml, "div");		//	imediately after the </div>
			String divString = remainingHtml.substring(0, endDivIndex);		//	includes the </div> but not the <div> tag
			
			JPanel divPanel = new JPanel();
			divPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 5));
			if (divTag.contains("boxed")) {
				Border blackline = BorderFactory.createLineBorder(Color.black);
				Border spacingBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
				divPanel.setBorder(BorderFactory.createCompoundBorder(blackline, spacingBorder));
				divPanel.setBackground(Color.white);
			}
			else if (divTag.contains("diagram")) {
				Border blackline = BorderFactory.createLineBorder(Color.black);
				Border spacingBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
				divPanel.setBorder(BorderFactory.createCompoundBorder(blackline, spacingBorder));
				divPanel.setBackground(kDiagramColor);
			}
			else {
				if (isSystemAdviceFile) {
					thePattern = Pattern.compile("class=['\"]([^'\"]*)['\"]", Pattern.CASE_INSENSITIVE);
					theMatcher = thePattern.matcher(divTag);
					String divName = null;
					if (theMatcher.find())
						divName = theMatcher.group(1);
					else {
						thePattern = Pattern.compile("id=['\"]([^'\"]*)['\"]", Pattern.CASE_INSENSITIVE);
						theMatcher = thePattern.matcher(divTag);
						if (theMatcher.find())
							divName = theMatcher.group(1);
					}
					if (divName != null) {		//		this a class or id for some OS or browser, so show its name
						Border blackline = BorderFactory.createLineBorder(Color.black);
						TitledBorder title = BorderFactory.createTitledBorder(blackline, divName);
						title.setTitleJustification(TitledBorder.LEFT);
						divPanel.setBorder(title);
					}
				}
				divPanel.setOpaque(false);
			}
			
			HtmlContainerElement divElement = new HtmlContainerElement(divString, divPanel, isSystemAdviceFile);
			parent.add(divPanel);
			insertElement(divElement);
			
			remainingHtml = remainingHtml.substring(endDivIndex);
		}
	}
	
	private void addSimpleElements(String htmlString, JPanel parent, boolean isSystemAdviceFile) {
		while (true) {
			int firstPIndex = minHitPos(htmlString.indexOf("<p "), htmlString.indexOf("<p>"));
																														//	so it does not catch <param> tags
			int firstUlIndex = htmlString.indexOf("<ul");
			int firstOlIndex = htmlString.indexOf("<ol");
			int firstDlIndex = htmlString.indexOf("<dl");
			int firstAppletIndex = htmlString.indexOf("<applet");
			int firstTableIndex = htmlString.indexOf("<table");
			
			int firstHit = minHitPos(firstPIndex, firstUlIndex);
			firstHit = minHitPos(firstHit, firstOlIndex);
			firstHit = minHitPos(firstHit, firstDlIndex);
			firstHit = minHitPos(firstHit, firstAppletIndex);
			firstHit = minHitPos(firstHit, firstTableIndex);
			
			if (firstHit < 0) {		//	only undisplayable HTML code
				insertElement(new HtmlStringElement(htmlString));
				break;
			}
			else if (firstHit == firstTableIndex) {
				int tagEndIndex = htmlString.indexOf(">", firstTableIndex) + 1;			//	just past the <table> tag
				insertElement(new HtmlStringElement(htmlString.substring(0, tagEndIndex)));
				htmlString = htmlString.substring(tagEndIndex);
				
				int tableEndIndex = findNestedEndIndex(htmlString, "table");
				tableEndIndex -= 8;																				//	back to the start of the </table> tag
				insertElement(new HtmlTableElement(htmlString.substring(0, tableEndIndex), parent));
				htmlString = htmlString.substring(tableEndIndex);
			}
			else {
				int tagEndIndex = htmlString.indexOf(">", firstHit) + 1;
				String fullTag = htmlString.substring(firstHit, tagEndIndex);
				insertElement(new HtmlStringElement(htmlString.substring(0, tagEndIndex)));
				htmlString = htmlString.substring(tagEndIndex);
				int endIndex = tagEndIndex;
				
				if (firstHit == firstPIndex) {
					endIndex = htmlString.indexOf("</p>");
					String content = htmlString.substring(0, endIndex);
					if (fullTag.contains("heading"))
						insertElement(new HtmlHeadingElement(content, parent));
					else if (content.matches("^\\s*<img[^>]*>\\s*<iframe[^>]*>\\s*</iframe>\\s*<script[^>]*>[^<]*</script>\\s*$")
											|| content.matches("^\\s*<object[^>]*>\\s*</object>\\s*$")
											|| content.matches("^\\s*<iframe[^>]*>\\s*</iframe>\\s*$")
											|| content.matches("^\\s*<img[^>]*>\\s*$"))
						insertElement(new HtmlImageElement(content, parent));
					else {
						JPanel container = parent;
						if (isSystemAdviceFile) {
							Pattern thePattern = Pattern.compile("class=['\"]([^'\"]*)['\"]", Pattern.CASE_INSENSITIVE);
							Matcher theMatcher = thePattern.matcher(fullTag);
							String paraName = null;
							if (theMatcher.find())
								paraName = theMatcher.group(1);
							else {
								thePattern = Pattern.compile("id=['\"]([^'\"]*)['\"]", Pattern.CASE_INSENSITIVE);
								theMatcher = thePattern.matcher(fullTag);
								if (theMatcher.find())
									paraName = theMatcher.group(1);
							}
							if (paraName != null) {		//		this a class or id for some OS or browser, so show its name
								container = new JPanel();
								container.setLayout(new BorderLayout(0, 0));
								Border blackline = BorderFactory.createLineBorder(Color.black);
								TitledBorder title = BorderFactory.createTitledBorder(blackline, paraName);
								title.setTitleJustification(TitledBorder.LEFT);
								container.setBorder(title);
								parent.add(container);			//	assumes that it is a VerticalLayout
							}
						}
						insertElement(new HtmlParaElement(content, container));
					}
				}
				else if (firstHit == firstUlIndex) {
					endIndex = htmlString.indexOf("</ul>");
					String content = htmlString.substring(0, endIndex);
					insertElement(new HtmlListElement(content, parent, false));
				}
				else if (firstHit == firstOlIndex) {
					endIndex = htmlString.indexOf("</ol>");
					String content = htmlString.substring(0, endIndex);
					insertElement(new HtmlListElement(content, parent, true));
				}
				else if (firstHit == firstDlIndex) {
					endIndex = htmlString.indexOf("</dl>");
					String content = htmlString.substring(0, endIndex);
					insertElement(new HtmlHeadingListElement(content, parent, true));
				}
				else if (firstHit == firstAppletIndex) {
					endIndex = htmlString.indexOf("</applet>");
					String content = htmlString.substring(0, endIndex);
					insertElement(new HtmlAppletElement(content, parent));
				}
				
				htmlString = htmlString.substring(endIndex);
			}
		}
	}
}
