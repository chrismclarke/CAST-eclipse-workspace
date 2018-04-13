package cast.index;


public class BookTree {
	static final public int CHAPTER = 0;
	static final public int SECTION = 1;
	static final public int PAGE = 2;
	
	public String dir, filePrefix, title;
	public String summaryDir = null, summaryFilePrefix = null;
	public String videoDir = null, videoFilePrefix = null;
	public String description = null;
	public BookTree[] children = null;
	
	protected BookTree(String dir, String filePrefix, String title, String description, String summaryDir,
																					String summaryFilePrefix, String videoDir, String videoFilePrefix) {
		this.dir = dir;			//	dir is a path relative to the core folder. It is null for a Part
		this.filePrefix = filePrefix;		//	null for Part
		this.title = title;
		this.description = description;
		this.summaryDir = summaryDir;
		this.summaryFilePrefix = summaryFilePrefix;
		this.videoDir = videoDir;
		this.videoFilePrefix = videoFilePrefix;
	}
	
	protected BookTree(String dir, String filePrefix, String title) {
		this(dir, filePrefix, title, null, null, null, null, null);
	}
	
	protected void addItem(int level, String dir, String filePrefix, String title, String description,
															String summaryDir, String summaryFilePrefix, String videoDir, String videoFilePrefix) {
		if (level > 0)
			children[children.length - 1].addItem(level - 1, dir, filePrefix, title, description,
																											summaryDir, summaryFilePrefix, videoDir, videoFilePrefix);
		else {
			if (children == null)
				children = new BookTree[1];
			else {
				BookTree[] temp = children;
				children = new BookTree[temp.length + 1];
				System.arraycopy(temp, 0, children, 0, temp.length);
			}
			children[children.length - 1] = new BookTree(dir, filePrefix, title, description, summaryDir,
																																		summaryFilePrefix, videoDir, videoFilePrefix);
		}
	}
	
	protected void addItem(int level, String dir, String filePrefix, String title) {
		addItem(level, dir, filePrefix, title, null, null, null, null, null);
	}
}