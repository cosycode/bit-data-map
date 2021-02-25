package com.github.cosycode.bdmp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

/**
 * <b>Description : </b> 二进制图片配置类, 用于作为生成 bit-data-map 时的配置依据
 * <p>
 * <b>created in </b> 2020/5/19
 *
 * @author CPF
 * @since 1.0
 */
@ToString
public class BdmpGeneConfig {

    /**
     * 代表像素图片 8 * 4
     */
    @Getter
    private final int type = 0;
    /**
     * 版本号: 8 * 4
     */
    @Getter
    private final int version = 1;
    /**
     * 版本号: 8 * 4
     */
    @Getter
    private final long versionTime = 1589939253407L;
    /**
     * 一行像素数量
     */
    @Getter
    @Setter
    private int rowPixelCnt;
    /**
     * 点阵像素宽度
     */
    @Getter
    @Setter
    private int pixelSideWidth;
    /**
     * 点阵像素高度
     */
    @Getter
    @Setter
    private int pixelSideHeight;
    /**
     * 映射颜色
     */
    @Getter
    @Setter
    private Color[] mappingColor;

    /**
     * 上右下左的边缘宽度(灰色)
     */
    @Getter
    private int[] marginLen;

    public void setMargin(int margin) {
        setMargin(margin, margin, margin, margin);
    }

    public void setMargin(int top, int right, int bottom, int left) {
        marginLen = new int[]{top, right, bottom, left};
    }

    public void checkWithThrow() {
        Objects.requireNonNull(marginLen, "marginLen cannot be null");
        BdmpUtils.isTrue(rowPixelCnt > 0, "margin require > 0: %s", rowPixelCnt);
        BdmpUtils.isTrue(pixelSideWidth > 0, "the pixelSideWidth:%s require > 0", pixelSideWidth);
        BdmpUtils.isTrue(pixelSideHeight > 0, "the pixelSideHeight:%s require > 0", pixelSideHeight);
        Objects.requireNonNull(mappingColor, "mappingColor cannot be null");

        int bitCnt = (int) (Math.log(mappingColor.length) / Math.log(2));
        BdmpUtils.isTrue(((int) Math.pow(2, bitCnt)) == mappingColor.length, "marginLen should be a power of 2");
        BdmpUtils.isTrue(Arrays.stream(mappingColor).noneMatch(Objects::isNull), "mappingColor中不能为空");
    }

}
