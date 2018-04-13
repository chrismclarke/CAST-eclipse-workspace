package dataView;

import java.applet.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;

import utils.*;


abstract public class XApplet extends JPanel {
	static private final Color kStandardBackColor = new Color(0xffffff);
	
	static final protected String VAR_NAME_PARAM = "varName";
	static final protected String VALUES_PARAM = "values";
	static final protected String LABEL_NAME_PARAM = "labelName";
	static final protected String LABELS_PARAM = "labels";
	static final protected String STEM_AXIS_PARAM = "stemAxis";
											//	minStem, repeatsPerStem, minStemRepeat, noOfBins, stemPower
	static final protected String CAT_NAME_PARAM = "catVarName";
	static final protected String CAT_VALUES_PARAM = "catValues";
	static final protected String CAT_LABELS_PARAM = "catLabels";
	static final private String FONT_SIZE_PARAM = "fontSize";
	static final private String BACKGROUND_COLOR_PARAM = "backgroundColor";
	static final private String LANGUAGE_PARAM = "language";
	static final public String CUSTOM_TEXT_PARAM = "customText";
	
	static private final String BIG = "big";
	
	static final public int OS_MAC = 0;
	static final public int OS_XP = 1;
	static final public int OS_VISTA = 2;
	static final public int OS_OTHER = 3;
	
	static public int osType;
	static {
		String osName = System.getProperty("os.name").toLowerCase();
		osType = osName.indexOf("mac") >= 0 ? OS_MAC
					: osName.indexOf("vista") >= 0 ? OS_VISTA
					: osName.indexOf("win") >= 0 ? OS_XP
					: OS_OTHER;
	}
	
	static public final String FONT = (osType == OS_VISTA || osType == OS_XP)
												? "Verdana, Sans-serif" : "Lucida Sans, Sans-serif";
	
	static public XApplet theApplet;
	
	public XApplet() {
		theApplet = this;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) { }
	}
	
	public TextStrings textStrings;
	
	private Font standardFont, standardBoldFont, standardItalicFont;
	private Font bigFont, bigBoldFont;
	private Font bigBigFont, bigBigBoldFont;
	private Font smallFont, smallBoldFont;
	private Font tinyFont, tinyBoldFont;
	
	private boolean initialisedFonts = false;
	private boolean bigFonts;
	
	private Map namedApplets;
	
	
