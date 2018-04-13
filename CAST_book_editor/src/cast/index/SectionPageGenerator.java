package cast.index;

import java.io.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

import cast.bookManager.*;
import cast.utils.*;


public class SectionPageGenerator {
	private CastEbook castEbook;
	
	public SectionPageGenerator(CastEbook castEbook) {
		this.castEbook = castEbook;
	}
	
	public void processSection(String dir, String filePrefix) {
		File xmlFile = castEbook.getXmlFile(dir, filePrefix);
		if (!xmlFile.exists())
			return;
		
		String charset = "UTF-8";
		
		File outputFile = castEbook.getSectionJsFile(dir, filePrefix);
		try {
			FileOutputStream fos = new FileOutputStream(outputFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, charset);
			BufferedWriter bw = new BufferedWriter(osw);
			PrintWriter outputWriter = new PrintWriter(bw);
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(true);
			try {
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document dom = db.parse(castEbook.getXmlFile(dir, filePrefix));
				
				Element sectionElement = dom.getDocumentElement();
				
				printHtmlStart(sectionElement, outputWriter, charset);
				
				NodeList nl = sectionElement.getElementsByTagName("page");
				for (int i=0 ; i<nl.getLength() ; i++) {
					Element pageElement = (Element)nl.item(i);
					printPage(pageElement, i+1, outputWriter);
				}
				
				printHtmlEnd(outputWriter);

			} catch(Exception e) {
				System.err.println("Error in section (" + dir + "," + filePrefix + "): " + e);
				e.printStackTrace();
			}

			outputWriter.flush();
			outputWriter.close();
			
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}
	
	private void printHtmlStart(Element sectionElement, PrintWriter outputWriter, String charset) {
		String sectionName = sectionElement.getAttribute("name");
		String shortSectionName = sectionElement.getAttribute("shortName");
		if (shortSectionName == null || shortSectionName.length() == 0)
			shortSectionName = sectionName;
		
		outputWriter.println("var sectionInnerHtml = \"<img class='sectionStar' src='structure/images/star.svg' width='33' height='33'>\\n\";");
		outputWriter.println("sectionInnerHtml += \"<img class='sectionUnderscore' src='structure/images/sectionUnderscore.png' width='443' height='4'>\\n\";");
//		outputWriter.println("var sectionInnerHtml = \"<img class='sectionStar' src='structure/images/star.gif' width='31' height='31'>\\n\";");
//		outputWriter.println("sectionInnerHtml += \"<img class='sectionUnderscore' src='structure/images/sectionUnderscore.gif' width='443' height='4'>\\n\";");
		outputWriter.println("sectionInnerHtml += \"<p class='sectionHeading'>" + sectionName + "</p>\\n\\n\";\n");
		
		String topText = XmlHelper.getUniqueTagAsString(sectionElement, "topText");
		if (topText != null) {
			topText = stripSpaces(topText);
			topText = topText.replaceAll("\n", " ");
			topText = topText.replaceAll("\"", "&#0022;");
			outputWriter.println("sectionInnerHtml += \"<div class='section_note'>\\n" + topText + "\\n</div>\\n\\n\";");
		}
		
		outputWriter.println("sectionInnerHtml += \"<dl>\\n\";\n");
	}
	
	private void printHtmlEnd(PrintWriter outputWriter) {
		outputWriter.println("sectionInnerHtml += \"</dl>\\n\";");
		
		outputWriter.println("var endString = getSectionEndString();");
		outputWriter.println("sectionInnerHtml += endString;\n");
		
		
		outputWriter.println("var sectionDiv = document.getElementById(\"section\");");
		outputWriter.println("sectionDiv.innerHTML = sectionInnerHtml;");
		outputWriter.println("sectionDiv.style.display = \"block\";\n");
		
		outputWriter.println("var pageIframe = document.getElementById(\"content\");");
		outputWriter.println("pageIframe.style.display = \"none\";");
	}
	
	private void printPage(Element pageElement, int index, PrintWriter outputWriter) {
		String dir = pageElement.getAttribute("dir");
		String filePrefix = pageElement.getAttribute("filePrefix");
		
		String pageName = pageElement.getAttribute("nameOverride");
		if (pageName == null || pageName.length() == 0) {
			File page = castEbook.getPageHtmlFile(dir, filePrefix);
			String fileString = HtmlHelper.getFileAsString(page);
			pageName = HtmlHelper.getTagInFile(fileString, "title");
		}
		
//		String pageClass = "contents";
		int redIndex = pageName.indexOf("#r#");
		if (redIndex == 0) {		//		if it starts with #r#, it should be drawn in red (an exercise)
//			pageClass += " exercise";
			pageName = pageName.substring(3);
		}
		
//		int optionalIndex = pageName.indexOf("#?#");
//		if (optionalIndex > 0)		//		ignore everything after #?#
//			pageName = pageName.substring(0, optionalIndex);
		
		int changeColorIndex = pageName.indexOf("#+#");
		if (changeColorIndex > 0)		//		delete #+# since pageNames in section contents do not change colour
			pageName = pageName.substring(0, changeColorIndex) + pageName.substring(changeColorIndex + 3);
		
		String note = pageElement.getAttribute("note");
		String noteHtml = "";
		if (note != null && note.length() > 0 && note.charAt(0) != '#') {
			int hashIndex = note.indexOf('#');				//	part after '#' only appears in banner
			if (hashIndex >= 0)
				note = note.substring(0, hashIndex);
			noteHtml = "&nbsp;&nbsp;&nbsp;<span class='note'>" + note + "</span>";
		}
		
		String description = XmlHelper.getTagInterior(pageElement);
		
		outputWriter.println("sectionInnerHtml += \"  <dt class='contents'><a href=\\\"javascript:showNamedPage('" + filePrefix + "')\\\">" + index + ". " + pageName + "</a>" + noteHtml + "</dt>\\n\";");
//		outputWriter.println("if (!hasSummaries || !showingSummary)");
		outputWriter.println("  sectionInnerHtml += \"  <dd class='full_text'>" + description + "</dd>\\n\";\n");
	}
	
	private String stripSpaces(String s) {
		int c0 = 0;
		while (c0 < s.length() && isWhiteSpace(s.charAt(c0)))
			c0 ++;
		
		int c1 = s.length() - 1;
		while (c1 > 0 && isWhiteSpace(s.charAt(c1)))
			c1 --;
		
		if (c1 < c0)
			return null;
		else
			return s.substring(c0, c1 + 1);
	}
	
	private boolean isWhiteSpace(char c) {
		return c == ' ' || c == '\n' || c == '\r' || c == '\t';
	}
}
