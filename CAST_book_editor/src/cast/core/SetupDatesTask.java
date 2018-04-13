package cast.core;

import java.awt.*;
import java.io.*;
import java.net.*;

import javax.swing.*;

//import org.w3c.dom.*;
//import com.apple.eawt.*;

//import utils.*;
//import bookManager.*;
//import exercise.*;
//import core.*;
import cast.server.*;
import cast.utils.*;
//import cast.other.*;

	
public class SetupDatesTask extends SwingWorker<Object,String> {
	static final public String getLanguageCode(String languageName) {
		return languageName.equals("german") ? "de" : languageName.equals("french") ? "fr"
																: languageName.equals("spanish") ? "es" : languageName.equals("chinese") ? "zh" : null;
																											//	returns null for the English version
	}
	
	private JFrame mainWindow;
	private File castDir;
	
//	private String collection;					//	for master copy with several collections, this is "public"
//	private String language;						//	null for the English version
	private AllDates localDates, serverDates;
	private StringsHash bookDescriptionsHash;
	private boolean noServerAccess, castNeedsUpdate;
	
	private JDialog waitMessage;
	private CastProgressBar progressBar;
	private int progressStage = 0;
	
	public SetupDatesTask(File castDir, JFrame mainWindow) {
		this.castDir = castDir;
		this.mainWindow = mainWindow;
		waitMessage = showWaitMessage();
	}
	
	public Object doInBackground() {
		String collection = getCollectionName();					//	for master copy with several collections, this is "public"
		String language = getLanguageCode(collection);
		
		localDates = new AllDates(castDir, collection);
		serverDates = new AllDates(collection, language, this);
		
		noServerAccess = serverDates.getReadFailed();
		castNeedsUpdate = localDates.needsUpdate(serverDates);
		try {
			publish("Book descriptions on server");
			bookDescriptionsHash = new StringsHash(new URL("http://" + Options.kCastDownloadUrl
																						+ "/core/dates/" + CoreCopyTask.kBookDescriptionsFileName));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void done() {
		waitMessage.dispose();
		if (mainWindow instanceof BookChoiceWindow)
			((BookChoiceWindow)mainWindow).finishSetup(localDates, serverDates, bookDescriptionsHash, noServerAccess, castNeedsUpdate);
	}

	protected void process(final java.util.List<String> chunks) {
		for (final String status : chunks) {
			progressStage ++;
			progressBar.setValue(progressStage, status);
		}
	}
	
	public void noteNextStage(String s) {
		publish(s);
	}

	private JDialog showWaitMessage() {
		JDialog dialog = new JDialog((Frame)null, "Wait!");
//			dialog.setContentPane(optionPane);
		dialog.setLayout(new BorderLayout(0, 16));
		
			JOptionPane optionPane = new JOptionPane("Finding whether CAST is up to date...", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
		dialog.add("North", optionPane);
		
			progressBar = new CastProgressBar("Checking...");
			progressBar.initialise(5, "Dates in local CAST");
		dialog.add("Center", progressBar);
		
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setResizable(false);
		dialog.pack();
		dialog.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)/2 - dialog.getWidth()/2, (Toolkit.getDefaultToolkit().getScreenSize().height)/2 - dialog.getHeight()/2);
		
		dialog.setVisible(true);
		return dialog;
	}
	
	private String getCollectionName() {
//		File coreDir = new File(castDir, "core");
		File[] collectionFiles = castDir.listFiles( new FilenameFilter() {
																								public boolean accept(File dir, String name) {
																									return name.startsWith("collection_");
																								}
																						});
		if (collectionFiles.length == 1) {
			String fileName = collectionFiles[0].getName();
			return fileName.substring(11, fileName.length() - 5);
		}
		else
			return "public";
	}
}