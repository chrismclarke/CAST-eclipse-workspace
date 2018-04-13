package pageStructure;

import java.awt.*;
import java.util.*;
import java.net.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;


abstract public class CoreDrawer {
	
	static final private String kStyleSheetUrl = "castStyles.css";
	static final protected String kNormalBackgroundColorString = "FFFFFF";
	static final protected String kBoxedBackgroundColorString = "FFFFFF";
	static final protected String kDiagramBackgroundColorString = "F1F1F1";
	static final protected String kDefinitionBackgroundColorString = "FFFFFF";
	static final protected String kExampleBackgroundColorString = "FFFBF3";
	
	static final protected String kLectureNoteBackgroundColorString = "F9F9FF";
	static final protected String kLecturingBackgroundColorString = "F0F0F0";
	
	static final protected String kTheoremBackgroundColorString = "EBF6FB";
	static final protected String kProofBackgroundColorString = "FFFFFF";
//	static final protected String kProofForegroundColorString = "19546C";
	static final protected String kProofForegroundColorString = "0C2A36";
	
	static final protected String kQuestionBackgroundColorString = "E9FFED";
	static final protected String kSolutionBackgroundColorString = "FFFFFF";
//	static final protected String kSolutionForegroundColorString = "18461F";
	static final protected String kSolutionForegroundColorString = "0C230F";
	
	
	static final public String kSerifFontName = "Times New Roman";
	static final public String kSanSerifFontName = "Arial";
	
	
	static private float fontScaleFactor = 1.0f;

	static public java.io.File coreDir;
	static private java.io.File styleSheetFile;
	
	static private StyleSheet styleSheet = null;
	static private StyleSheet boxedStyleSheet = null;
	static private StyleSheet diagramStyleSheet = null;
	static private StyleSheet definitionStyleSheet = null;
	static private StyleSheet exampleStyleSheet = null;
	
	static private StyleSheet theoremStyleSheet = null;
	static private StyleSheet proofStyleSheet = null;
	static private StyleSheet questionStyleSheet = null;
	static private StyleSheet solutionStyleSheet = null;
	static private StyleSheet lectureNoteStyleSheet = null;
	static private StyleSheet lecturingStyleSheet = null;
	
	static public void setCoreDir(java.io.File coreDirParam) {
		coreDir = coreDirParam;
		styleSheetFile = new java.io.File(coreDirParam, "java/" + kStyleSheetUrl);
	}
	
	static public int scaledSize(int baseSize) {
		return (int)Math.round(baseSize * fontScaleFactor);
	}
	
	static public void setScaleFactor(float f) {
		fontScaleFactor = f;
		
		styleSheet = null;
		boxedStyleSheet = null;
		diagramStyleSheet = null;
		definitionStyleSheet = null;
		exampleStyleSheet = null;
		
		theoremStyleSheet = null;
		proofStyleSheet = null;
		questionStyleSheet = null;
		solutionStyleSheet = null;
		lectureNoteStyleSheet = null;
		lecturingStyleSheet = null;
	}
	
	static public StyleSheet getStyleSheet() {
		if (styleSheet == null) {
			styleSheet = new StyleSheet();
			try {
				URL styleUrl = styleSheetFile.toURI().toURL();
				styleSheet.importStyleSheet(styleUrl);
				
				styleSheet.addRule("body {background-color:#" + kNormalBackgroundColorString + ";}");
				styleSheet.addRule("body {font-size:" + scaledSize(16) + "pt;}");
			} catch (MalformedURLException e) {
				System.out.println("Cannot find URL for styleSheet");
				e.printStackTrace(System.out);
			}
		}
		return styleSheet;
	}
	
	static public StyleSheet getBoxedStyleSheet() {
		if (boxedStyleSheet == null) {
			boxedStyleSheet = new StyleSheet();
			try {
				URL styleUrl = styleSheetFile.toURI().toURL();
				boxedStyleSheet.importStyleSheet(styleUrl);
				
				boxedStyleSheet.addRule("body {margin:0px 0px;}");
				boxedStyleSheet.addRule("body {background-color:#" + kBoxedBackgroundColorString + ";}");
				boxedStyleSheet.addRule("body {font-size:" + scaledSize(16) + "pt;}");
				boxedStyleSheet.addRule("p {font-weight:bold;}");
				boxedStyleSheet.addRule("p {color:black;}");
				boxedStyleSheet.addRule("p {text-align:center;}");
				boxedStyleSheet.addRule("strong {color:red;}");
				boxedStyleSheet.addRule("ul {margin:5px 5px 13px 15px;}");
				boxedStyleSheet.addRule("ul {color:black;}");
				boxedStyleSheet.addRule("ul {font-weight:bold;}");
//				boxedStyleSheet.addRule("li {margin-left:7px;}");
			} catch (MalformedURLException e) {
				System.out.println("Cannot find URL for styleSheet");
				e.printStackTrace(System.out);
			}
		}
		return boxedStyleSheet;
	}
	
