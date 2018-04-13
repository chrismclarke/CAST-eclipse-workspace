package pageStructure;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.text.html.*;

import utils.*;
import ebook.*;


public class DivChoiceDrawer extends CoreDrawer {
	static final private String kDataChoicePattern = "<form>.*?changeDataset.*?>(.*?)</select>\\s*</form>(.*)";
	static final private String kDataChoice2Pattern = "<form>.*?changeBlock.*?>(.*?)</select>\\s*</form>(.*)";
	static final private String kDataOptionPattern = "<option value=\"(.*?)\".*?>(.*?)</option>";
	
	private Vector divNames = new Vector();
	private Vector dataDivs = new Vector();
	
	private boolean isLecturing;
	
	public DivChoiceDrawer(String htmlString, String dirString, StyleSheet theStyleSheet,
																																	BookFrame theBookFrame, Map namedApplets) {
		String optionsString = null;
		String divsString = null;
		
		isLecturing = theBookFrame.getEbook().isLecturingVersion();
		
		Pattern dataChoicePattern = Pattern.compile(kDataChoicePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher dataChoiceMatcher = dataChoicePattern.matcher(htmlString);
		if (dataChoiceMatcher.find()) {
			optionsString = dataChoiceMatcher.group(1);
			divsString = dataChoiceMatcher.group(2);
		}
		else {
			Pattern dataChoice2Pattern = Pattern.compile(kDataChoice2Pattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher dataChoice2Matcher = dataChoice2Pattern.matcher(htmlString);
			if (dataChoice2Matcher.find()) {
				optionsString = dataChoice2Matcher.group(1);
				divsString = dataChoice2Matcher.group(2);
			}
		}

		if (optionsString != null) {
			Pattern optionPattern = Pattern.compile(kDataOptionPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher optionMatcher = optionPattern.matcher(optionsString);
			while (optionMatcher.find()) {
				String idString = optionMatcher.group(1);
				if (idString.length() == 1 && idString.charAt(0) >= '0' && idString.charAt(0)<= '9') {
					idString = "example" + (Integer.parseInt(idString) + 1);		//	for changeBlock(), options are "0", "1", ...
																																			//	and divs have id "example1", "example2", etc.
				}
				String optionNameString = optionMatcher.group(2);
				
				Pattern dataPattern = Pattern.compile("(.*)<div id=\"" + idString + "\".*?>(.*)", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
				Matcher dataMatcher = dataPattern.matcher(divsString);
				if (dataMatcher.find()) {
					String startString = dataMatcher.group(1);
					String endString = dataMatcher.group(2);
					int divEndIndex = endString.indexOf("</div>");
					int tempStartIndex = 0;
					while (true) {
						int startIndexOfInteriorDiv = endString.substring(0, divEndIndex).indexOf("<div", tempStartIndex);
						if (startIndexOfInteriorDiv < 0)
							break;
						tempStartIndex = startIndexOfInteriorDiv + 5;
						divEndIndex = endString.indexOf("</div>", divEndIndex + 6);
					}
					String thisDivString = endString.substring(0, divEndIndex);
					
					divsString = startString + endString.substring(divEndIndex + 6);	//	remove this <div>
					
					addChild(new HtmlDrawer(thisDivString, dirString, theStyleSheet, theBookFrame, namedApplets));
					
					divNames.add(optionNameString);
				}
			}
		}
	}
	
	
	public JPanel createPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setOpaque(false);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			JPanel topPanel = new JPanel();
			topPanel.setOpaque(false);
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			int menuSpacing = isLecturing ? 40 : 20;
			topPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, menuSpacing, 0));
			final JComboBox divChoice = new JComboBox();
				for (int i=0 ; i<divNames.size() ; i++) {
					divChoice.addItem((String)divNames.get(i));
					JPanel theDiv = getChild(i).createPanel();
					theDiv.setVisible(i == 0);
					dataDivs.add(theDiv);
				}
				
				divChoice.addActionListener(new ActionListener() {
												public void actionPerformed(ActionEvent e) { 
													int selectedIndex = divChoice.getSelectedIndex();
													for (int i=0 ; i<dataDivs.size() ; i++)
														((JPanel)dataDivs.get(i)).setVisible(i == selectedIndex);
												}
				});
				Font menuFont = divChoice.getFont();
				int size = menuFont.getSize();
				int scaledSize = scaledSize(size);
				if (size != scaledSize)
					divChoice.setFont(menuFont.deriveFont(scaledSize));
			topPanel.add(divChoice);
		
		thePanel.add("North", topPanel);
		
			JPanel divsPanel = new JPanel();
			divsPanel.setOpaque(false);
			divsPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
			for (int i=0 ; i<dataDivs.size() ; i++)
				divsPanel.add((JPanel)dataDivs.get(i));
				
		thePanel.add("Center", divsPanel);
		
		return thePanel;
	}
}
