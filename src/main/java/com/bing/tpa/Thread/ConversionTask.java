package com.bing.tpa.Thread;

public class ConversionTask {
    private final String pptRootPath;
    private final String userName;
    private final String pptBaseName;
    private final int pageCount;

    public ConversionTask(String pptRootPath, String userName,
                          String pptBaseName, int pageCount) {
        this.pptRootPath = pptRootPath;
        this.userName = userName;
        this.pptBaseName = pptBaseName;
        this.pageCount = pageCount;
    }

    // Getters
    public String getPptRootPath() { return pptRootPath; }
    public String getUserName() { return userName; }
    public String getPptBaseName() { return pptBaseName; }
    public int getPageCount() { return pageCount; }
}
