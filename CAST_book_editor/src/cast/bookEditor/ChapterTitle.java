package cast.bookEditor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import cast.bookManager.*;
import cast.utils.*;


public class ChapterTitle extends ElementTitle {
	
	static final public Color kGreyColor = new Color(0x333333);
	
	private CastEbook castEbook;
	private int chapterIndex;
	
	private JLabel fileLocation;
	
	public ChapterTitle(final DomChapter domChapter, final CastEbook castEbook, int chapterIndex) {
		super(domChapter);
		this.castEbook = castEbook;
		this.chapterIndex = chapterIndex;
		
		setLayout(new BorderLayout(0, 0));
		setOpaque(false);
		
			title = new JLabel("");
			setTitle(domChapter);
			title.setFont(new Font("SansSerif", Font.BOLD, 14));
		add("Center", title);
		
			JPanel labelPanel = new JPanel();
			labelPanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT, VerticalLayout.VERT_BOTTOM, 0));
			labelPanel.setOpaque(false);
		
				fileLocation = new JLabel("");
				setFileLocation(domChapter);
				fileLocation.setFont(new Font("SansSerif", Font.ITALIC | Font.BOLD, 11));
				fileLocation.setForeground(kGreyColor);
			labelPanel.add(fileLocation);
			
		add("East", labelPanel);
		
		if (castEbook.canEditBook()) {
			menu = new JPopupMenu();
			
			final JMenuItem copyItem = new JMenuItem("Replace with Copy...");
			menu.add(copyItem);
			copyItem.addActionListener(new ActionListener() {
																	public void actionPerformed(ActionEvent e) {
																		if (domChapter.createCopyInEbook(ChapterTitle.this)) {
																			copyItem.setEnabled(false);
																			setFileLocation(domChapter);
																		}
																	}
													});
			if (domChapter.getDir().equals(castEbook.getHomeDirName()))
				copyItem.setEnabled(false);
			
			JMenuItem changeItem = new JMenuItem("Select New Chapter File...");
			menu.add(changeItem);
			changeItem.addActionListener(new ActionListener() {
																	public void actionPerformed(ActionEvent e) {
																		File newPageFile = castEbook.selectHtmlFile(ChapterTitle.this);
																		if (newPageFile != null) {
																			String newFileName = newPageFile.getName();
																			String newFilePrefix = newFileName.substring(0, newFileName.length() - 5);
																			String newDir = newPageFile.getParentFile().getParentFile().getName() + "/" + newPageFile.getParentFile().getName();
																			domChapter.setChapterFile(newDir, newFilePrefix);
																			setTitle(domChapter);
																			setFileLocation(domChapter);
																			copyItem.setEnabled(!newDir.equals(castEbook.getHomeDirName()));
																		}
																	}
													});
			if (!CastEbook.isPreface(domChapter.getFilePrefix())) {
				JMenuItem deleteItem = new JMenuItem("Delete");
				menu.add(deleteItem);
				deleteItem.addActionListener(new ActionListener() {
																public void actionPerformed(ActionEvent e) {
																	int result = JOptionPane.showConfirmDialog(ChapterTitle.this, "Are you sure that you want to delete this chapter?",
																							"Delete Chapter?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
																	
																	if (result == JOptionPane.OK_OPTION)
																		deleteSelf(castEbook);
																}
												});
			}
		}
			
		setMenuDragMouseListener(castEbook);
		
		setTitleForeground(Color.black, kGreyColor, castEbook);
	}
	
	public Insets getInsets() {
		return new Insets(3, 20, 0, 0);
	}
	
	private void setTitle(DomChapter domChapter) {
		String decodedTitle = domChapter.getChapterName();
		title.setText(chapterIndex + ". " + decodedTitle);
	}
	
	private void setFileLocation(DomChapter domChapter) {
		String dir = domChapter.getDir();
		String filePrefix = domChapter.getFilePrefix();
		if (!castEbook.canChangeStructure())
			fileLocation.setText("");
		else if (dir.equals(castEbook.getHomeDirName()))
			fileLocation.setText(filePrefix + " ");
		else
			fileLocation.setText(filePrefix + " (in " + dir + ") ");
	}
}
