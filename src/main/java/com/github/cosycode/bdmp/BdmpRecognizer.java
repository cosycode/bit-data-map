package com.github.cosycode.bdmp;

import com.github.cosycode.common.ext.bean.DoubleBean;
import com.github.cosycode.common.lang.WrongBranchException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <b>Description : </b> 解析二进制图片工具类
 * <p>
 * <b>created in </b> 2020/5/19
 *
 * @author CPF
 * @since 1.0
 */
@Slf4j
public class BdmpRecognizer {

    /**
     * 图片
     */
    @Setter
    private BufferedImage image;
    @Getter
    private PixelPngReader pixelReader;
    /**
     * 边界半径
     */
    private int radixBorder;

    /**
     * @param image 待识别的图片
     * @return 识别后的识别器对象
     */
    public static BdmpRecInfo resolver(BufferedImage image) {
        BdmpRecognizer recognizer = new BdmpRecognizer();
        recognizer.setImage(image);
        final boolean distinguish = recognizer.distinguish();
        if (!distinguish) {
            // 未识别出区域
            return null;
        }
        recognizer.pixelReader.readFileInfo();
        // 封装结果返回
        final PixelPngReader pixelReader = recognizer.getPixelReader();
        final BdmpRecInfo picRecInfo = new BdmpRecInfo();
        picRecInfo.setBdmpHeader(pixelReader.getBdmpHeader());
        picRecInfo.setByteModal(pixelReader.getByteModal());
        picRecInfo.setXArr(pixelReader.getXArr());
        picRecInfo.setYArr(pixelReader.getYArr());
        picRecInfo.setFileContent(pixelReader.getFileContent());
        picRecInfo.setBitCnt(pixelReader.getBitCnt());
        picRecInfo.setContentLength(pixelReader.getContentLength());
        return picRecInfo;
    }

    /**
     * 判断定位区像素是否是白色或黑色
     *
     * @param rgb rgb 值
     */
    public static boolean isBorderVal(int rgb) {
        return isBlack(rgb) || isWhite(rgb);
    }

    /**
     * 判断定位区像素是否是黑色
     *
     * @param rgb rgb 值
     */
    public static boolean isBlack(int rgb) {
        int n = 0;
        n += (rgb >>> 16 & 0xFF);
        n += (rgb >>> 8 & 0xFF);
        n += (rgb & 0xFF);
        return n < 60;
    }

    /**
     * 判断定位区像素是否是黑色
     *
     * @param rgb rgb 值
     */
    public static boolean isGray(int rgb) {
        int n = (rgb >>> 16 & 0xFF);
        if (0x60 > n || n > 0xa0) {
            return false;
        }
        n = (rgb >>> 8 & 0xFF);
        if (0x60 > n || n > 0xa0) {
            return false;
        }
        n = (rgb & 0xFF);
        return 0x60 <= n && n <= 0xa0;
    }

    /**
     * 判断定位区像素是否是白色
     *
     * @param rgb rgb 值
     */
    public static boolean isWhite(int rgb) {
        int n = 0;
        n += (rgb >>> 16 & 0xFF);
        n += (rgb >>> 8 & 0xFF);
        n += (rgb & 0xFF);
        return n > 705;
    }

    /**
     * 判断两个像素是否相近
     *
     * @param rgb1 整型rgb像素值1
     * @param rgb2 整型rgb像素值2
     * @return 两个像素是否相近
     */
    public static boolean isNearColor(int rgb1, int rgb2) {
        int rgb = rgb1 ^ rgb2;
        final int cnt = 0x10;
        if ((rgb >>> 16 & 0xFF) > cnt) {
            return false;
        }
        if ((rgb >>> 8 & 0xFF) > cnt) {
            return false;
        }
        return (rgb & 0xFF) <= cnt;
    }

