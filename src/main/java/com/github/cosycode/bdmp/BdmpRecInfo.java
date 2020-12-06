package com.github.cosycode.bdmp;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * <b>Description : </b> 用于存放像素图片识别后的信息
 *
 * @author CPF
 * @date 2020/11/10
 **/
@Data
@Slf4j
public class BdmpRecInfo {

    private BdmpHeader bdmpHeader;

    private int[] byteModal;
    /**
     * x 像素区域列表
     */
    private int[] xArr;

    /**
     * y 像素区域列表
     */
    private int[] yArr;

    private byte[] fileContent;

    private int pixelTypeCnt;

    private int bitCnt;

    private int contentLength;

    /**
     * 检查文件MD5值
     */
    public boolean check() {
        String md5Hex = BdmpUtils.encrypt2ToMd5(fileContent);
        String md5 = bdmpHeader.getContentMd5();
        log.info("像素head信息MD5值: {}", md5);
        log.info("文件解析内容MD5值: {}", md5);
        return md5.equalsIgnoreCase(md5Hex);
    }

    @Override
    public String toString() {
        return "BdmpRecInfo{" +
                "bdmpHeader=" + bdmpHeader +
                ", pixelTypeCnt=" + pixelTypeCnt +
                ", bitCnt=" + bitCnt +
                ", contentLength=" + contentLength +
                '}';
    }
}
