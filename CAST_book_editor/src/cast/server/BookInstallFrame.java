package cast.server;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import cast.core.*;
import cast.utils.*;


public class BookInstallFrame extends JFrame {
	static final private Color kLineColor = new Color(0x990000);
	
	private BookDownloadPanel downloadBookPanel;
	
	public BookInstallFrame(final File coreDir, DatesHash localBooksHash, DatesHash serverBooksHash,
									StringsHash bookDescriptionsHash, final boolean canDownload, final JButton callingButton,
									final JFrame callingFrame) {
		super("Download book");
		
		JPanel contentPanel = new JPanel();
		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		contentPanel.setBorder(padding);
		setContentPane(contentPanel);
		
		
		setLayout(new BorderLayout(0, 20));
			
		if (canDownload) {
			downloadBookPanel = new BookDownloadPanel(localBooksHash, serverBooksHash, bookDescriptionsHash,
																		"No new e-books on server", "Download e-book", coreDir, this);
			add("North", downloadBookPanel);
			
			Separator line = new Separator(1.0, 3);
			line.setForeground(kLineColor);
			add("Center", line);
		}
			
			final ZipBookInstallPanel zipBookInstallPanel = new ZipBookInstallPanel(localBooksHash, coreDir, this);
		add("South", zipBookInstallPanel);
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
										public void windowClosing(WindowEvent e) {
											boolean finished = true;
											if (canDownload && downloadBookPanel.isWaiting())
												finished = false;
											
											if (zipBookInstallPanel.isWaiting())
												finished = false;
											
											if (finished) {
												if (callingFrame instanceof BookChoiceWindow) {
													((BookChoiceWindow)callingFrame).reenable(callingButton);
													((BookChoiceWindow)callingFrame).updateBookList();
												}
												else {
//													((AdvancedWindow)callingFrame).reenable(callingButton);
//													((AdvancedWindow)callingFrame).updateBookList();
												}
												dispose();
											}
											else
												JOptionPane.showMessageDialog(BookInstallFrame.this, "Files are downloading. Cancel the download before closing this window.",
																											"Error!", JOptionPane.ERROR_MESSAGE);
										}
									});
		
		setResizable(false);
		pack();
		setVisible(true);
		toFront();
	}
	
}
