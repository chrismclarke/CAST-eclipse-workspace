package pageStructure;

import java.util.*;
import java.util.regex.*;
import java.io.*;

import javax.swing.*;

import utils.*;
import ebook.*;
import ebookStructure.*;


public class PageDrawer extends CoreDrawer {
	static final private String kTitlePattern = "addChapterHeading\\('(.*?)'\\).*?</script>(.*)<script.*?writePageEnd\\(\\)";
	static final private String kModulePattern = "addModuleHeading\\('(.*?)'\\).*?</script>(.*)<script.*?writePageEnd\\(\\)";
	static final private String kBodyPattern = "writePageStart\\(\\).*?</script>(.*)<script.*?writePageEnd\\(\\)";
	static final private String kBodyPattern2 = "(.*)<script.*?writePageEnd\\(\\)";
	static final private String kImageChoiceSetupPattern = "setupChoiceImages\\((.*?)\\);";
	
	static final private String kImgChoicePattern = "(.*?)<form>.*?<select.*?(<option.*?)</select>.*?</form>.*?"
																+ "<p[^>]*>\\s*<img([^>]*)>\\s*</p>"
																+ "(.*)";
	static final private String kImgPrefixPattern = "\"([^\"]*)\"";
	static final private String kImgNamePattern = "<option[^>]*>(.*?)</option>";
	static final private String kWidthPattern = "width=\"(\\d*)\"";
	static final private String kHeightPattern = "height=\"(\\d*)\"";
	
	static final private String kVideoPattern = "(.*)<script[^>]*>writeVideo\\('(.*?)',\\s*'(.*?)',\\s*'(.*?)',\\s*'(.*?)',\\s*'(.*?)'\\);?\\s*</script>(.*)";
	
	static final private String kVideoButtonPattern = "(.*)<script[^>]*>writeVideoButton\\('(.*?)'\\);\\s*</script>(.*)";
	
	static final private String kExerciseTopicPattern = "exercises.js.*/exercises/([^\"]*).js\"";
	static final private String kExercisePattern = "(.*)<script.*?outputExercise\\(\"([^\"]*)\",\\s*(null|\"[^\"]*\")[\\s,]*(null|\"[^\"]*\").*?</script>(.*)";
		
	static final private String kDisplayLatexPattern = "(.*)\\\\\\[\\s*(.*?)\\s*\\\\\\](.*)";
	
	static final private String kCentredImagePattern = "(.*?)<p\\s*(class=\"?eqn\"?|align=\"center\")>\\s*<img\\s*(.*?)>\\s*</p>(.*)";
	static final private String kCentredImage2Pattern = "(.*?)<div\\s*class=\"centred\">\\s*<img\\s*(.*?)>\\s*</div>(.*)";
	
	static final private String kAppletPattern = ".*<applet\\s+([^>]+)>(.*?)</applet>.*";
	static final private String kAppletWidthPattern = "width=\"(\\d*)\"";
	static final private String kAppletHeightPattern = "height=\"(\\d*)\"";
	static final private String kReferenceNamePattern = "name=\"(.*?)\"";
	
	static final private String kLectureLinkPattern = "(?s)<script type=\"text/javascript\">\\s*writeLecturerNotesLink.*?</script>";
//	static final private String kDataNotePattern = "<div class=\"lecturerNote\" id=\"dataNote\">";
	
	static public String getFileEncoding(File f, String charsetPattern) {
		String encoding = "UTF-8";			//	default and used for "xxx.properties" files
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String s;
			Pattern encodingPattern = Pattern.compile(charsetPattern, Pattern.CASE_INSENSITIVE);
			while ((s = reader.readLine()) != null) {
				Matcher encodingMatcher = encodingPattern.matcher(s);
				if (encodingMatcher.find()) {
					encoding = encodingMatcher.group(1).toUpperCase();
					break;
				}
			}
			reader.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
		return encoding;
	}
	
	static public String getFileAsString(File f) {
		String charset = getFileEncoding(f, "<meta .* charset=([^\"']*)[\"']\\s*/?>");
		return getFileAsString(f, charset);
	}
	
