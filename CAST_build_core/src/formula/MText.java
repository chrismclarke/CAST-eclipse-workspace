package formula;

import java.awt.*;
import java.util.*;

import dataView.*;


public class MText extends MFormula {
	static final private Hashtable codeLookup;
	
	static {
		codeLookup = new Hashtable(4);
		codeLookup.put("alpha", "\u03B1");
		codeLookup.put("beta", "\u03B2");
		codeLookup.put("gamma", "\u03B3");
		codeLookup.put("delta", "\u03B4");
		codeLookup.put("epsilon", "\u03B5");
		codeLookup.put("mu", "\u03BC");
		codeLookup.put("sigma", "\u03C3");
		codeLookup.put("capitalSigma", "\u03A3");
		codeLookup.put("rho", "\u03C1");
		codeLookup.put("pi", "\u03C0");
		codeLookup.put("lambda", "\u03BB");
		codeLookup.put("theta", "\u03B8");
		codeLookup.put("chi", "\u03C7");
		codeLookup.put("hat", "\u0302");
		codeLookup.put("bar", "\u0305");
		codeLookup.put("sub0", "\u2080");
		codeLookup.put("sub1", "\u2081");
		codeLookup.put("sub2", "\u2082");
		codeLookup.put("sub3", "\u2083");
		codeLookup.put("sub4", "\u2084");
		codeLookup.put("sub5", "\u2085");
		codeLookup.put("sub6", "\u2086");
		codeLookup.put("sub7", "\u2087");
		codeLookup.put("sub8", "\u2088");
		codeLookup.put("sub9", "\u2089");
		codeLookup.put("sup0", "\u2070");
		codeLookup.put("sup1", "\u00B9");
		codeLookup.put("sup2", "\u00B2");
		codeLookup.put("sup3", "\u00B3");
		codeLookup.put("sup4", "\u2074");
		codeLookup.put("sup5", "\u2075");
		codeLookup.put("sup6", "\u2076");
		codeLookup.put("sup7", "\u2077");
		codeLookup.put("sup8", "\u2078");
		codeLookup.put("sup9", "\u2079");
		codeLookup.put("plusMinus", "\u00B1");
		codeLookup.put("degrees", "\u00B0");
		codeLookup.put("approxEqual", "\u2248");
		codeLookup.put("sqrt", "\u221A");
		codeLookup.put("infinity", "\u221E");
		codeLookup.put("bullet", "\u2022");
		codeLookup.put("spade", "\u2660");
		codeLookup.put("club", "\u2663");
		codeLookup.put("heart", "\u2665");
		codeLookup.put("diamond", "\u2666");
		codeLookup.put("en", "\u2013");
		codeLookup.put("em", "\u2014");
		codeLookup.put("times", "\u00D7");
		codeLookup.put("quarter", "\u00BC");
		codeLookup.put("half", "\u00BD");
		codeLookup.put("le", "\u2264");
		codeLookup.put("ge", "\u2265");
		codeLookup.put("ne", "\u2260");
		codeLookup.put("lt", "<");
		codeLookup.put("gt", ">");
		codeLookup.put("amp", "&");
		codeLookup.put("quot", "\"");
		codeLookup.put("apos", "'");
		codeLookup.put("ell", "\u2113");
		codeLookup.put("tilde", "\u007E");
		codeLookup.put("sim", "\u007E");		//	same as tilde
	}
	
	static final public String expandText(String s) {
		int hashIndex = s.indexOf("#");
		if (hashIndex >= 0) {
			StringBuffer sb = new StringBuffer(s);
			int charIndex = 0;
			while (charIndex < sb.length()) {
				if (sb.charAt(charIndex) == '#') {
					int upperIndex = charIndex + 1;
					while (upperIndex < sb.length() - 1 && sb.charAt(upperIndex) != '#')
						upperIndex ++;
					String code = sb.substring(charIndex + 1, upperIndex);
					if (code.indexOf("sub") == 0 && code.length() > 4) {
													//	to deal with subscripts > 9;
						String translated = "";
						for (int i=3 ; i<code.length() ; i++)
							translated += (String)codeLookup.get("sub" + code.charAt(i));
						sb.replace(charIndex, upperIndex + 1, translated);
					}
					else if (code.indexOf("sup") == 0 && code.length() > 4) {
													//	to deal with superscripts > 9;
						String translated = "";
						for (int i=3 ; i<code.length() ; i++)
							translated += (String)codeLookup.get("sup" + code.charAt(i));
						sb.replace(charIndex, upperIndex + 1, translated);
					}
					else {
						String translated = (String)codeLookup.get(code);
						if (translated != null)
							sb.replace(charIndex, upperIndex + 1, translated);
						else
							charIndex = upperIndex;
					}
				}
				charIndex ++;
			}
			return sb.toString();
		}
		else
			return s;
	}
	
	static final public String translateUnicode(String s) {
		return (String)codeLookup.get(s);
	}
	
	static final private String kCharsWithDescent = expandText("gjpqy,;#beta##mu##rho##sub0##sub1##sub2##sub3##sub4##sub5##sub6##sub7##sub8##sub9#");
	static final private String kCharsWithTopAscent = expandText("ABCDEFGHIJKLMNOPQRSTUVWXYZbdfhijklt1234567890?!(){}[]#degrees##sup0##sup1##sup2##sup3##sup4##sup5##sup6##sup7##sup8##sup9##gamma##delta##hat##bar#");
	
	static final public boolean hasDescent(String s) {
		for (int i=0 ; i<s.length() ; i++)
			if (kCharsWithDescent.indexOf(s.charAt(i)) >= 0)
				return true;
		return false;
	}
	
	static final public boolean hasTopAscent(String s) {
		for (int i=0 ; i<s.length() ; i++)
			if (kCharsWithTopAscent.indexOf(s.charAt(i)) >= 0)
				return true;
		return false;
	}
	
	//----------------------------------------------------------------------------------------
	
	private Value theValue;
	
	public MText(Value theValue, FormulaContext context) {
		super(context);
		
		this.theValue = theValue;
	}
	
	public MText(String theString, FormulaContext context) {
		this(new LabelValue(expandText(theString)), context);
	}
	
	protected void doInitialisation(Graphics g) {
		super.doInitialisation(g);
		
		String valueString = theValue.toString();
		
		layoutWidth = g.getFontMetrics().stringWidth(valueString);
		layoutAscent = hasTopAscent(valueString) ? ascent : ascent * 4 / 5;
		layoutDescent = hasDescent(valueString) ? descent : 1;
	}
	
	public void layoutContainer(Container parent) {
	}
	
	protected void paintAroundItems(Graphics g) {
		int horizStart = (getSize().width - layoutWidth) / 2;
		int baseline = (getSize().height + layoutAscent - layoutDescent) / 2;
		
		theValue.drawRight(g, horizStart, baseline);
	}
}