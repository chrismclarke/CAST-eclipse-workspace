package cast.bookManager;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.channels.*;

import javax.swing.*;

import cast.utils.*;


public class BookCreator extends JDialog {
	
	public static String createBook(Frame parent, String[] bookName, File coreDir, boolean duplicate) {
		BookCreator dialog = new BookCreator(parent, bookName, duplicate);

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
		
		String newName = null;
		if (dialog.ok) {
			newName = dialog.getShortBookName();
			if (duplicate) {
				String nameToDuplicate = dialog.getBookToDuplicate();
				File booksDir = new File(coreDir, "bk");
				File sourceBookDir = new File(booksDir, nameToDuplicate);
				File newBookDir = new File(booksDir, newName);
				try {
					newBookDir.mkdir();
					
					File oldSplashFile = new File(sourceBookDir, "book_splash.html");
					File newSplashFile = new File(newBookDir, "book_splash.html");
					deepCopyItem(oldSplashFile, newSplashFile);
					
					deepCopyItem(new File(sourceBookDir, "images"), new File(newBookDir, "images"));
					
					File xmlDir = new File(newBookDir, "xml");
					xmlDir.mkdir();
					
					File secDir = new File(newBookDir, "sec");
					secDir.mkdir();
					
					File oldXmlBook = new File(new File(sourceBookDir, "xml"), "book.xml");
					File newXmlBook = new File(xmlDir, "book.xml");
					deepCopyItem(oldXmlBook, newXmlBook);
					
					deepCopyItem(new File(sourceBookDir, "text"), new File(newBookDir, "text"));
					
					deepCopyItem(new File(sourceBookDir, "sec"), new File(newBookDir, "sec"));
					
					File sourceVideoDir = new File(sourceBookDir, "video");
					if (sourceVideoDir.exists())
						deepCopyItem(sourceVideoDir, new File(newBookDir, "video"));
				} catch (IOException e) {
					JOptionPane.showMessageDialog(parent, "Could not create book \"" + newName + "\".", "Error!", JOptionPane.ERROR_MESSAGE);
				}
			}
			else {
				File booksDir = new File(coreDir, "bk");
				File sourceBookDir = new File(booksDir, "general");
				File newBookDir = new File(booksDir, newName);
				try {
					newBookDir.mkdir();
					
					File oldSplashFile = new File(sourceBookDir, "splash_new.html");
					File newSplashFile = new File(newBookDir, "book_splash.html");
					deepCopyItem(oldSplashFile, newSplashFile);
					
					new File(sourceBookDir, "images").mkdir();
					
					File xmlDir = new File(newBookDir, "xml");
					xmlDir.mkdir();
					
					File oldXmlBook = new File(new File(sourceBookDir, "xml"), "book_new.xml");
					File newXmlBook = new File(new File(newBookDir, "xml"), "book.xml");
					deepCopyItem(oldXmlBook, newXmlBook);
					
					File textDir = new File(newBookDir, "text");
					textDir.mkdir();
					
					File secDir = new File(newBookDir, "sec");
					secDir.mkdir();
					
					File videoDir = new File(newBookDir, "video");
					videoDir.mkdir();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(parent, "Could not create book \"" + newName + "\".", "Error!", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		
		dialog.dispose();
		return newName;
	}
	
	static public void deepCopyItem(File sourceLocation , File targetLocation) throws IOException {
		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists())
				targetLocation.mkdir();

			String[] children = sourceLocation.list();
			for (int i=0; i<children.length; i++)
				deepCopyItem(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
		}
		else {
			@SuppressWarnings("resource")
			FileChannel inChannel = new FileInputStream(sourceLocation).getChannel();
			@SuppressWarnings("resource")
			FileChannel outChannel = new FileOutputStream(targetLocation).getChannel();
			inChannel.transferTo(0, inChannel.size(), outChannel);
			inChannel.close();
			outChannel.close();
		}
	}
	
	
	
	private boolean ok = false;
	
	private JTextField shortBookNameEdit;
	private JComboBox bookChoice;
	
	private BookCreator(Frame parent, final String[] bookName, boolean duplicate) {
		super(parent, duplicate ? "Duplicate e-book" : "New e-book", true);
		setLayout(new BorderLayout(0, 0));
		
		JPanel innerPanel = new JPanel() { public Insets getInsets() { return new Insets(10, 20, 10, 20); }};
		
		innerPanel.setLayout(new BorderLayout(0, 12));
		
			JPanel settingsPanel = new JPanel();
			settingsPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 20));
			
				if (duplicate) {
					JPanel duplicatePanel = new JPanel();
					duplicatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 10));
						JLabel duplicateLabel = new JLabel("What e-book do you want to duplicate?", JLabel.CENTER);
					duplicatePanel.add(duplicateLabel);
					
						bookChoice = new JComboBox(bookName);
						for (int i=0 ; i<bookName.length ; i++)
							if (bookName[i].equals("general"))
								bookChoice.setSelectedIndex(i);
					duplicatePanel.add(bookChoice);
					
					settingsPanel.add(duplicatePanel);
				}
				
