package net.cb.cb.library.bean;

/***
 * @author jyj
 * @date 2016/12/20
 */
public class UpBean {
    private Integer fileSize;
    private String fileOldName;
    private String filePath;
    private String suffix;
    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }
    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileOldName(String fileOldName) {
        this.fileOldName = fileOldName;
    }
    public String getFileOldName() {
        return fileOldName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public String getFilePath() {
        return filePath;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    public String getSuffix() {
        return suffix;
    }
}
