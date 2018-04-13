package cast.bookManager;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import cast.utils.*;


public class FileNameChooser extends JDialog {
	static final public int CHAPTER_TYPE = 0;
	static final public int PAGE_TYPE = 1;
	static final public int SECTION_TYPE = 2;
	
	static final private String[] kItemName = {"chapter", "page", "section"};
	static final private String[] kFileStart = {"ch_", "", "sec_"};
	
	public static String findNewFilePrefix(Frame parent, String filePrefix, String newDir,
																																	boolean isNewItem, int fileType) {
		FileNameChooser dialog = new FileNameChooser(parent, filePrefix,  newDir, isNewItem, fileType);

		Point p1 = parent.getLocation();
		Dimension d1 = parent.getSize();
		Dimension d2 = dialog.getSize();

		int x = p1.x + (d1.width - d2.width) / 2;
		int y = p1.y + (d1.height - d2.height) / 2;

		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;

		dialog.setLocation(x,y);
		dialog.setVisible(true);

		String newFilePrefix = dialog.ok ? dialog.getFilePrefix() : null;

		dialog.dispose();
		return newFilePrefix;
	}
	
	private JTextField newNameField;
	private boolean ok = false;
	private int fileType;
	
	public FileNameChooser(Frame parent, String filePrefix, String newDir, boolean isNewItem,
																																			int fileType) {
		super(parent, true);
		this.fileType = fileType;
		
		String itemType = kItemName[fileType];
		String fileStart = kFileStart[fileType];
		if (fileType == SECTION_TYPE)
			newDir += "/xml";
		String fileFormat = (fileType == SECTION_TYPE) ? "XML" : "HTML";
		String fileExtension = (fileType == SECTION_TYPE) ? ".xml" : ".html";
		
		setTitle(isNewItem ? ("New " + itemType + " file") : ("Duplicate " + itemType + " file"));
		
		if (fileStart.length() > 0 && filePrefix.indexOf(fileStart) == 0)
			filePrefix = filePrefix.substring(fileStart.length());
		
		JComponent contents = (JComponent)
		getContentPane();
		contents.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
		contents.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 12));
			
			String message;
//			int nLines;
			if (isNewItem)
				message = "An " + fileFormat + " file will be created for this new " + itemType + ". (It will be saved in folder \"" + newDir
																+ "\".) What should it be called?";
			else
				message = "Make a copy of the " + fileFormat + " file being used for this " + itemType + " (" + fileStart + filePrefix + ".html), "
																+ "save it in the current e-book (i.e. in folder \"" + newDir
																+ "\") and use it for the " + itemType + " page. You will probably want to keep the same name.";
			JTextArea instructions = new JTextArea(message, 3, 40);
			instructions.setLineWrap(true);
			instructions.setWrapStyleWord(true);
			instructions.setEditable(false);
			instructions.setOpaque(false);
		contents.add(instructions);
		
			JPanel newNamePanel = new JPanel();
			newNamePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				newNameField = new JTextField(filePrefix, 12);
				
				JLabel newLabel = new JLabel("New file name:  " + fileStart);
				newLabel.setLabelFor(newNameField);
				
				JLabel htmlLabel = new JLabel(fileExtension);
				htmlLabel.setLabelFor(newNameField);
		
			newNamePanel.add(newLabel);
			newNamePanel.add(newNameField);
			newNamePanel.add(htmlLabel);
		
		contents.add(newNamePanel);
		
			JTextArea warning = new JTextArea("Note that you will need to save the changes to the e-book "
																+ "for the new file to permanently appear in it.", 3, 30);
			warning.setLineWrap(true);
			warning.setWrapStyleWord(true);
			warning.setEditable(false);
			warning.setOpaque(false);
		contents.add(warning);
		
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			
				JButton saveButton = new JButton("Save");
				saveButton.addActionListener(new ActionListener() {
																				public void actionPerformed(ActionEvent e) {
																					ok = true;
																					setVisible(false);
																				}
																		});
			buttonPanel.add(saveButton);
			
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
																				public void actionPerformed(ActionEvent e) {
																					ok = false;
																					setVisible(false);
																				}
																		});
			buttonPanel.add(cancelButton);
		
		contents.add(buttonPanel);
			
		pack();
	}
	
	private String getFilePrefix() {
		return kFileStart[fileType] + newNameField.getText();
	}
}
