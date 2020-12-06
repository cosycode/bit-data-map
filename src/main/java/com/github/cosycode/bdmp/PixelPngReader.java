package com.github.cosycode.bdmp;

import com.github.cosycode.common.util.common.ArrUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;

/**
 * <b>Description : </b> 用于从png图片上面读取信息的类
 *
 * @author CPF
 * @date 2020/11/18
 **/
@Slf4j
class PixelPngReader {
    @Getter
    private BufferedImage image;

    /**
     * x 像素区域列表
     */
    @Getter
    private int[] xArr;

    /**
     * y 像素区域列表
     */
    @Getter
    private int[] yArr;

    /**
     * 读取像素位置(用于计算下次读取位置)
     */
    private int no;

    @Getter
    private byte[] fileContent;

    @Getter
    private int[] byteModal;

    @Getter
    private int bitCnt;

    @Getter
    private BdmpHeader bdmpHeader;

    @Getter
    private int contentLength;

    public PixelPngReader(BufferedImage image, int[] xArr, int[] yArr) {
        this.image = image;
        this.xArr = xArr;
        this.yArr = yArr;
        init();
    }

    /**
     * 将一段整形数据 按规则 解析成一个整数
     *
     * @param byteModal 解析 byte 类型
     * @param valArr    int 值
     * @param bit       基数位
     * @return 解析后的整数
     */
    public static int deCode(int[] byteModal, int[] valArr, int bit) {
        int val = 0;
        for (int i : valArr) {
            int i1 = ArrUtils.indexOf(byteModal, i);
            if (i1 < 0) {
                throw new RuntimeException("解析整形数据像素失真! 在byteModal中未发现xi相关数据;");
            }
            val = val << bit | i1;
        }
        return val;
    }

    /**
     * 读取初始化数据
     */
    public void init() {
        no = 0;
        int[] powOf2Bin = readPixel(8);
        int[] oneTwo = readPixel(2);
        no -= 2;
        bitCnt = deCode(oneTwo, powOf2Bin, 1);
        int pixelTypeCnt = (int) Math.pow(2, bitCnt);
        byteModal = readPixel(pixelTypeCnt);
    }

    /**
     * 将从图片上面读取到的整型的rgb值转换为具体的byte数组
     *
     * @param valArr 图片上面读取到的整型rgb像素值
     * @return 转换成数据的byte数组
     */
    private byte[] deCodeToByte(int[] valArr) {
        int max = byteModal.length;
        byte[] bytes = new byte[valArr.length];
        for (int i = 0; i < valArr.length; i++) {
            int v = ArrUtils.indexOf(byteModal, valArr[i]);
            if (v < 0 || v >= max) {
                throw new RuntimeException("解析rgb数据像素失真! 在byteModal中未发现xi相关数据;");
            }
            bytes[i] = (byte) v;
        }
        return BdmpUtils.deCodeToByte(bitCnt, bytes);
    }

    /**
     * 读取文件信息
     */
    public void readFileInfo() {
        // 1 byte所占bit数 和 一个像素所占bit之比
        int bi = 8 / bitCnt;
        // 一行有多少像素
        int[] rowPixelCnt = readPixel(4 * bi);
        int rowPxNumLength = deCode(byteModal, rowPixelCnt, bitCnt);
        Validate.isTrue(rowPxNumLength == xArr.length, String.format("rowPxNumLength : %S != xArr.length: %s", rowPxNumLength, xArr.length));
        // 文件信息长度
        int[] fileInfoLength = readPixel(4 * bi);
        contentLength = deCode(byteModal, fileInfoLength, bitCnt);
        // 读取文件信息
        int[] intSerial = readPixel(contentLength * bi);
        byte[] content = deCodeToByte(intSerial);
        // 文件头
        final String json = new String(content, StandardCharsets.UTF_8);
        log.info("文件头信息: {}", json);
        bdmpHeader = BdmpHeader.fromJson(json);
        // 文件内容
        intSerial = readPixel((int) (bdmpHeader.getContentLength() * bi));
        fileContent = deCodeToByte(intSerial);
    }

    /**
     * @param number 读取像素图片内容区中指定数目的 pixel, 并返回
     * @return 读取的像素值
     */
    @SuppressWarnings({"java:S1994", "java:S127"})
    public int[] readPixel(int number) {
        int xLength = xArr.length;
        int yLength = yArr.length;
        int[] arr = new int[number];
        int i = 0;
        for (int y = no / xLength, x = no % xLength; y < yLength && i < number; y++) {
            for (; x < xLength && i < number; x++) {
                int rgb = image.getRGB(xArr[x], yArr[y]);
                arr[i] = rgb;
                i++;
            }
            x = 0;
        }
        no += number;
        return arr;
    }
}
