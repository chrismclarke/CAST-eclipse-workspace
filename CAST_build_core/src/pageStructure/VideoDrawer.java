package pageStructure;

import java.awt.*;
import java.util.regex.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;

/*
import javafx.application.*;
import javafx.embed.swing.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.scene.effect.*;
import javafx.scene.media.*;
*/

import javafx.application.*;
import javafx.embed.swing.*;
import javafx.scene.*;
import javafx.scene.web.*;

import ebook.*;
import ebookStructure.*;


public class VideoDrawer extends CoreDrawer {
	static final public String kStartHtmlString1 = "<!DOCTYPE HTML>\n<html>\n<head>\n"
																+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
																+ "<style>html, body {margin:0px; padding:0px; overflow:hidden}</style>\n";
	static final public String kStartHtmlString2 = "</head><body>\n\n";
																
	static final public String kEndHtmlString = "\n\n</body>\n</html>";

	static final private String kVideoParamPattern = "book=\"(.*?)\".*"
																										+ "section=\"(.*?)\".*"
																										+ "videoName=\"(.*?)\".*"
																										+ "width=\"(.*?)\".*"
																										+ "height=\"(.*?)\"";
	
	static final private String kBlankHtml = "<!DOCTYPE HTML>\n<html>\n<head>\n"
																+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
																+ "</head><body></body></html>\n";;
	
	private int width, height;
	private String videoUrlBase = null;
	private File videoTemplateFile;
	private String controllerUrlBase = null;
	
	private JFXPanel videoPanel;          // The JavaFX components
	
	public VideoDrawer(String htmlString, BookFrame theBookFrame) {
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
			videoTemplateFile = new File(theEbook.getCoreDir(), "java/videos/videoTemplate.html");
			try {
				File javaVideoDir = new File(theEbook.getCoreDir(), "java/videos");
				controllerUrlBase = new File(javaVideoDir.getCanonicalPath()).toURI().toURL().toString();
				if (controllerUrlBase.endsWith("/"))
					controllerUrlBase = controllerUrlBase.substring(0, controllerUrlBase.length() - 1);
//				controllerUrlBase = "file://" + javaVideoDir.getCanonicalPath();
			} catch (IOException e) {
			}
		}
	}
	
	
	private String pageHtml = null;
	
	public JPanel createPanel() {
		JPanel thePanel = new JPanel();
		thePanel.addAncestorListener(new AncestorListener() {
														public void ancestorAdded(AncestorEvent e) {};
														public void ancestorMoved(AncestorEvent e) {};
														public void ancestorRemoved(AncestorEvent e) {
															stopVideo();		//	otherwise the video seems to keep playing even after changing the page
														};
													});
		thePanel.setOpaque(false);
		thePanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
		
		final int scaledWidth = scaledSize(width);
		final int scaledHeight = scaledSize(height);
		
		pageHtml = HtmlHelper.getFileAsString(videoTemplateFile);
		pageHtml = pageHtml.replaceAll("##videoPath##", videoUrlBase);
		pageHtml = pageHtml.replaceAll("##controllerPath##", controllerUrlBase);
		pageHtml = pageHtml.replace("##width##", String.valueOf(scaledWidth));
		pageHtml = pageHtml.replace("##height##", String.valueOf(scaledHeight));
		
//		System.out.println(pageHtml);
		
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		videoPanel = new JFXPanel() {
										public Dimension getMinimumSize() {
											return new Dimension(Math.max(scaledWidth, 455), scaledHeight + 45);
										}
										
										public Dimension getPreferredSize() {
											return new Dimension(Math.max(scaledWidth, 455), scaledHeight + 45);
										}
									};
		videoPanel.setOpaque(false);
		thePanel.add(videoPanel);
		
		Platform.runLater(new Runnable() {
			public void run() {
				initFX(videoPanel);
			}
		});
		Platform.setImplicitExit(false);
		
		return thePanel;
	}
	
	public void stopVideo() {
		Platform.runLater(new Runnable() {
			public void run() {
				clearVideo();
			}
		});
	}
	
	
//**************************************
//******	method invoked on JavaFX thread
//**************************************

	private WebEngine engine = null;
	
	private void initFX(JFXPanel videoPanel) {
							// Fill the JavaFX panel
		WebView view = new WebView();
		engine = view.getEngine();
		engine.loadContent(pageHtml);
		
    Scene scene = new Scene(view, videoPanel.getPreferredSize().width, videoPanel.getPreferredSize().height);
    videoPanel.setScene(scene);
	}
	
	private void clearVideo() {
		engine.executeScript("vid.pause();");
		engine.loadContent(kBlankHtml);
	}
}