    /**
     * 识别图片定位区
     */
    @SuppressWarnings({"java:S3776", "java:S135", "java:S1199", "java:S3518"})
    public boolean distinguish() {
        int maxR = Math.min(image.getHeight(), image.getWidth());
        /* 寻找四边, 斜线切入, 直到寻找到黑白色的像素 */
        // 斜边切入边距, 黑白框外层需要有一层灰色边, 因此 r 需要 >= 1
        for (int r = Math.max(1, radixBorder); r < maxR; r++) {
            int rgb = image.getRGB(r, r);
            // 如果(r, r) 为黑白框像素, 同时(r - 1, r - 1)为灰色像素
            if (isBorderVal(rgb) && isGray(image.getRGB(r - 1, r - 1))) {
                radixBorder = r;
                final Point leftTopPoint = findLeftTopPointFromSidePixel(r);
                if (leftTopPoint == null) {
                    continue;
                }
                final BdmpRecCngInfo info = recognizeDrawArea(leftTopPoint);
                if (info == null) {
                    continue;
                }
                final DoubleBean<int[], int[]> xyArr = findXYArr(info);
                if (xyArr == null) {
                    continue;
                }
                info.setXArr(xyArr.getO1());
                info.setXArr(xyArr.getO2());
                pixelReader = new PixelPngReader(image, xyArr.getO1(), xyArr.getO2());
                return true;
            }
        }
        return false;
    }

    /**
     * 通过斜边切入点找到该绘制区域(可能是绘制区域)的左上方的点
     * (r, r) 是当前切入的点
     *
     * @param r 斜边切入的边长
     * @return 该绘制区域(可能是绘制区域)的左上方的点
     */
    private Point findLeftTopPointFromSidePixel(int r) {
        // 通过斜边切入到当前点的上一个点, 在此应该是刚从边缘切向黑白边, 因此应该是灰色, 与灰色相近的点, 不应该和黑色和白色相近
        int rgbMinusOne = image.getRGB(r - 1, r - 1);
        // 判断(r,r)是否是横轴点还是纵轴点
        if (isNearColor(rgbMinusOne, image.getRGB(r, r - 1)) && isBorderVal(image.getRGB(r - 1, r))) {
            // 如果左边是 黑白框 点,
            int x = r - 2;
            while (isBorderVal(image.getRGB(x, r))) x--;
            return new Point(++x, r);
        } else if (isNearColor(rgbMinusOne, image.getRGB(r - 1, r)) && isBorderVal(image.getRGB(r, r - 1))) {
            int y = r - 2;
            while (isBorderVal(image.getRGB(r, y))) y--;
            return new Point(r, ++y);
        } else if (isNearColor(rgbMinusOne, image.getRGB(r - 1, r)) && isNearColor(rgbMinusOne, image.getRGB(r, r - 1))) {
            return new Point(r, r);
        }
        return null;
    }

    /**
     * 识别绘制区域
     * <p>
     * 从 leftTopPoint 出发, 向右, 向下, 找到黑白框边界
     * 判断边界是否围了一周,
     * 判断边界外一圈是否全是和rgb_1相近的像素
     *
     * @return 识别的绘制区域, 如果识别不出则返回null
     */
    @SuppressWarnings("java:S1659")
    private BdmpRecCngInfo recognizeDrawArea(@NonNull Point leftTopPoint) {
        // 图片高度
        int height = image.getHeight();
        // 图片宽度
        int width = image.getWidth();
        // 绘制左上角像素应该是黑色
        if (!isBlack(image.getRGB(leftTopPoint.x, leftTopPoint.y))) {
            return null;
        }
        int x, y, len;
        // →
        for (x = leftTopPoint.x + 1, y = leftTopPoint.y, len = width - 1; x < len && isBorderVal(image.getRGB(x, y)); x++)
            ;
        Point rightTop = new Point(x - 1, y);
        // →↓
        for (x = rightTop.x, y = rightTop.y, len = height - 1; y < len && isBorderVal(image.getRGB(x, y)); y++) ;
        Point rightBottom1 = new Point(x, y - 1);
        // ↓
        for (x = leftTopPoint.x, y = leftTopPoint.y + 1, len = height - 1; y < len && isBorderVal(image.getRGB(x, y)); y++)
            ;
        Point leftBottom = new Point(x, y - 1);
        // ↓→
        for (x = leftBottom.x, y = leftBottom.y, len = width - 1; x < len && isBorderVal(image.getRGB(x, y)); x++) ;
        Point rightBottom2 = new Point(x - 1, y);
        // 判断黑白像素从左上角向下, 向右延申查询有效区域, 是否能够在右下角合并一起
        if (!rightBottom1.equals(rightBottom2)) {
            return null;
        }
        /* 判断周围边缘全部是灰色 */
        final int borderWidth = rightTop.x - leftTopPoint.x + 2;
        final int borderHeight = leftBottom.y - leftTopPoint.y + 2;
        // → : 上边缘
        int[] rgb = image.getRGB(leftTopPoint.x - 1, leftTopPoint.y - 1, borderWidth, 1, null, 0, borderWidth);
        if (!Arrays.stream(rgb).allMatch(BdmpRecognizer::isGray)) {
            return null;
        }
        // →↓ : 右边缘
        rgb = image.getRGB(rightTop.x + 1, rightTop.y - 1, 1, borderHeight, null, 0, 1);
        if (!Arrays.stream(rgb).allMatch(BdmpRecognizer::isGray)) {
            return null;
        }
        // ↓ : 左边缘
        rgb = image.getRGB(leftTopPoint.x - 1, leftTopPoint.y, 1, borderHeight, null, 0, 1);
        if (!Arrays.stream(rgb).allMatch(BdmpRecognizer::isGray)) {
            return null;
        }
        // ↓→ : 下边缘
        rgb = image.getRGB(leftBottom.x, leftBottom.y + 1, borderWidth, 1, null, 0, borderWidth);
        if (!Arrays.stream(rgb).allMatch(BdmpRecognizer::isGray)) {
            return null;
        }
        // 确定变量
        BdmpRecCngInfo info = new BdmpRecCngInfo();
        info.setLeftTopPoint(leftTopPoint);
        info.setLeftBottomPoint(leftBottom);
        info.setRightTopPoint(rightTop);
        info.setRightBottomPoint(rightBottom1);
        return info;
    }

