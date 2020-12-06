package com.github.cosycode.bdmp;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

/**
 * <b>Description : </b> Pixel 图片生成信息对象, 在生成信息时传入此对象.
 *
 * @author CPF
 * @date 2020/11/10
 **/
@Slf4j
@Getter
@ToString
public class BdmpGeneInfo {

    /**
     * 图片宽度
     */
    private final int imageWidth;
    /**
     * 图片高度
     */
    private final int imageHeight;
    /**
     * 绘制区域
     */
    private final Rectangle drawArea;
    /**
     * 像素bit基数
     */
    private final int bitCnt;
    /**
     * 绘制区域高度
     */
    private final int pointXStart;
    /**
     * 像素X尾部
     */
    private final int pointXEnd;
    /**
     * 像素Y起始
     */
    private final int pointYStart;
    /**
     * 像素Y尾部
     */
    private final int pointYEnd;
    private BdmpGeneConfig param;
    private BdmpSource source;
    /**
     * 文件头
     */
    private BdmpHeader header;

    public BdmpGeneInfo(BdmpGeneConfig param, BdmpSource source) throws IOException {
        this.param = param;
        this.source = source;
        // check
        Objects.requireNonNull(param, "生成参数不能为空");
        Objects.requireNonNull(source, "数据源不能为空");
        source.checkWithThrow();
        param.checkWithThrow();
        // 取出数据
        final Color[] mappingColor = param.getMappingColor();
        final String name = source.getName();
        final byte[] dataByte = source.getContent();

        // 像素基数数量
        bitCnt = (int) (Math.log(mappingColor.length) / Math.log(2));
        // 初始化文件头信息
        header = new BdmpHeader();
        header.setType(source.getSourceType().name());
        header.setTag(name);
        header.setContentLength(dataByte.length);
        header.setContentMd5(BdmpUtils.encrypt2ToMd5(dataByte));
        final String json = header.toJson();
        log.info("head\t" + json);
        final byte[] headBytes = json.getBytes();

        // 像素总个数 ==> 前8个像素:这是个像素图片 + 映射颜色的像素 + (一行像素数量:4, 头长度信息:4, 数据头, 数据内容)
        int pxTotalSize = 8 + mappingColor.length + (4 + 4 + headBytes.length + dataByte.length) * (8 / bitCnt);
        // 绘制区域宽度
        int areaWidth = param.getRowPixelCnt() * param.getPixelSideWidth();
        // 绘制区域高度
        int areaHeight = (int) Math.ceil((double) pxTotalSize / param.getRowPixelCnt()) * param.getPixelSideHeight();
        // pic 长度 = 边缘长度 + 定位区长度
        final int[] marginLen = param.getMarginLen();
        // 图片宽度和高度
        imageWidth = marginLen[2] + param.getPixelSideWidth() * 2 + areaWidth + marginLen[1];
        imageHeight = marginLen[0] + param.getPixelSideHeight() * 2 + areaHeight + marginLen[3];
        // 绘制区域
        drawArea = new Rectangle(marginLen[2] + param.getPixelSideWidth(), marginLen[0] + param.getPixelSideHeight(), areaWidth, areaHeight);
        // 其它参数
        pointXStart = drawArea.x;
        pointXEnd = pointXStart + (drawArea.width / param.getPixelSideWidth() - 1) * param.getPixelSideWidth();
        pointYStart = drawArea.y;
        pointYEnd = pointYStart + (drawArea.height / param.getPixelSideHeight() - 1) * param.getPixelSideHeight();
    }

}
