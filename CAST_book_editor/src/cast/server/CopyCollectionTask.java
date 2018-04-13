package cast.server;

import java.io.*;
import java.util.*;

import javax.swing.*;

import cast.bookManager.*;
import cast.utils.*;
import cast.core.*;




public class CopyCollectionTask extends CoreCopyTask {
	static final private int kCopyStages = 3;
	
	private File castDestDir, coreDestDir, datesDestDir;
	
	public CopyCollectionTask(File castSourceDir, File castDestDir, String collection, HashSet<String> bookDirs,
																						JLabel progressStage, CastProgressBar uploadStageProgress,
																						CastProgressBar uploadItemProgress) {
		super(castSourceDir, collection, bookDirs, progressStage, uploadStageProgress, uploadItemProgress);
		uploadStageProgress.initialise(kCoreCopyStages + kCopyStages, "");
		
		this.castDestDir = castDestDir;
	}
	
	protected void copyToDestination() throws InterruptedException  {
		castDestDir.mkdir();
		File coreSourceDir = new File(castSourceDir, "core");
		coreDestDir = new File(castDestDir, "core");
		coreDestDir.mkdir();
//		File datesSourceDir = new File(coreSourceDir, "dates");
		datesDestDir = new File(coreDestDir, "dates");
		datesDestDir.mkdir();
		
		DatesHash thisCollectionDates = collectionDates.get(collection);
		publish(new CopyStatus("Copying core folders...", coreDates.size() + thisCollectionDates.size(), RESET_BAR));
		failIfInterrupted();
		
		copyFiles(coreDates, new File(datesDestDir, kCoreDatesFileName));
		createIndexFile();
		versionInfo.saveToFile(new File(coreDestDir, kReleasInfoFileName));
		copyOneFile(new File(castSourceDir, "README_" + collection + ".rtf"), new File(castDestDir, "README.rtf"));
		
		copyFiles(thisCollectionDates, new File(datesDestDir, kCollectionDatesPrefix + collection + ".text"));
		
		publish(new CopyStatus("Copying book folders...", bookDirs.size(), RESET_BAR));
		failIfInterrupted();
		
		copyBooks(coreSourceDir, coreDestDir, datesDestDir);
		
		publish(new CopyStatus("Copying section folders...", sectionNames.size(), RESET_BAR));
		failIfInterrupted();
		
		copySections(coreSourceDir, coreDestDir, datesDestDir);
		
		new DatesHash().saveToFile(new File(datesDestDir, CoreCopyTask.kVideoDatesFileName));
												//	dates file for videos should be empty since the copy contains no videos
		
//		copyOneFile(new File(datesSourceDir, kVersionFileName), new File(datesDestDir, kVersionFileName));
	}
	