	static public StyleSheet getDiagramStyleSheet() {
		if (diagramStyleSheet == null) {
			diagramStyleSheet = new StyleSheet();
			try {
				URL styleUrl = styleSheetFile.toURI().toURL();
				diagramStyleSheet.importStyleSheet(styleUrl);
				
				diagramStyleSheet.addRule("body {background-color:#" + kDiagramBackgroundColorString + ";}");
				diagramStyleSheet.addRule("body {font-size:" + scaledSize(16) + "pt;}");
				diagramStyleSheet.addRule("p {color:black;}");
			} catch (MalformedURLException e) {
				System.out.println("Cannot find URL for styleSheet");
				e.printStackTrace(System.out);
			}
		}
		return diagramStyleSheet;
	}
	
	static public StyleSheet getLectureNoteStyleSheet() {
		if (lectureNoteStyleSheet == null) {
			lectureNoteStyleSheet = new StyleSheet();
			try {
				URL styleUrl = styleSheetFile.toURI().toURL();
				lectureNoteStyleSheet.importStyleSheet(styleUrl);
				
				lectureNoteStyleSheet.addRule("body {background-color:#" + kLectureNoteBackgroundColorString + ";}");
				lectureNoteStyleSheet.addRule("body {font-size:" + scaledSize(16) + "pt;}");
			} catch (MalformedURLException e) {
				System.out.println("Cannot find URL for styleSheet");
				e.printStackTrace(System.out);
			}
		}
		return lectureNoteStyleSheet;
	}
	
	static public StyleSheet getLecturingStyleSheet() {
		if (lecturingStyleSheet == null) {
			lecturingStyleSheet = new StyleSheet();
			try {
				URL styleUrl = styleSheetFile.toURI().toURL();
				lecturingStyleSheet.importStyleSheet(styleUrl);
				
				lecturingStyleSheet.addRule("body {background-color:#" + kLecturingBackgroundColorString + ";}");
				lecturingStyleSheet.addRule("body {font-size:" + scaledSize(16) + "pt;}");
			} catch (MalformedURLException e) {
				System.out.println("Cannot find URL for styleSheet");
				e.printStackTrace(System.out);
			}
		}
		return lecturingStyleSheet;
	}
	
//------------------------------------------------------------------------
	
	static private StyleSheet getBlockStyleSheet() {
		StyleSheet ss = new StyleSheet();
		try {
			URL styleUrl = styleSheetFile.toURI().toURL();
			ss.importStyleSheet(styleUrl);
			
			ss.addRule("body {margin:0px 0px;}");
			ss.addRule("body {color:#000000;}");
			ss.addRule("body {font-size:" + scaledSize(16) + "pt;}");
			ss.addRule("p {color:black;}");
			ss.addRule("ul {margin:0px 5px 5px 30px;}");
			ss.addRule("ul {color:black;}");
			ss.addRule("ol {margin:0px 5px 5px 30px;}");
			ss.addRule("ol {color:black;}");
		} catch (MalformedURLException e) {
			System.out.println("Cannot find URL for styleSheet");
			e.printStackTrace(System.out);
		}
		return ss;
	}
	
	static public StyleSheet getDefinitionStyleSheet() {
		if (definitionStyleSheet == null) {
			definitionStyleSheet = getBlockStyleSheet();
			definitionStyleSheet.addRule("body {background-color:#" + kDefinitionBackgroundColorString + ";}");
		}
		return definitionStyleSheet;
	}
	
	static public StyleSheet getExampleStyleSheet() {
		if (exampleStyleSheet == null) {
			exampleStyleSheet = getBlockStyleSheet();
			exampleStyleSheet.addRule("body {background-color:#" + kExampleBackgroundColorString + ";}");
		}
		return exampleStyleSheet;
	}
	
//------------------------------------------------------------------------
	
	static private StyleSheet getExpandingHeadingStyleSheet() {
		StyleSheet ss = new StyleSheet();
		try {
			URL styleUrl = styleSheetFile.toURI().toURL();
			ss.importStyleSheet(styleUrl);
			
			ss.addRule("body {margin:0px 0px;}");
			ss.addRule("body {color:#000000;}");
			ss.addRule("body {font-size:" + scaledSize(16) + "pt;}");
			ss.addRule("p {color:black;}");
			ss.addRule("ul {margin:0px 5px 5px 30px;}");
			ss.addRule("ul {color:black;}");
			ss.addRule("ol {margin:0px 5px 5px 30px;}");
			ss.addRule("ol {color:black;}");
		} catch (MalformedURLException e) {
			System.out.println("Cannot find URL for styleSheet");
			e.printStackTrace(System.out);
		}
		return ss;
	}
	
