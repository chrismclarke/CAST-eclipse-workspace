package cast.server;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import cast.bookManager.*;
import cast.core.*;
import cast.utils.*;


public class VideoDownloadFrame extends JFrame {
	static final public Color kBackgroundColor = new Color(0x666666);
	static final public Color kDarkBlue = new Color(0x000066);
	
	private JButton downloadButton, cancelButton;
	private CastProgressBar sectionProgressBar, videoProgressBar;
	private JLabel videoCountLabel;
	private JLabel cancelLabel;
	
	private VideosDownloadTask downloadTask = null;
	
	private int noOfSections, currentSectionIndex, currentVideoIndex, totalVideos;
	
	public VideoDownloadFrame(String[] bookName, final File coreDir, final JButton callingButton,
																																		final JFrame callingFrame) {
		super("Download videos");
		
		JPanel contentPanel = new JPanel();
		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		contentPanel.setBorder(padding);
		setContentPane(contentPanel);

		boolean[] bookHasVideos = new boolean[bookName.length];
		for (int i=0 ; i<bookName.length ; i++) {
			CastEbook theEbook = new CastEbook(coreDir, bookName[i], false);
			theEbook.setupDom();
			bookHasVideos[i] = theEbook.hasVideos();
		}
		
		setLayout(new BorderLayout(0, 20));
		
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				JLabel bookLabel = new JLabel("E-book with videos: ", JLabel.RIGHT);
				final JComboBox bookChoice = new JComboBox();
				for (int i=0 ; i<bookName.length ; i++)
					if (bookHasVideos[i])
						bookChoice.addItem(bookName[i]);
				bookLabel.setLabelFor(bookChoice);
				
			topPanel.add(bookLabel);
			topPanel.add(bookChoice);
				
				downloadButton = new JButton("Download videos");
				downloadButton.addActionListener(new ActionListener() {
																						public void actionPerformed(ActionEvent e) {
																							downloadButton.setEnabled(false);
																							String bookName = (String) bookChoice.getSelectedItem();
																							downloadVideosForBook(coreDir, bookName);
																							cancelButton.setEnabled(true);
																							cancelLabel.setForeground(getBackground());		//	to hide it
																						}
																				});
			topPanel.add(downloadButton);
		
		add("North", topPanel);
			
			JPanel reportPanel = new JPanel();
			reportPanel.setLayout(new BorderLayout(0, 20));
			
				JPanel progressPanel = new JPanel();
				progressPanel.setLayout(new GridLayout(0, 1, 0, 20));
				
					sectionProgressBar = new CastProgressBar("Section progress");
					
				progressPanel.add(sectionProgressBar);
				
					videoProgressBar = new CastProgressBar("Video progress");
					
				progressPanel.add(videoProgressBar);
					
			reportPanel.add("Center", progressPanel);
				
				videoCountLabel = new JLabel("Videos downloaded: 0", JLabel.CENTER);
				videoCountLabel.setForeground(kDarkBlue);
				Font labelFont = videoCountLabel.getFont();
				Font boldLabelFont = new Font(labelFont.getName(), Font.BOLD, labelFont.getSize());
				videoCountLabel.setFont(boldLabelFont);
				
			reportPanel.add("South", videoCountLabel);
			
		add("Center", reportPanel);
		
			JPanel cancelPanel = new JPanel();
			cancelPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0)); 
			
				cancelButton = new JButton("Cancel download");
				cancelButton.addActionListener(new ActionListener() {
																						public void actionPerformed(ActionEvent e) {
																							cancelLabel.setText("Cancelling...");
																							downloadTask.cancel(true);
																							downloadButton.setEnabled(true);
																							cancelButton.setEnabled(false);
																							cancelLabel.setForeground(Color.blue);
																						}
																				});
				cancelButton.setEnabled(false);
				
			cancelPanel.add(cancelButton);
				
				cancelLabel = new JLabel("Cancelling...", JLabel.LEFT);
				cancelLabel.setForeground(getBackground());				//	to make it invisible but get it laid out large enough
				cancelLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
				
			cancelPanel.add(cancelLabel);
		
		add("South", cancelPanel);
		
		addWindowListener(new WindowAdapter() {
										public void windowClosing(WindowEvent e) {
											if (downloadTask == null) {
												if (callingFrame instanceof BookChoiceWindow)
													((BookChoiceWindow)callingFrame).reenable(callingButton);
												else
													((AdvancedWindow)callingFrame).reenable(callingButton);
												dispose();
											}
											else
												JOptionPane.showMessageDialog(VideoDownloadFrame.this, "Files are downloading. Cancel the download before closing this window.",
																											"Error!", JOptionPane.ERROR_MESSAGE);
										}
									});
		setResizable(false);
		pack();
		setVisible(true);
		toFront();
	}
	
	
	private void downloadVideosForBook(File coreDir, String homeBookName) {
		downloadTask = new VideosDownloadTask(coreDir, homeBookName, this);
		downloadTask.execute();
	}
	
	public void setFinished() {
		sectionProgressBar.setValue(noOfSections, "Finished");
		videoProgressBar.clear();
		downloadButton.setEnabled(true);
		videoCountLabel.setText("Videos downloaded: " + totalVideos);
		cancelButton.setEnabled(false);
		downloadTask = null;
	}
	
	public void setCancelled() {
		downloadButton.setEnabled(true);
		cancelButton.setEnabled(false);
		downloadTask = null;
		cancelLabel.setText("Cancelled");
	}
	
	public void updateForStart(int noOfSections) {
		this.noOfSections = noOfSections;
		currentSectionIndex = -1;
		
		sectionProgressBar.initialise(noOfSections, "Finding videos in book...");
		videoProgressBar.initialise(1, "");
		totalVideos = -1;
		videoCountLabel.setText("Videos downloaded: 0");
	}
	
	public void updateForNewSection(String sectionName, int noOfVideos) {
		currentSectionIndex ++;
		
		sectionProgressBar.setValue(currentSectionIndex, sectionName);
		currentVideoIndex = -1;
		videoProgressBar.initialise(noOfVideos, "");
	}
	
	public void updateForNewVideo(String videoName) {
		currentVideoIndex ++;
		totalVideos ++;
		videoProgressBar.setValue(currentVideoIndex, videoName);
		videoCountLabel.setText("Videos downloaded: " + totalVideos);
	}
	
	public void updateForNewFile(String videoNameAndType) {
		videoProgressBar.setValue(videoNameAndType);
	}
	
}
