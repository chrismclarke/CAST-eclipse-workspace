package pageStructure;

import java.awt.*;
import java.util.regex.*;
import java.io.*;

import javax.swing.*;

import javafx.application.*;
import javafx.embed.swing.*;
import javafx.scene.*;
import javafx.scene.media.*;

import ebook.*;
import ebookStructure.*;

/*
 * 		This class has similar functionality to the VideoDrawer class, but uses the JavaFX MediaPlayer rather than a WebView
 */

public class VideoDrawer_2 extends CoreDrawer {
	static final private String kVideoParamPattern = "book=\"(.*?)\".*"
																										+ "section=\"(.*?)\".*"
																										+ "videoName=\"(.*?)\".*"
																										+ "width=\"(.*?)\".*"
																										+ "height=\"(.*?)\"";
	static final private int kMinControllerHeight = 50;
	
	private int width, height;
	private String videoUrlBase = null;
	
	private JFXPanel videoPanel;          // The JavaFX components
	private MediaPlayer mediaPlayer;
	
	public VideoDrawer_2(String htmlString, BookFrame theBookFrame) {
		Pattern videoPattern = Pattern.compile(kVideoParamPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher videoMatcher = videoPattern.matcher(htmlString);
		if (videoMatcher.find()) {
			String bookName = videoMatcher.group(1);
			String sectionName = videoMatcher.group(2);
			String videoName = videoMatcher.group(3);
			width = Integer.parseInt(videoMatcher.group(4));
			height = Integer.parseInt(videoMatcher.group(5));
			
			CastEbook theEbook = theBookFrame.getEbook();
			File bookDir = theEbook.getBookDir(bookName);
			File videoDir = new File(bookDir, "videos");
			File sectionDir = new File(videoDir, sectionName);
			File videoFile = new File(sectionDir, videoName + ".mp4");
			if (videoFile.exists())
				try {
					String videoUrl = new File(videoFile.getCanonicalPath()).toURI().toURL().toString();
//					String path = videoFile.getCanonicalPath();
//					String videoUrl = "file://" + path;
					videoUrlBase = videoUrl.substring(0, videoUrl.length() - 4);		//	remove the ".mp4"
				} catch (IOException e) {
				}
			else {
				String serverUrl = theEbook.getServerUrl();
				if (serverUrl != null)
					videoUrlBase = serverUrl + "/core/" + bookName + "/videos/" + sectionName + "/" + videoName;
			}
		}
	}
	
	
	public JPanel createPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setOpaque(false);
		
		if (videoUrlBase != null) {
			thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
			videoPanel = new JFXPanel() {
											public void removeNotify() {
												super.removeNotify();
												Platform.runLater(new Runnable() {
													public void run() {
														stopVideo();
													}
												});
											}
											
											public Dimension getMinimumSize() {
												return new Dimension(Math.max(width, 500), height + kMinControllerHeight);
											}
											
											public Dimension getPreferredSize() {
												return new Dimension(Math.max(width, 500), height + kMinControllerHeight);
											}
										};
			videoPanel.setOpaque(false);
			thePanel.add(videoPanel);
			
			Platform.setImplicitExit(false);
			Platform.runLater(new Runnable() {
				public void run() {
					initFX(videoPanel);
				}
			});
			Platform.setImplicitExit(false);
		}
		else {
			thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			thePanel.setBackground(Color.red);
			thePanel.add(new JLabel("Error: reference to video badly formatted in HTML", JLabel.LEFT));
		}
		
		return thePanel;
	}
	
	
	public int getMinimumWidth() {
		return width;
	}
	
	
//**************************************
//******	methods invoked on JavaFX thread
//**************************************

	private void initFX(JFXPanel videoPanel) {
							// Fill the JavaFX panel
		Scene videoScene = createVideoScene();
		videoPanel.setScene(videoScene);
	}
	
	private void stopVideo() {
							// Stop video from playing
		mediaPlayer.stop();
		mediaPlayer.dispose();
	}
	

	private Scene createVideoScene() {
		Media media = new Media(videoUrlBase + ".mp4");
		mediaPlayer = new MediaPlayer(media);
		
		MediaControl root = new MediaControl(mediaPlayer);
		Scene scene = new Scene(root);
		return scene;
	}
}