    /**
     * 寻找 XArr, YArr
     * XArr: x轴每一个像素点阵中的平均值
     * yArr: y轴每一个像素点阵中的平均值
     *
     * @param info 识别的像素信息
     * @return DoubleBean<x坐标集合, y坐标集合>
     */
    @SuppressWarnings({"java:S3776", "java:S3518"})
    public DoubleBean<int[], int[]> findXYArr(BdmpRecCngInfo info) {
        Point leftTopPoint = info.getLeftTopPoint();
        // 图片高度
        int height = image.getHeight();
        // 图片宽度
        int width = image.getWidth();
        // 找到xy 有效像素列表
        boolean isBlack = true;
        int cnt = 0;
        int sum = 0;
        List<Integer> xList = new ArrayList<>();
        for (int x = leftTopPoint.x, y = leftTopPoint.y; x < width; x++) {
            int rgb = image.getRGB(x, y);
            if (!isBorderVal(rgb)) {
                // 到此说明找到了黑白框外面
                if (cnt == 0) {
                    throw new WrongBranchException("");
                }
                xList.add(sum / cnt);
                Validate.isTrue(info.getRightTopPoint().equals(new Point(x - 1, y)), "两次识别的右上点不相同");
                break;
            }
            if ((isBlack && isBlack(rgb)) || (!isBlack && isWhite(rgb))) {
                // 在此说明黑色白色像素x宽度不为1, 需要通过取平均值来作为x像素坐标位置
                cnt++;
                sum += x;
            } else {
                // 在此说明跳到了不同颜色点阵
                xList.add(sum / cnt);
                cnt = 1;
                sum = x;
                isBlack = !isBlack;
            }
        }
        // check
        if (xList.size() < 3) {
            return null;
        }
        // 移除x首尾坐标点
        xList.remove(xList.size() - 1);
        xList.remove(0);
        int[] xArr = xList.stream().mapToInt(it -> it).toArray();
        xList.clear();
        isBlack = true;
        cnt = 0;
        sum = 0;
        for (int x = leftTopPoint.x, y = leftTopPoint.y; y < height; y++) {
            int rgb = image.getRGB(x, y);
            if (!isBorderVal(rgb)) {
                xList.add(sum / cnt);
                Validate.isTrue(info.getLeftBottomPoint().equals(new Point(x, y - 1)), "两次识别的右上点不相同");
                break;
            }
            if ((isBlack && isBlack(rgb)) || (!isBlack && isWhite(rgb))) {
                cnt++;
                sum += y;
            } else {
                xList.add(sum / cnt);
                cnt = 1;
                sum = y;
                isBlack = !isBlack;
            }
        }
        // check
        if (xList.size() < 3) {
            return null;
        }
        xList.remove(xList.size() - 1);
        xList.remove(0);
        int[] yArr = xList.stream().mapToInt(it -> it).toArray();
        return DoubleBean.of(xArr, yArr);
    }

}
