package pageStructure;

import java.awt.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.text.html.*;

import utils.*;
import ebook.*;


public class HtmlDrawer extends CoreDrawer {
	static final private String kDivPattern = "(.*?)<div\\s+class=\"(diagram|definition|theoremProof"
										+ "|theorem|proof|questionSoln|question|solution|explanation|example|boxed.*?"
										+ "|imageChoice|video|videoButton|divChoice|browser_only|latex|applet"
										+ "|lectureLink|lecturerNote|centredImage|html5)\".*?>(.*)";
	static final private String kBadStartPattern = "^\\s*</div>(.*)";
	static final private String kBadEndPattern = "(.*)<div class=\"centred\">\\s*$";
	
	private Color backgroundColor;
	
	public HtmlDrawer(String htmlString, String dirString, StyleSheet theStyleSheet,
																													BookFrame theBookFrame, Map namedApplets) {
		backgroundColor = getBackgroundColor(theStyleSheet);
		
		String restOfString = htmlString;
		
		while (true) {
			Pattern diagramPattern = Pattern.compile(kDivPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher diagramMatcher = diagramPattern.matcher(restOfString);
			if (!diagramMatcher.find())
				break;
			
			String startString = diagramMatcher.group(1);
			String divType = diagramMatcher.group(2);
			restOfString = diagramMatcher.group(3);
			
			Pattern badEndPattern = Pattern.compile(kBadEndPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
																			// gets rid of <div class="centred"> before a boxed div
			Matcher badEndMatcher = badEndPattern.matcher(startString);
			if (badEndMatcher.find())
				startString = badEndMatcher.group(1);
			
			startString = startString.replaceFirst("^\\s*", "");
			if (startString.length() > 0) {
//				System.out.println("Adding startOfString: " + restOfString);
				addChild(new SimpleHtmlDrawer(startString, dirString, theStyleSheet, theBookFrame));
			}
			
			
			int level = 0;
			int currentIndex = 0;
			boolean foundEndDiv = false;
			while (!foundEndDiv) {
				int nextStartDivIndex = restOfString.indexOf("<div", currentIndex);
				int nextEndDivIndex = restOfString.indexOf("</div>", currentIndex);
				if (nextStartDivIndex >= 0 && nextStartDivIndex < nextEndDivIndex) {
					level ++;
					currentIndex = nextStartDivIndex + 4;
				}
				else {
					if (level > 0) {
						level --;
						currentIndex = nextEndDivIndex + 6;
					}
					else {
						String divString = restOfString.substring(0, nextEndDivIndex);
						restOfString = restOfString.substring(nextEndDivIndex + 6);
						if (divType.contains("boxed"))
							addChild(new BoxedDrawer(divString, dirString, theBookFrame, namedApplets));
						else if (divType.equals("diagram"))
							addChild(new DiagramDrawer(divString, dirString, theBookFrame, namedApplets));
						else if (divType.equals("imageChoice"))
							addChild(new ImageChoiceDrawer(divString, dirString, theBookFrame));
						
						else if (divType.equals("video"))
							addChild(new VideoDrawer(divString, theBookFrame));			//		uses JavaFX WebView to render video
//								addChild(new VideoDrawer_2(divString, theBookFrame));		//		uses JavaFX MediaPlayer to render video
						else if (divType.equals("videoButton"))
							addChild(new VideoButtonDrawer(divString, theBookFrame));
						
						else if (divType.equals("definition"))
							addChild(new HeadedBlockDrawer(divString, dirString, theBookFrame, HeadedBlockDrawer.DEFINITION, namedApplets));
						else if (divType.equals("example"))
							addChild(new HeadedBlockDrawer(divString, dirString, theBookFrame, HeadedBlockDrawer.EXAMPLE, namedApplets));
						
						else if (divType.equals("theoremProof"))
							addChild(new ExpandingBlockDrawer(divString, dirString, theStyleSheet, theBookFrame, ExpandingBlockDrawer.THEOREM, namedApplets));
						else if (divType.equals("theorem"))
							addChild(new TheoremQuestionDrawer(divString, dirString, theBookFrame, ExpandingBlockDrawer.THEOREM, namedApplets));
						else if (divType.equals("proof"))
							addChild(new ProofSolutionDrawer(divString, dirString, theBookFrame, ExpandingBlockDrawer.THEOREM, namedApplets));
						
						
						else if (divType.equals("questionSoln"))
							addChild(new ExpandingBlockDrawer(divString, dirString, theStyleSheet, theBookFrame, ExpandingBlockDrawer.EXERCISE, namedApplets));
						else if (divType.equals("question"))
							addChild(new TheoremQuestionDrawer(divString, dirString, theBookFrame, ExpandingBlockDrawer.EXERCISE, namedApplets));
						else if (divType.equals("solution"))
							addChild(new ProofSolutionDrawer(divString, dirString, theBookFrame, ExpandingBlockDrawer.EXERCISE, namedApplets));
						
						else if (divType.equals("latex"))
							addChild(new LatexDrawer(divString));
							
						else if (divType.equals("centredImage"))
							addChild(new CentredImageDrawer(divString, dirString, theStyleSheet, theBookFrame));
							
						else if (divType.equals("applet"))
							addChild(new AppletDrawer(divString, theStyleSheet, namedApplets));
							
						else if (divType.equals("lectureLink"))
							addChild(new LectureLinkDrawer());
						else if (divType.equals("lecturerNote"))
							addChild(new LectureNoteDrawer(divString, dirString, theBookFrame, namedApplets));
							
						else if (divType.equals("divChoice"))
							addChild(new DivChoiceDrawer(divString, dirString, theStyleSheet, theBookFrame, namedApplets));
						else if (divType.equals("browser_only"))		//	This div should not be shown in stand-alone version
							;
						
						else if (divType.equals("html5"))
							addChild(new Html5Drawer(divString, theBookFrame));
						foundEndDiv = true;
					}
				}
			}
			
			Pattern badStartPattern = Pattern.compile(kBadStartPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
																			// gets rid of <\div> from centred div surrounding a boxed div
			Matcher badStartMatcher = badStartPattern.matcher(restOfString);
			if (badStartMatcher.find())
				restOfString = badStartMatcher.group(1);
		}
			
		restOfString = restOfString.replaceFirst("^\\s*", "");
		if (restOfString.length() > 0) {
//			System.out.println("Adding restOfString: " + restOfString);
			addChild(new SimpleHtmlDrawer(restOfString, dirString, theStyleSheet, theBookFrame));
		}
		
		int nChildren = noOfChildren();
		for (int i=0; i<nChildren ; i++)
			if (getChild(i) instanceof LectureLinkDrawer) {
				LectureLinkDrawer linkDrawer = (LectureLinkDrawer)getChild(i);
				if (i == nChildren - 2 && getChild(nChildren - 1) instanceof HtmlDrawer) {
					CoreDrawer lectureNote = (HtmlDrawer)getChild(nChildren - 1);
					removeChild(lectureNote);
					linkDrawer.setNotes(lectureNote, null);
				}
				else if (i == nChildren - 3 && getChild(nChildren - 2) instanceof HtmlDrawer
																					&& getChild(nChildren - 1) instanceof HtmlDrawer) {
					CoreDrawer lectureNote = (HtmlDrawer)getChild(nChildren - 2);
					CoreDrawer dataNote = (HtmlDrawer)getChild(nChildren - 1);
					removeChild(lectureNote);
					removeChild(dataNote);
					linkDrawer.setNotes(lectureNote, dataNote);
				}
				break;
			}
	}
	
	
	public JPanel createPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
//		thePanel.setOpaque(false);
		thePanel.setBackground(backgroundColor);
		
		int nChildren = noOfChildren();
		for (int i=0 ; i<nChildren ; i++)
			thePanel.add(getChild(i).createPanel());
		
		return thePanel;
	}
}