//------------------------------------------------------------------------------
	
	public int scaledFontSize(int baseSize) {
		if (usesBigFonts())
			baseSize *= 1.1667;		//	changes 12 point to 14 point
		return (int)Math.round(baseSize);
	}
	
	public boolean usesBigFonts() {
		if (!initialisedFonts) {
			String bigFontString = getParameter(FONT_SIZE_PARAM);
			bigFonts = (bigFontString != null) && bigFontString.equals(BIG);
			initialisedFonts = true;
		}
		return bigFonts;
	}
	
	public Font getStandardFont() {
		if (standardFont == null)
			standardFont = new Font(FONT, Font.PLAIN, scaledFontSize(12));
		return standardFont;
	}
	
	public Font getStandardBoldFont() {
		if (standardBoldFont == null)
			standardBoldFont = new Font(FONT, Font.BOLD, scaledFontSize(12));
		return standardBoldFont;
	}
	
	public Font getStandardItalicFont() {
		if (standardItalicFont == null)
			standardItalicFont = new Font(FONT, Font.ITALIC, scaledFontSize(12));
		return standardItalicFont;
	}
	
	public Font getSmallFont() {
		if (smallFont == null)
			smallFont = new Font(FONT, Font.PLAIN, scaledFontSize(10));
		return smallFont;
	}
	
	public Font getSmallBoldFont() {
		if (smallBoldFont == null)
			smallBoldFont = new Font(FONT, Font.BOLD, scaledFontSize(10));
		return smallBoldFont;
	}
	
	public Font getBigFont() {
		if (bigFont == null)
			bigFont = new Font(FONT, Font.PLAIN, scaledFontSize(14));
		return bigFont;
	}
	
	public Font getBigBoldFont() {
		if (bigBoldFont == null)
			bigBoldFont = new Font(FONT, Font.BOLD, scaledFontSize(14));
		return bigBoldFont;
	}
	
	public Font getBigBigFont() {
		if (bigBigFont == null)
			bigBigFont = new Font(FONT, Font.PLAIN, scaledFontSize(18));
		return bigBigFont;
	}
	
	public Font getBigBigBoldFont() {
		if (bigBigBoldFont == null)
			bigBigBoldFont = new Font(FONT, Font.BOLD, scaledFontSize(18));
		return bigBigBoldFont;
	}
	
	public Font getTinyFont() {
		if (tinyFont == null)
			tinyFont = new Font(FONT, Font.PLAIN, scaledFontSize(9));
		return tinyFont;
	}
	
	public Font getTinyBoldFont() {
		if (tinyBoldFont == null)
			tinyBoldFont = new Font(FONT, Font.PLAIN, scaledFontSize(9));
		return tinyBoldFont;
	}
	
	public String getAppletInfo() {
		return "A component of the CAST package\nCopyright � 1999-2017 W. Douglas Stirling";
	}
	
	protected XApplet getApplet(String appletName) {	//	to communicate with other applets on same page; no longer allowed
		if (appletName == null)
			return null;
		else if (appletParent == null) {
			XApplet theApplet = (XApplet)namedApplets.get(appletName);
			return theApplet;
		}
		else {
			@SuppressWarnings("deprecation")
			CastApplet otherParent = (CastApplet)appletParent.getAppletContext().getApplet(appletName);
			return otherParent.getXApplet();
		}

//		if (appletName == null)
//			return null;
//		else
//			return (XApplet)getAppletContext().getApplet(appletName);
	}
	
	
//------------------------------------------------------------------------------
	
	abstract protected void setupApplet();
	
	public void init() {
		String languageString = getParameter(LANGUAGE_PARAM);
		textStrings = new TextStrings(languageString, this);
		
//		if (badTestPin())
//			return;
		
		setBackground(getBackgroundColor());		//	some components query applet for background
		setupApplet();
		initBackgroundColor();									//	propagates background color to all components
		
		repaint();
	}
	
	public String translate(String key) {
		return textStrings.translate(key);
	}
	
//	protected boolean badTestPin() {	
//		return false;
//	}


//*********************************************************
	
	
	private Color getBackgroundColor() {
		String customBackgroundColor = getParameter(BACKGROUND_COLOR_PARAM);
		Color bg;
		try {
			int backgroundColorInt = Integer.parseInt(customBackgroundColor, 16);
			bg = new Color(backgroundColorInt);
		} catch (NumberFormatException e) {
			bg = kStandardBackColor;
		}
		return bg;
	}
	
	private void initBackgroundColor() {
		Color bg = getBackgroundColor();
		setBackground(bg);
		
		for (int i=0 ; i<getComponentCount() ; i++) {
			Component comp = getComponent(i);
			if (comp instanceof XPanel)
				((XPanel)comp).setBackground(bg);
		}
		
		setFont(getStandardFont());
	}
	
	protected void frameChanged(DataView theView) {
	}
	
	public void notifyDataChange(DataView theView) {
							//		to allow DataView to communicate with Applet when dragging makes changes
	}


//*********************************************************
	
	@SuppressWarnings("deprecation")
	private Applet appletParent = null;
	private HashMap<String,String> parameterMap = null;
	
	public void setAppletParent(@SuppressWarnings("deprecation") Applet appletParent) {
		this.appletParent = appletParent;
	}
	
	public void setParameters(HashMap<String,String> parameterMap) {
		this.parameterMap = parameterMap;
	}
	
	public void setNamedApplets(Map namedApplets) {
		this.namedApplets = namedApplets;
	}
	
	@SuppressWarnings("deprecation")
	public String getParameter(String key) {
		if (appletParent != null)
			return appletParent.getParameter(key);
		else
			return parameterMap.get(key);
	}
}