	static public String getFileAsString(File f, String charset) {
		try {
			if (charset != null) {
				FileInputStream fis = new FileInputStream(f);
				InputStreamReader isr = new InputStreamReader(fis, charset);
				
				BufferedReader reader = new BufferedReader(isr);
					
				StringBuffer sb = new StringBuffer();
				String s;
				while ((s = reader.readLine()) != null) {
					sb.append(s);
					sb.append("\n");
				}
				
				reader.close();
				
				return sb.toString();
			}
		} catch (IOException e) {
			System.err.println(e.toString());
		}
		return null;
	}
	
	
//----------------------------------------------------------------
	
	private String chapterTitleString=null, moduleTitleString=null, bodyString;
	
	private Map namedApplets = new HashMap();
	
	public PageDrawer(DomElement theElement, BookFrame theBookFrame) {
		int effectiveVersion = theBookFrame.getEffectivePageVersion();
		File f = theElement.getFile(effectiveVersion);
		String html = getFileAsString(f);
		String dirString = theElement.getDirStrings()[effectiveVersion];
		
		Pattern titlePattern = Pattern.compile(kTitlePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher titleMatcher = titlePattern.matcher(html);
		if (titleMatcher.find()) {
			chapterTitleString = titleMatcher.group(1);
			bodyString = titleMatcher.group(2);
		}
		else {
			Pattern modulePattern = Pattern.compile(kModulePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher moduleMatcher = modulePattern.matcher(html);
			if (moduleMatcher.find()) {
				moduleTitleString = moduleMatcher.group(1);
				bodyString = moduleMatcher.group(2);
			}
			else {
				Pattern bodyPattern = Pattern.compile(kBodyPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
				Matcher bodyMatcher = bodyPattern.matcher(html);
				if (bodyMatcher.find()) {		//	for ordinary pages starting with writePageStart()
//					chapterTitleString = null;
					bodyString = bodyMatcher.group(1);
				}
				else {
					Pattern bodyPattern2 = Pattern.compile(kBodyPattern2, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
					Matcher bodyMatcher2 = bodyPattern2.matcher(html);
					if (bodyMatcher2.find()) {		//	for pages without writePageStart() such as initial splash
//						chapterTitleString = null;
						bodyString = bodyMatcher2.group(1);
					}
				}
			}
		}
		
		File coreDir = theBookFrame.getEbook().getCoreDir();
		if (chapterTitleString != null)
			addChild(new TitleDrawer(chapterTitleString, true, coreDir));
		else if (moduleTitleString != null)
			addChild(new TitleDrawer(moduleTitleString, false, coreDir));
		
		Map codeBlocks = new HashMap<String, String>();
		bodyString = extractTables(bodyString, codeBlocks);
			
		bodyString = replaceExercises(html, bodyString, theBookFrame);
		bodyString = extractApplets(bodyString, codeBlocks);
		
		bodyString = replaceDiagramChoice(html, bodyString);
		bodyString = replaceVideos(bodyString);
		bodyString = replaceVideoButton(bodyString);
		bodyString = replaceDisplayLatex(bodyString);
		bodyString = replaceCentredImages(bodyString);
		bodyString = replaceLectureLink(bodyString);
		bodyString = bodyString.replaceAll(" notPrinted", "");
		
		bodyString = returnCodeBlocks(bodyString, codeBlocks);
		
		addChild(new HtmlDrawer(bodyString, dirString, theBookFrame.getPageStyleSheet(), theBookFrame, namedApplets));
//		System.out.println(bodyString);
		
		addChild(new NextButtonDrawer(theElement, theBookFrame));
	}
	
	private String extractTables(String bodyHtml, Map codeBlocks) {
		String outputString = "";
		String remainingString = bodyHtml;
		int nextIndex = 0;
		while (true) {
			int tableIndex = remainingString.indexOf("<table");
			if (tableIndex < 0) {
				outputString += remainingString;
				break;
			}
			outputString += remainingString.substring(0, tableIndex);
			remainingString = remainingString.substring(tableIndex);
			
			int currentIndex = 6;
			int level = 1;
			while (level > 0) {
				int nextEndIndex = remainingString.indexOf("</table>", currentIndex);
				int nextStartIndex = remainingString.indexOf("<table", currentIndex);
				if (nextStartIndex > 0 && nextStartIndex < nextEndIndex) {
					level ++;
					currentIndex = nextStartIndex + 6;
				}
				else {
					level --;
					currentIndex = nextEndIndex + 8;
				}
			}
			String tableString = remainingString.substring(0, currentIndex);
			String key = "<tableBlock " + nextIndex + ">";
			codeBlocks.put(key, tableString);
			outputString += key;
			remainingString = remainingString.substring(currentIndex);
			nextIndex ++;
		}
//		System.out.println(outputString + "\n\n*******************\n");
		return outputString;
	}
	
	private String extractApplets(String bodyHtml, Map codeBlocks) {
		String outputString = "";
		String remainingString = bodyHtml;
		int nextIndex = 0;
		while (true) {
			int tableIndex = remainingString.indexOf("<applet");
			if (tableIndex < 0) {
				outputString += remainingString;
				break;
			}
			outputString += remainingString.substring(0, tableIndex);
			remainingString = remainingString.substring(tableIndex);
			
			int endIndex = remainingString.indexOf("</applet>");
			
			String appletString = remainingString.substring(0, endIndex + 9);
			String key = "<appletBlock " + nextIndex + ">";
			codeBlocks.put(key, modifyApplet(appletString));
			outputString += key;
			remainingString = remainingString.substring(endIndex + 9);
			nextIndex ++;
		}
//		System.out.println(outputString + "\n\n*******************\n");
		return outputString;
	}
	
	private String modifyApplet(String appletString) {
		Pattern appletPattern = Pattern.compile(kAppletPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher appletMatcher = appletPattern.matcher(appletString);
		if (appletMatcher.find()) {		//	should always match
			String appletArguments = appletMatcher.group(1);
			String appletParams = appletMatcher.group(2);
			
				Pattern appletWidthPattern = Pattern.compile(kAppletWidthPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
				Matcher appletWidthMatcher = appletWidthPattern.matcher(appletArguments);
				appletWidthMatcher.find();
				String appletWidth = appletWidthMatcher.group(1);
			appletParams += "<param name=\"width\" value=\"" + appletWidth + "\">\n";
			
				Pattern appletHeightPattern = Pattern.compile(kAppletHeightPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
				Matcher appletHeightMatcher = appletHeightPattern.matcher(appletArguments);
				appletHeightMatcher.find();
				String appletHeight = appletHeightMatcher.group(1);
			appletParams += "<param name=\"height\" value=\"" + appletHeight + "\">\n";
			
				Pattern referenceNamePattern = Pattern.compile(kReferenceNamePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
				Matcher referenceNameMatcher = referenceNamePattern.matcher(appletArguments);
			if (referenceNameMatcher.find())
				appletParams += "<param name=\"name\" value=\"" + referenceNameMatcher.group(1) + "\">\n";
			
			return "<div class=\"applet\">\n" + appletParams + "</div>";
		}
		return null;
	}
	
	private String returnCodeBlocks(String bodyHtml, Map codeBlocks) {
    Iterator it = codeBlocks.entrySet().iterator();
    while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			String key = (String)pair.getKey();
			String code = (String)pair.getValue();
			int codeIndex = bodyHtml.indexOf(key);
			bodyHtml = bodyHtml.substring(0, codeIndex) + code + bodyHtml.substring(codeIndex + key.length());
		}
		return bodyHtml;
	}
	
	private String replaceDiagramChoice(String theHtml, String bodyHtml) {
		Pattern imgChoiceSetupPattern = Pattern.compile(kImageChoiceSetupPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Pattern imgPrefixPattern = Pattern.compile(kImgPrefixPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		
		Pattern imgChoicePattern = Pattern.compile(kImgChoicePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Pattern imgNamePattern = Pattern.compile(kImgNamePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Pattern imgWidthPattern = Pattern.compile(kWidthPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Pattern imgHeightPattern = Pattern.compile(kHeightPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		
		Matcher imgChoiceSetupMatcher = imgChoiceSetupPattern.matcher(theHtml);
		Matcher imgChoiceMatcher = imgChoicePattern.matcher(bodyHtml);
		boolean foundSetup = imgChoiceSetupMatcher.find();
		boolean foundForm = imgChoiceMatcher.find();
		if (foundSetup && foundForm) {
			String imgPrefixeString = imgChoiceSetupMatcher.group(1);
			Matcher imgPrefixMatcher = imgPrefixPattern.matcher(imgPrefixeString);
			Vector imgPrefixes = new Vector();
			while (imgPrefixMatcher.find()) {
				String prefix = imgPrefixMatcher.group(1);
				imgPrefixes.add(prefix);
			}
			
			String startString = imgChoiceMatcher.group(1);			// up to <form>
			String choiceOptionsString = imgChoiceMatcher.group(2);
			String imgContentString = imgChoiceMatcher.group(3);
			String endString = imgChoiceMatcher.group(4);	// from <img> paragraph to end
			
			Matcher imgNamesMatcher = imgNamePattern.matcher(choiceOptionsString);
			Vector imgNames = new Vector();
			while (imgNamesMatcher.find()) {
				String name = imgNamesMatcher.group(1);
				imgNames.add(name);
			}
			
			Matcher imgWidthMatcher = imgWidthPattern.matcher(imgContentString);
			String imgWidth = "";
			if (imgWidthMatcher.find())
				imgWidth = imgWidthMatcher.group(1);
			
			Matcher imgHeightMatcher = imgHeightPattern.matcher(imgContentString);
			String imgHeight = "";
			if (imgHeightMatcher.find())
				imgHeight = imgHeightMatcher.group(1);
			
			int nImages = imgPrefixes.size();
			if (nImages == 0 || nImages != imgNames.size())
				System.out.println("Bad image choices in file");
			else {
				String choiceDiv = "<div class=\"imageChoice\"><imageChoice width=\"" + imgWidth
											+ "\" height=\"" + imgHeight+ "\" nImages=\"" + nImages + "\">";
				for (int i=0 ; i<nImages ; i++)
					choiceDiv += "#" + (String)imgPrefixes.get(i) + "#" + (String)imgNames.get(i) + "# ";
				choiceDiv += "</imageChoice></div>\n";
//				System.out.println(choiceDiv);
				
				return startString + choiceDiv + endString;
			}
		}
		
		return bodyHtml;
	}
	
	private String replaceVideos(String bodyHtml) {
		Pattern videoPattern = Pattern.compile(kVideoPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher videoMatcher = videoPattern.matcher(bodyHtml);
		if (videoMatcher.find()) {
			String startString = videoMatcher.group(1);
			String bookString = videoMatcher.group(2);
			String sectionString = videoMatcher.group(3);
			String pageString = videoMatcher.group(4);
			String widthString = videoMatcher.group(5);
			String heightString = videoMatcher.group(6);
			String endString = videoMatcher.group(7);
		
			String videoDiv = "<div class=\"video\"><castVideo book=\"" + bookString + "\" "
																+ "section=\"" + sectionString + "\" "
																+ "videoName=\"" + pageString + "\" "
																+ "width=\"" + widthString + "\" "
																+ "height=\"" + heightString + "\""
																+ "></div>";
//			System.out.println("Found video in HTML file:\n" + videoDiv);
			return startString + videoDiv + endString;
		}
		else
			return bodyHtml;
	}
	
	private String replaceVideoButton(String bodyHtml) {
		Pattern videoButtonPattern = Pattern.compile(kVideoButtonPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher videoButtonMatcher = videoButtonPattern.matcher(bodyHtml);
		if (videoButtonMatcher.find()) {
			String startString = videoButtonMatcher.group(1);
			String buttonName = videoButtonMatcher.group(2);
			String endString = videoButtonMatcher.group(3);
		
			String videoButtonDiv = "<div class=\"videoButton\">" + buttonName + "</div>";
//			System.out.println("Found video in HTML file:\n" + videoDiv);
			return startString + videoButtonDiv + endString;
		}
		else
			return bodyHtml;
	}
	
	private String replaceDisplayLatex(String bodyHtml) {
		bodyHtml = bodyHtml.replaceAll("\\[\\d\\.\\dem\\]", "");		//	since jlatexmath does not support [0.4em] line spacing, etc
		Pattern displayLatexPattern = Pattern.compile(kDisplayLatexPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		while (true) {
			Matcher displayLatexMatcher = displayLatexPattern.matcher(bodyHtml);
			if (displayLatexMatcher.find()) {
				String startString = displayLatexMatcher.group(1);
				String latex = displayLatexMatcher.group(2);
				String endString = displayLatexMatcher.group(3);
			
				String latexDiv = "<div class=\"latex\">" + latex + "</div>";
				bodyHtml = startString + latexDiv + endString;
			}
			else
				break;
		}
//		System.out.println(bodyHtml);
		return bodyHtml;
	}
	
	private String replaceCentredImages(String bodyHtml) {
		Pattern centredImagePattern = Pattern.compile(kCentredImagePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		while (true) {
			Matcher centredImageMatcher = centredImagePattern.matcher(bodyHtml);
			if (centredImageMatcher.find()) {
				String startString = centredImageMatcher.group(1);
				String imageParams = centredImageMatcher.group(3);
				String endString = centredImageMatcher.group(4);
			
				String imageDiv = "<div class=\"centredImage\">" + imageParams + "</div>";
				bodyHtml = startString + imageDiv + endString;
			}
			else
				break;
		}
		Pattern centredImage2Pattern = Pattern.compile(kCentredImage2Pattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		while (true) {
			Matcher centredImage2Matcher = centredImage2Pattern.matcher(bodyHtml);
			if (centredImage2Matcher.find()) {
				String startString = centredImage2Matcher.group(1);
				String imageParams = centredImage2Matcher.group(2);
				String endString = centredImage2Matcher.group(3);
			
				String imageDiv = "<div class=\"centredImage\">" + imageParams + "</div>";
				bodyHtml = startString + imageDiv + endString;
			}
			else
				break;
		}
//		System.out.println(bodyHtml);
		return bodyHtml;
	}
	
	private String replaceExercises(String allHtml, String bodyHtml, BookFrame theBookFrame) {
		Pattern topicPattern = Pattern.compile(kExerciseTopicPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher topicMatcher = topicPattern.matcher(allHtml);
		if (topicMatcher.find()) {
			String topicName = topicMatcher.group(1);
			
			while (true) {
				Pattern exercisePattern = Pattern.compile(kExercisePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
				Matcher exerciseMatcher = exercisePattern.matcher(bodyHtml);
				if (exerciseMatcher.find()) {
					String startString = exerciseMatcher.group(1);
					String appletName = exerciseMatcher.group(2);
					String variations = exerciseMatcher.group(3);
					if (variations.equals("null"))
						variations = null;
					else
						variations = variations.replaceAll("\"", "");

					String options = exerciseMatcher.group(4);
					if (options.equals("null"))
						options = null;
					else
						options = options.replaceAll("\"", "");
					String endString = exerciseMatcher.group(5);
					
					DomExercise theExercise = new DomExercise(theBookFrame.getEbook(), topicName,
																											appletName, variations, options);
					String appletString = theExercise.getAppletString();
					bodyHtml = startString + "\n" + appletString + endString;
//					System.out.println("Variations: " + variations);
//					System.out.println("Options: " + options);
//					System.out.println(appletString);
				}
				else
					break;
			}
		}
		return bodyHtml;
	}
	
	private String replaceLectureLink(String bodyHtml) {
		String result = bodyHtml.replaceAll(kLectureLinkPattern, "<div class=\"lectureLink\">xxx</div>");
		return result;
	}
	
	
//----------------------------------------------------------------------
	
	
	public JPanel createPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setOpaque(false);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		
		for (int i=0 ; i<noOfChildren() ; i++)
			thePanel.add(getChild(i).createPanel());
		
		return thePanel;
	}
}