	private void createIndexFile() {
		try {
			File fileDir = new File(castDestDir, "index.html");

			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), "UTF8"));

			out.write("<html>\n");
			out.write("<head>\n");
			out.write("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />\n");
			out.write("<title>Redirect</title>\n\n");
			out.write("<script src='init/indexScript.js'></script>\n\n");
			out.write("<script type='text/javascript' language='javascript'>\n");
			out.write("	parseSearchString('" + collection + "');\n");
			out.write("</script>\n\n");
			out.write("</head>\n\n");
			out.write("<body>\n");
			out.write("</body>\n");
			out.write("</html>");

			out.flush();
			out.close();
		} 
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void copyBooks(File coreSourceDir, File coreDestDir, File datesDestDir) throws InterruptedException {
		File booksSourceDir = new File(coreSourceDir, "bk");
		File booksDestDir = new File(coreDestDir, "bk");
		booksDestDir.mkdir();
		
		Enumeration<String> bookNames = bookDates.keys();
		while (bookNames.hasMoreElements()) {
			String bookName = bookNames.nextElement();
			publish(new CopyStatus(bookName, INCREMENT_BAR));
			failIfInterrupted();
			
			File bookSourceDir = new File(booksSourceDir, bookName);
			File bookDestDir = new File(booksDestDir, bookName);
			
			copyBookDir(bookSourceDir, bookDestDir);
			
			CastEbook destEbook = new CastEbook(coreDestDir, bookName, false);
			VideosDownloadTask.rememberVideosDownloaded(destEbook, false);
		}
		bookDates.saveToFile(new File(datesDestDir, kBookDatesFileName));
	}
	
	private void copySections(File coreSourceDir, File coreDestDir, File datesDestDir) throws InterruptedException {
		Enumeration<String> sectionNames = sectionDates.keys();
		while (sectionNames.hasMoreElements()) {
			String sectionPath = sectionNames.nextElement();
			publish(new CopyStatus(sectionPath, INCREMENT_BAR));
			failIfInterrupted();
			
			int slashIndex = sectionPath.indexOf("/");
			String languageDirName = sectionPath.substring(0, slashIndex);
			String sectionName = sectionPath.substring(slashIndex + 1);
			
			File folderSourceDir = new File(new File(coreSourceDir, languageDirName), sectionName);
			File languageDestDir = new File(coreDestDir, languageDirName);
			languageDestDir.mkdir();
			File folderDestDir = new File(languageDestDir, sectionName);
			
			copyRecursive(folderSourceDir, folderDestDir);
		}
		sectionDates.saveToFile(new File(datesDestDir, kSectionDatesFileName));
	}
	
	private void copyFiles(DatesHash datesHash, File outputDatesHashFile) throws InterruptedException {
		Enumeration<String> fileNames = datesHash.keys();
		while (fileNames.hasMoreElements()) {
			String fileName = fileNames.nextElement();
			publish(new CopyStatus(fileName, INCREMENT_BAR));
			failIfInterrupted();
			
			File source = parseFilePath(castSourceDir, fileName);
			File dest = parseFilePath(castDestDir, fileName);
			if (fileName.endsWith(DatesHash.kSystemAdviceFileName))
				copyAdviceFile(source, dest);
			else if (source.isFile())
				copyOneFile(source, dest);
			else
				copyRecursive(source, dest);
		}
		datesHash.saveToFile(outputDatesHashFile);
	}
	
	private File parseFilePath(File dest, String relativePath) {
		int slashIndex = relativePath.indexOf("/");
		if (slashIndex < 0 || slashIndex == relativePath.length() - 1)		//	file or directory
			return new File(dest, relativePath);
		else {
			File dir = new File(dest, relativePath.substring(0, slashIndex));
			if (!dir.exists())
				dir.mkdir();
			String fileName = relativePath.substring(slashIndex + 1);
			return parseFilePath(dir, fileName);
		}
	}
	
	private void copyAdviceFile(File source, File dest) {
		String indexFileString = HtmlHelper.getFileAsString(source, "UTF-8");
		indexFileString = indexFileString.replace("<script src=\"../releaseInfo.js\"></script>",
												"<script src=\"../releaseInfo.js\"></script>"
												+ "\n<script src=\"http://" + Options.kCastDownloadUrl + "/core/serverReleaseInfo.js\"></script>");
		
		try {
			PrintWriter out = new PrintWriter(dest);
			out.print(indexFileString);
			out.close();
		} catch (IOException e) {
			System.err.println("Could not write .../systemAdvice.html");
			e.printStackTrace();
		}
	}
	
	private void copyBookDir(File bookSourceDir, File bookDestDir) {		//	does not copy video folder
		bookDestDir.mkdir();
		File[] sourceFiles = bookSourceDir.listFiles(new FilenameFilter() {
																	public boolean accept(File dir, String name) {
																		return !name.startsWith(".");		//	not hidden files
																	}
																});
		for (int i=0 ; i<sourceFiles.length ; i++) {
			File sourceFile = sourceFiles[i];
			if (sourceFile.isFile()) {
				File destFile = new File(bookDestDir, sourceFile.getName());
				copyOneFile(sourceFile, destFile);
			}
		}
		
		for (int i=0 ; i<DatesHash.kBookFolders.length ; i++) {
			String folderName = DatesHash.kBookFolders[i];
			copyRecursive(new File(bookSourceDir, folderName), new File(bookDestDir, folderName));
		}
	}
}