package pageStructure;

import java.awt.*;
import java.awt.font.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import utils.*;


public class LectureLinkDrawer extends CoreDrawer {
	static final private String kLectureNote = "Notes about use of diagram";
	static final private String kLectureNoteDim = "Hide lecturer notes";
	static final private String kDataNote = "Notes about data";
	static final private String kDataNoteDim = "Hide data notes";
	
	static final private int kBaseLinkFontSize = 20;
	static final private Color kLinkColor = new Color(0x660000);
	static final private Color kLinkColorSelected = new Color(0xFF0000);
	
	private CoreDrawer lectureNote, dataNote;
	private JPanel lectureNotePanel, dataNotePanel;
	private JLabel lectureNoteLink, dataNoteLink;
	
	public LectureLinkDrawer() {
	}
	
	
	public void setNotes(CoreDrawer lectureNote, CoreDrawer dataNote) {
		this.lectureNote = lectureNote;
		this.dataNote = dataNote;
	}
	
	public JPanel createPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		thePanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
		thePanel.setOpaque(false);
//		thePanel.setOpaque(true);
//		thePanel.setBackground(Color.yellow);
			
		thePanel.add(linkPanel());
		
		if (lectureNote != null) {
			lectureNotePanel = lectureNote.createPanel();
			lectureNotePanel.setVisible(false);
			thePanel.add(lectureNotePanel);
		}
		
		if (dataNote != null) {
			dataNotePanel = dataNote.createPanel();
			dataNotePanel.setVisible(false);
			thePanel.add(dataNotePanel);
		}
		
		return thePanel;
	}
	
	private JPanel linkPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		thePanel.setOpaque(false);
		thePanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 8, 0));
		
		Font linkFont = new Font(kSerifFontName, Font.BOLD, kBaseLinkFontSize);
			Map attributes = linkFont.getAttributes();
			attributes.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_TWO_PIXEL);
			linkFont = linkFont.deriveFont(attributes);
		
		if (lectureNote != null) {
			lectureNoteLink = new JLabel(kLectureNote, JLabel.LEFT);
			lectureNoteLink.setFont(linkFont);
			lectureNoteLink.setForeground(kLinkColor);
			lectureNoteLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
			lectureNoteLink.addMouseListener(new MouseAdapter() {
														public void mouseClicked(MouseEvent e) {
															boolean newVisible = !lectureNotePanel.isVisible();
															lectureNotePanel.setVisible(newVisible);
															lectureNoteLink.setText(newVisible ? kLectureNoteDim : kLectureNote);
															lectureNoteLink.setForeground(newVisible ? kLinkColorSelected : kLinkColor);
															
															if (dataNotePanel != null && newVisible) {
																dataNotePanel.setVisible(false);
																dataNoteLink.setText(kDataNote);
																dataNoteLink.setForeground(kLinkColor);
															}
														}
													});
			
			thePanel.add("West", lectureNoteLink);
		}
		if (dataNote != null) {
			dataNoteLink = new JLabel(kDataNote, JLabel.RIGHT);
			dataNoteLink.setFont(linkFont);
			dataNoteLink.setForeground(kLinkColor);
			dataNoteLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
			dataNoteLink.addMouseListener(new MouseAdapter() {
														public void mouseClicked(MouseEvent e) {
															boolean newVisible = !dataNotePanel.isVisible();
															dataNotePanel.setVisible(newVisible);
															dataNoteLink.setText(newVisible ? kDataNoteDim : kDataNote);
															dataNoteLink.setForeground(newVisible ? kLinkColorSelected : kLinkColor);
															
															if (lectureNotePanel != null && newVisible) {
																lectureNotePanel.setVisible(false);
																lectureNoteLink.setText(kLectureNote);
																lectureNoteLink.setForeground(kLinkColor);
															}
														}
													});

			thePanel.add("East", dataNoteLink);
		}
		return thePanel;
	}
}
