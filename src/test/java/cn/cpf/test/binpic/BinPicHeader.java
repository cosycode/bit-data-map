package cn.cpf.test.binpic;

import java.io.Serializable;
import java.util.Date;

/**
 * <b>Description : </b> 头实体信息
 *
 * @author CPF
 * Date: 2020/5/21 14:17
 */
public class BinPicHeader implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TYPE_FILE = "TYPE_FILE";
    public static final String TYPE_CONTENT = "TYPE_CONTENT";
    public static final String TYPE_CLIPBOARD = "TYPE_CLIPBOARD";

    /**
     * 版本号: 8 * 4
     */
    private int version = 1;
    /**
     * 版本号: 8 * 4
     */
    private long versionTime = 1589939253407L;
    /**
     * 加压时间
     */
    private long enTime = new Date().getTime();
    /**
     * 标记类型
     */
    private String type;
    /**
     * 文件名
     */
    private String tag;
    /**
     * 文件内容长度  8 * 8
     */
    private long contentLength;
    /**
     * MD5值
     */
    private String contentMd5;

    public String toJson() {
        final String format = "{\"version\":%s,\"versionTime\":%s,\"enTime\":%s,\"type\":\"%s\",\"tag\":\"%s\",\"contentLength\":%s,\"contentMd5\":\"%s\"}";
        return String.format(format, version, versionTime, enTime, type, tag, contentLength, contentMd5);
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getVersionTime() {
        return versionTime;
    }

    public void setVersionTime(long versionTime) {
        this.versionTime = versionTime;
    }

    public long getEnTime() {
        return enTime;
    }

    public void setEnTime(long enTime) {
        this.enTime = enTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentMd5() {
        return contentMd5;
    }

    public void setContentMd5(String contentMd5) {
        this.contentMd5 = contentMd5;
    }
}