	static public StyleSheet getTheoremStyleSheet() {
		if (theoremStyleSheet == null) {
			theoremStyleSheet = getExpandingHeadingStyleSheet();
			theoremStyleSheet.addRule("body {background-color:#" + kTheoremBackgroundColorString + ";}");
		}
		return theoremStyleSheet;
	}
	
	static public StyleSheet getQuestionStyleSheet() {
		if (questionStyleSheet == null) {
			questionStyleSheet = getExpandingHeadingStyleSheet();
			questionStyleSheet.addRule("body {background-color:#" + kQuestionBackgroundColorString + ";}");
		}
		return questionStyleSheet;
	}
	
/*
	static private StyleSheet getExpandingBodyStyleSheet() {
		StyleSheet ss = new StyleSheet();
		try {
			URL styleUrl = styleSheetFile.toURI().toURL();
			ss.importStyleSheet(styleUrl);
			
			ss.addRule("body {margin:0px 0px;}");
			ss.addRule("body {font-size:" + scaledSize(16) + "pt;}");
			ss.addRule("ul {margin:0px 5px 5px 30px;}");
			ss.addRule("ol {margin:0px 5px 5px 30px;}");
		} catch (MalformedURLException e) {
			System.out.println("Cannot find URL for styleSheet");
			e.printStackTrace(System.out);
		}
		return ss;
	}
*/
	
	static public StyleSheet getProofStyleSheet() {
		if (proofStyleSheet == null) {
			proofStyleSheet = getExpandingHeadingStyleSheet();
			proofStyleSheet.addRule("body {background-color:#" + kProofBackgroundColorString + ";}");
			proofStyleSheet.addRule("body {color:#" + kProofForegroundColorString + ";}");
			proofStyleSheet.addRule("p {color:#" + kProofForegroundColorString + ";}");
			proofStyleSheet.addRule("ul {color:#" + kProofForegroundColorString + ";}");
			proofStyleSheet.addRule("ol {color:#" + kProofForegroundColorString + ";}");
		}
		return proofStyleSheet;
	}
	
	static public StyleSheet getSolutionStyleSheet() {
		if (solutionStyleSheet == null) {
			solutionStyleSheet = getExpandingHeadingStyleSheet();
			solutionStyleSheet.addRule("body {background-color:#" + kSolutionBackgroundColorString + ";}");
			solutionStyleSheet.addRule("body {color:#" + kSolutionForegroundColorString + ";}");
			solutionStyleSheet.addRule("p {color:#" + kSolutionForegroundColorString + ";}");
			solutionStyleSheet.addRule("ul {color:#" + kSolutionForegroundColorString + ";}");
			solutionStyleSheet.addRule("ol {color:#" + kSolutionForegroundColorString + ";}");
		}
		return solutionStyleSheet;
	}
	
//------------------------------------------------------------------------
	
	static public int getAttribute(StyleSheet s, String tag, CSS.Attribute property) {
		AttributeSet pAttributes = (AttributeSet)s.getStyle(tag);
		Object att = pAttributes.getAttribute(property);
		if (att == null)
			return 0;
		else {
			String as = att.toString();
			int ptStart = as.indexOf("pt");
			if (ptStart >= 0)
				as = as.substring(0, ptStart);
			else {
				int pxStart = as.indexOf("px");
				if (pxStart >= 0)
					as = as.substring(0, pxStart);
			}
			return Integer.parseInt(as);
		}
	}
	
	static public String getBackgroundColorString(StyleSheet s) {
		AttributeSet pAttributes = (AttributeSet)s.getStyle("body");
		Object att = pAttributes.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
		return att.toString().substring(1);
	}
	
	static public Color getBackgroundColor(StyleSheet s) {
		return new Color(Integer.parseInt(getBackgroundColorString(s), 16));
	}
	
	static public String getForegroundColorString(StyleSheet s) {
		AttributeSet pAttributes = (AttributeSet)s.getStyle("body");
		Object att = pAttributes.getAttribute(CSS.Attribute.COLOR);
		if (att == null)
			return "000000";
		else
			return att.toString().substring(1);
	}
	
	static public Color getForegroundColor(StyleSheet s) {
		return new Color(Integer.parseInt(getForegroundColorString(s), 16));
	}
	
//--------------------------------------------------------------------
	
	private Vector children = new Vector();
	
	public CoreDrawer() {
	}
	
	public void addChild(CoreDrawer newSection) {
		children.add(newSection);
	}
	
	public int noOfChildren() {
		return children.size();
	}
	
	public CoreDrawer getChild(int i) {
		return (CoreDrawer)children.elementAt(i);
	}
	
	public void removeChild(CoreDrawer drawer) {
		children.removeElement(drawer);
	}
	
	abstract public JPanel createPanel();
	
	public int getMinimumWidth() {
		int minWidth = 0;
		for (int i=0 ; i<noOfChildren() ; i++)
			minWidth = Math.max(minWidth, getChild(i).getMinimumWidth());
		return minWidth;
	}
}
