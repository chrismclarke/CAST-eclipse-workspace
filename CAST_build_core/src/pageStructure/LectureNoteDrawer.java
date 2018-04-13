package pageStructure;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import ebook.*;


public class LectureNoteDrawer extends HtmlDrawer {
	static final private Color kNoteBorderColor = new Color(0xFFCC99);
	
	public LectureNoteDrawer(String htmlString, String dirString, BookFrame theBookFrame, Map namedApplets) {
		super(htmlString, dirString, getLectureNoteStyleSheet(), theBookFrame, namedApplets);
	}
	
	
	public JPanel createPanel() {
		JPanel thePanel = super.createPanel();
		thePanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, kNoteBorderColor));
		
		return thePanel;
	}
}
