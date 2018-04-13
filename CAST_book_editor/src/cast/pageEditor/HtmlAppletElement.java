package cast.pageEditor;

import java.awt.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;

import cast.utils.*;


public class HtmlAppletElement extends CoreHtmlElement {
	public HtmlAppletElement(String appletString, JPanel parent) {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 5));
		thePanel.setOpaque(false);
		
			Border innerBorder = BorderFactory.createEmptyBorder(4, 10, 2, 6);
			Border redLineBorder = BorderFactory.createLineBorder(Color.red, 1);
			Border compound = BorderFactory.createCompoundBorder(redLineBorder, innerBorder);
			Border outerBorder = BorderFactory.createEmptyBorder(0, 40, 0, 40);
			Border compound2 = BorderFactory.createCompoundBorder(outerBorder, compound);
		thePanel.setBorder(compound2);
		
		
		Pattern thePattern = Pattern.compile("^(\\s*<script[^>]*>[^<>]*</script>)(.*$)", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher theMatcher = thePattern.matcher(appletString);
		if (theMatcher.find()) {
			insertElement(new HtmlStringElement(theMatcher.group(1)));
			appletString = theMatcher.group(2);
		}
		
		thePattern = Pattern.compile("([^<]*)<param\\s*name=['\"]([^'\"]*)['\"]\\s*value=['\"]([^'\"]*)['\"]\\s*>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			theMatcher = thePattern.matcher(appletString);
		while (theMatcher.find()) {
			String startString = theMatcher.group(1);
			if (startString.length() > 0)
				insertElement(new HtmlStringElement(startString));
			
			String paramName = theMatcher.group(2);
			String paramValue = theMatcher.group(3);
			
			if (paramName.equals("customText")) {
				insertElement(new HtmlCustomTextElement(paramValue, thePanel));
			}
			else {
				boolean hasText = false;
				String testText = paramValue.replace("true", "").replace("false", "");	//	"true" and "false" shouldn't be translated
				for (int i=0 ; i<testText.length() ; i++)
					if (Character.isLetter(paramValue.charAt(i))) {
						hasText = true;
					}
				if (hasText)
					insertElement(new HtmlParamElement(paramName, paramValue, thePanel));
				else
					insertElement(new HtmlStringElement("<param name=\"" + paramName + "\" value=\"" + paramValue + "\">"));
			}
			
		}
		
		int endOfParams = appletString.lastIndexOf(">") + 1;
		
		if (endOfParams > 0 && endOfParams < appletString.length())
			insertElement(new HtmlStringElement(appletString.substring(endOfParams)));
		
		parent.add(thePanel);
	}
}
