package cast.other;


public class DataReference {
	private String name;
	private String source;
	private PageReference[] pageReferences;
	private int bestPageIndex = 0;
	private int bestAppletInPage;
	private boolean selectedForExport = false;

	public DataReference(String name, PageReference[] pageReferences, String source) {
		this.name = name;
		this.pageReferences = pageReferences;
		this.source = source;

		for (int i=0 ; i<pageReferences.length ; i++)
			if (pageReferences[i].noOfApplets() > 0) {	
				bestPageIndex = i;
				bestAppletInPage = 0;
				break;
			}
	}

/*
	public void setBestApplet(String dir, String filePrefix, int appletInPage) {
		for (int i=0 ; i<pageReferences.length ; i++)
			if (pageReferences[i].matches(dir, filePrefix)) {
				if (appletInPage >= 0 && appletInPage < pageReferences[i].noOfApplets()) {
					setBestApplet(pageReferences[i], appletInPage);
					return;
				}
			}
		System.out.println("Cannot set \"best\" applet for dataset \"" + name + "\" to index " + appletInPage
				+ " in (" + bestPage.getDir() + ", " + bestPage.getFilePrefix() + ")");
	}
*/
	
	public void setBestApplet(PageReference p, int appletInPage) {
		for (int i=0 ; i<pageReferences.length ; i++)
			if (p == pageReferences[i])
				bestPageIndex = i;
		bestAppletInPage = appletInPage;
	}
	
	public void setBestApplet(int appletIndex, int appletInPage) {
		bestPageIndex = appletIndex;
		bestAppletInPage = appletInPage;
	}
	
	public int noOfApplets() {
		int n = 0;
		for (int i=0 ; i<pageReferences.length ; i++)
			n += pageReferences[i].noOfApplets();
		return n;
	}
	
	public String getName() {
		return name;
	}
	
	public void selectForExport(boolean selected) {
		selectedForExport = selected;
	}
	
	public boolean isSelectedForExport() {
		return selectedForExport;
	}
	
	public int getBestPageIndex() {
		return bestPageIndex;
	}
	
	public PageReference getBestPage() {
		return (pageReferences != null && pageReferences.length > bestPageIndex) ? pageReferences[bestPageIndex] : null;
	}
	
	public int getBestAppletInPage() {
		return bestAppletInPage;
	}
	
	public PageReference[] getPages() {
		return pageReferences;
	}
	
	public String getSource() {
		return source;
	}
}