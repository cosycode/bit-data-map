package com.github.cosycode.bdmp;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * <b>Description : </b> 用于实际完成绘制点阵到png图片上面的类
 * <p>
 * <b>created in </b> 2020/11/18
 *
 * @author CPF
 * @since 1.0
 */
@Slf4j
public class PixelPngDrawer {
    /**
     * 像素宽度
     */
    private final int pxSideWidth;
    /**
     * 像素高度
     */
    private final int pxSideHeight;
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
    /**
     * 绘制像素颜色映射数组
     */
    private final Color[] mappingColor;
    /**
     * 绘图对象
     */
    private final Graphics2D g2;
    /**
     * 绘制图片对象
     */
    private final BufferedImage image;
    /**
     * 当前绘制 x坐标
     */
    private int x;
    /**
     * 当前绘制 y 坐标
     */
    private int y;
    /**
     * 写到第几个像素
     */
    private int num = 0;

    protected PixelPngDrawer(BdmpGeneInfo bdmpGeneInfo) {
        final BdmpGeneConfig param = bdmpGeneInfo.getParam();
        final int imageWidth = bdmpGeneInfo.getImageWidth();
        final int imageHeight = bdmpGeneInfo.getImageHeight();
        pointXStart = bdmpGeneInfo.getPointXStart();
        pointXEnd = bdmpGeneInfo.getPointXEnd();
        pointYStart = bdmpGeneInfo.getPointYStart();
        pointYEnd = bdmpGeneInfo.getPointYEnd();
        mappingColor = param.getMappingColor();
        pxSideWidth = param.getPixelSideWidth();
        pxSideHeight = param.getPixelSideHeight();
        //得到图片缓冲区, INT精确度达到一定,RGB三原色
        image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        //得到它的绘制环境(这张图片的笔)
        g2 = (Graphics2D) image.getGraphics();
    }

    public static BufferedImage geneRatePixelPng(BdmpGeneInfo bdmpGeneInfo) throws IOException {
        PixelPngDrawer pngDrawer = new PixelPngDrawer(bdmpGeneInfo);
        pngDrawer.drawBackground();
        pngDrawer.drawerPosition();
        pngDrawer.drawContent(bdmpGeneInfo);
        return pngDrawer.image;
    }

    /**
     * 绘制背景
     */
    protected void drawBackground() {
        g2.setColor(Color.gray);
        g2.fillRect(0, 0, image.getWidth(), image.getHeight());
    }

    /**
     * 绘制背景, 以及定位区,
     * 定位区为黑色白色像素相间交替围成的一个矩形.
     * 定位区左上角为黑色, 从左上角分别向下, 向右扩展, 右下角可能出现两个黑色像素或两个白色像素出现的情况.
     */
    protected void drawerPosition() {
        int xFrom = pointXStart - pxSideWidth;
        int xTo = pointXEnd + pxSideWidth;
        int yFrom = pointYStart - pxSideHeight;
        int yTo = pointYEnd + pxSideHeight;
        int xAdd;
        int yAdd;
        boolean colorFlag = false;
        final int[][] setting = {
                {-1, xFrom, xTo, yFrom, yFrom},
                {0, xTo, xTo, yFrom + pxSideHeight, yTo},
                {1, xFrom, xFrom, yFrom + pxSideHeight, yTo},
                {0, xFrom + pxSideWidth, xTo, yTo, yTo}
        };
        for (int[] arr : setting) {
            xFrom = arr[1];
            xTo = arr[2];
            yFrom = arr[3];
            yTo = arr[4];
            xAdd = xTo == xFrom ? 0 : pxSideWidth;
            yAdd = yTo == yFrom ? 0 : pxSideHeight;
            if (arr[0] > 0) {
                colorFlag = true;
            } else if (arr[0] < 0) {
                colorFlag = false;
            }
            for (int ix = xFrom, iy = yFrom; ix <= xTo && iy <= yTo; ix += xAdd, iy += yAdd) {
                if (colorFlag) {
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(Color.BLACK);
                }
                g2.fillRect(ix, iy, pxSideWidth, pxSideHeight);
                colorFlag = !colorFlag;
            }
        }
    }

    /**
     * 绘制图片内容
     */
    protected void drawContent(BdmpGeneInfo bdmpGeneInfo) throws IOException {
        final int bitCnt = bdmpGeneInfo.getBitCnt();
        // 8bit
        doDrawer(BdmpUtils.convertByte(2, new byte[]{(byte) bitCnt}, 1));
        // color draw
        for (Color color : mappingColor) {
            doDrawPixel(color);
        }
        // 行像素数量
        drawer(BdmpUtils.toBytes(bdmpGeneInfo.getParam().getRowPixelCnt()));
        // 文件头长度
        final byte[] bytes = bdmpGeneInfo.getHeader().toJson().getBytes(StandardCharsets.UTF_8);
        drawer(BdmpUtils.toBytes(bytes.length));
        // 文件头
        drawer(bytes);
        // 文件内容
        drawer(bdmpGeneInfo.getSource().getContent());
    }

    /**
     * 将 byte 数组绘制到图片上
     *
     * @param b 二进制数组
     */
    private void drawer(byte[] b) {
        drawer(b, b.length);
    }

    /**
     * 将 byte 从0开始len长度的数组绘制到图片上
     *
     * @param b   二进制数组
     * @param len 绘制二进制数组的长度
     */
    private void drawer(byte[] b, int len) {
        Objects.requireNonNull(mappingColor, "mappingColor must not be null");
        // 将byte数组转为 mappingColor 进制数组
        int[] clr = BdmpUtils.convertByte(mappingColor.length, b, len);
        doDrawer(clr);
    }

    /**
     * 绘制像素
     *
     * @param clr 绘制像素单位
     */
    private void doDrawer(int[] clr) {
        for (int aChar : clr) {
            doDrawPixel(mappingColor[aChar]);
        }
    }

    /**
     * 绘制像素
     *
     * @param color 像素颜色
     */
    private void doDrawPixel(Color color) {
        pointShift();
        // 移动至下一像素 x, y 位置
        // 绘制像素
        g2.setColor(color);
        g2.fillRect(x, y, pxSideWidth, pxSideHeight);
        // 绘制像素数目自增
        num++;
    }

    /**
     * 绘制像素坐标移动
     */
    private void pointShift() {
        // 初始化 x, y 坐标
        if (x <= 0 && y <= 0) {
            x = pointXStart;
            y = pointYStart;
            return;
        }
        // 坐标移动至下一像素
        if (x < pointXEnd) {
            x += pxSideWidth;
            return;
        }
        if (y < pointYEnd) {
            y += pxSideHeight;
            x = pointXStart;
            return;
        }
        throw new RuntimeException(String.format("draw pixel out of range ==> x: %s, y: %s, drawer: %s", x, y, toString()));
    }

}