				JPanel descriptionPanel = new JPanel();
				descriptionPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 0));
					JLabel description1 = new JLabel("Carefully pick a short name for the new e-book.");
				descriptionPanel.add(description1);
					JLabel description2 = new JLabel("This short name should have no spaces");
				descriptionPanel.add(description2);
					JLabel description3 = new JLabel("and you won't be able to change it later.");
				descriptionPanel.add(description3);
				
			settingsPanel.add(descriptionPanel);
			
				JPanel shortNamePanel = new JPanel();
				shortNamePanel.setLayout(new BorderLayout(0, 0));
					JLabel shortNameTitle = new JLabel("Short name for e-book:");
					shortBookNameEdit = new JTextField(12);
					shortNameTitle.setLabelFor(shortBookNameEdit);
				
				shortNamePanel.add("West", shortNameTitle);
				shortNamePanel.add("Center", shortBookNameEdit);
				
			settingsPanel.add(shortNamePanel);
				
				JPanel notePanel = new JPanel();
				notePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 0));
					JLabel note1 = new JLabel("The short name will be used in the names of some files");
				notePanel.add(note1);
					JLabel note2 = new JLabel("and in the URL for starting the e-book.");
				notePanel.add(note2);
				
			settingsPanel.add(notePanel);
			
		innerPanel.add("Center", settingsPanel);
		
		
			JPanel bottomPanel = new JPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			
				JButton saveButton = new JButton("Create new e-book");
				saveButton.addActionListener(new ActionListener() {
																				public void actionPerformed(ActionEvent e) {
																					String newName = getShortBookName();
																					if (newName.length() == 0) {
																						JOptionPane.showMessageDialog(BookCreator.this, "A short name must be given.", "Error!", JOptionPane.ERROR_MESSAGE);
																						return;
																					}
																					for (int i=0 ; i<bookName.length ; i++)
																						if (newName.equals(bookName[i])) {
																							JOptionPane.showMessageDialog(BookCreator.this, "The short name is the same as that\nof an existing e-book", "Error!", JOptionPane.ERROR_MESSAGE);
																							return;
																						}
																					ok = true;
																					setVisible(false);
																				}
																		});
			bottomPanel.add(saveButton);
			
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
																				public void actionPerformed(ActionEvent e) {
																					ok = false;
																					setVisible(false);
																				}
																		});
			bottomPanel.add(cancelButton);
			
		innerPanel.add("South", bottomPanel);
		
		add(innerPanel);
			
		pack();
	}
	
	public String getShortBookName() {
		return shortBookNameEdit.getText().replaceAll("\\s", "");
	}
	
	public String getBookToDuplicate() {
		if (bookChoice == null)
			return null;
		return (String)bookChoice.getSelectedItem();
	}
}
