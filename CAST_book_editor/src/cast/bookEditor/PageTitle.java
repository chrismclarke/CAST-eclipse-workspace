package cast.bookEditor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import cast.bookManager.*;
import cast.utils.*;


public class PageTitle extends ElementTitle {
	
//	static final private Color kSelectedBackground = new Color(0xBBBBFF);
	static final private Color kGreyColor = ChapterTitle.kGreyColor;
	static final private Color kLockedColor = new Color(0x550000);
	
	private CastEbook castEbook;
	private int pageIndex;
	
	private JLabel fileLocation;
//	private JButton changeButton;
//	private boolean showingButtons = false;
	
//	private JPanel buttonPanel;
	
	public PageTitle(final DomPage domPage, final CastEbook castEbook, int pageIndex) {
		super(domPage);
		this.castEbook = castEbook;
		this.pageIndex = pageIndex;
		
		setLayout(new BorderLayout(0, 0));
		setOpaque(false);
		
		String decodedTitle = domPage.getPageName();
		title = new JLabel(pageIndex + ". " + decodedTitle);
		title.setFont(new Font("SansSerif", Font.ITALIC, 12));
		add("Center", title);
		
			JPanel labelPanel = new JPanel();
			labelPanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT, VerticalLayout.VERT_BOTTOM, 0));
			labelPanel.setOpaque(false);
		
				fileLocation = new JLabel("");
				setFileLocation(domPage);
				fileLocation.setFont(new Font("SansSerif", Font.ITALIC | Font.BOLD, 11));
				fileLocation.setForeground(kGreyColor);
			labelPanel.add(fileLocation);
		
		add("East", labelPanel);
		
		if (castEbook.canEditBook()) {
			menu = new JPopupMenu();
			JMenuItem changeItem = new JMenuItem("Change Page...");
			menu.add(changeItem);
			changeItem.addActionListener(new ActionListener() {
																public void actionPerformed(ActionEvent e) {
																	File newPageFile = castEbook.selectHtmlFile(PageTitle.this);
																	if (newPageFile != null) {
																		String newFileName = newPageFile.getName();
																		String newFilePrefix = newFileName.substring(0, newFileName.length() - 5);
																		String newDir = newPageFile.getParentFile().getParentFile().getName() + "/" + newPageFile.getParentFile().getName();
																		domPage.setPageFile(newDir, newFilePrefix);
																		setTitle(domPage);
																		setFileLocation(domPage);
																	}
																}
												});
		
			JMenuItem deleteItem = new JMenuItem("Delete");
			menu.add(deleteItem);
			deleteItem.addActionListener(new ActionListener() {
														public void actionPerformed(ActionEvent e) {
															int result = JOptionPane.showConfirmDialog(PageTitle.this, "Are you sure that you want to delete this page?",
																					"Delete Page?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
															
															if (result == JOptionPane.OK_OPTION)
																deleteSelf(castEbook);
														}
										});
		}
		
		setMenuDragMouseListener(castEbook);
		
		if (!castEbook.canChangeStructure()) {
			title.setForeground(kGreyColor);
			
			JLabel lockedLabel = new JLabel("(Locked)");
			lockedLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
			lockedLabel.setForeground(kLockedColor);
			add("East", lockedLabel);
		}
	}
	
	public Insets getInsets() {
		return new Insets(0, 40, 0, 0);
	}
	
	private void setTitle(DomPage domPage) {
		String decodedTitle = domPage.getPageName();
		title.setText(pageIndex + ". " + decodedTitle);
	}
	
	private void setFileLocation(DomPage domPage) {
		String dir = domPage.getDir();
		String filePrefix = domPage.getFilePrefix();
		if (!castEbook.canChangeStructure())
			fileLocation.setText("");
		else if (dir.equals(castEbook.getHomeDirName()))
			fileLocation.setText(filePrefix + "    ");
		else
			fileLocation.setText(filePrefix + " (in " + dir + ")    ");
	}
}
