package com.github.cosycode.bdmp;

import com.google.gson.Gson;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <b>Description : </b> 头实体信息
 *
 * @author CPF
 * Date: 2020/5/21 14:17
 */
@Data
public class BdmpHeader implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 标记类型 {@link BdmpSource.SourceType}
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

    public static BdmpHeader fromJson(String json) {
        return new Gson().fromJson(json, BdmpHeader.class);
    }

    public String toJson() {
        final String format = "{\"version\":%s,\"versionTime\":%s,\"enTime\":%s,\"type\":\"%s\",\"tag\":\"%s\",\"contentLength\":%s,\"contentMd5\":\"%s\"}";
        return String.format(format, version, versionTime, enTime, type, tag, contentLength, contentMd5);
    }

}