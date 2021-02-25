package cn.cpf.test.binpic;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * <b>Description : </b> 生成 binPic 图片工具类.
 * 因为当前类可能需要手动输入到指定环境, 因此当前类中的代码, 以及其调用到的代码不使用任何其它非JRE官方jar包
 *
 * @author CPF
 * Date: 2020/5/19 15:40
 */
public class BinPicGenerator {

    /**
     * 像素宽度
     */
    private final int pxSideLength;
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
     * 当前绘制 x坐标
     */
    private int x;
    /**
     * 当前绘制 y 坐标
     */
    private int y;
    /**
     * 绘图对象
     */
    private Graphics2D g2;
    /**
     * 绘制图片对象
     */
    private BufferedImage image;
    /**
     * 绘制像素颜色映射数组
     */
    private Color[] mappingColor;
    /**
     * h
     */
    private int num = 0;

    /**
     * @param imageWidth 图片宽度
     * @param imageHeight 图片高度
     * @param area 绘图区域
     * @param pixelSideLength 像素边长
     */
    public BinPicGenerator(int imageWidth, int imageHeight, Rectangle area, int pixelSideLength) {
        BinPicUtils.logInfo("image: [width, height]:[{}, {}], area: {}, pixelSideLength:{}", imageWidth, imageHeight, area, pixelSideLength);
        // check
        BinPicUtils.isTrue(area.x + area.width <= imageWidth, "the setting of width out of range");
        BinPicUtils.isTrue(area.y + area.height <= imageHeight, "the setting of height out of range");
        this.pxSideLength = pixelSideLength;
        pointXStart = area.x;
        pointXEnd = pointXStart + (area.width / pixelSideLength - 1) * pixelSideLength;
        pointYStart = area.y;
        pointYEnd = pointYStart + (area.height / pixelSideLength - 1) * pixelSideLength;

        //得到图片缓冲区, INT精确度达到一定,RGB三原色
        image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

        //得到它的绘制环境(这张图片的笔)
        g2 = (Graphics2D) image.getGraphics();

        // 绘制背景
        g2.setColor(Color.gray);
        g2.fillRect(0, 0, imageWidth, imageHeight);
    }

    /**
     * 绘制背景, 以及定位区,
     * 定位区为黑色白色像素相间交替围成的一个矩形.
     * 定位区左上角为黑色, 从左上角分别向下, 向右扩展, 右下角可能出现两个黑色像素或两个白色像素出现的情况.
     */
    public void drawerPosition() {
        int xFrom = pointXStart - pxSideLength;
        int xTo = pointXEnd + pxSideLength;
        int yFrom = pointYStart - pxSideLength;
        int yTo = pointYEnd + pxSideLength;
        int xAdd;
        int yAdd;
        boolean colorFlag = false;
        final int[][] setting = {
                {-1, xFrom, xTo, yFrom, yFrom},
                {0, xTo, xTo, yFrom + pxSideLength, yTo},
                {1, xFrom, xFrom, yFrom + pxSideLength, yTo},
                {0, xFrom + pxSideLength, xTo, yTo, yTo}
        };
        for (int[] arr : setting) {
            xFrom = arr[1];
            xTo = arr[2];
            yFrom = arr[3];
            yTo = arr[4];
            xAdd = xTo == xFrom ? 0 : pxSideLength;
            yAdd = yTo == yFrom ? 0 : pxSideLength;
            if (arr[0] > 0) {
                colorFlag = true;
            } else if (arr[0] < 0) {
                colorFlag =  false;
            }
            for (int ix = xFrom, iy = yFrom; ix <= xTo && iy <= yTo; ix += xAdd, iy+= yAdd) {
                if (colorFlag) {
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(Color.BLACK);
                }
                g2.fillRect(ix, iy, pxSideLength, pxSideLength);
                colorFlag = !colorFlag;
            }
        }
    }

    public void setMappingColor(Color[] mappingColor) {
        Objects.requireNonNull(mappingColor, "mappingColor cannot be null");
        int length = mappingColor.length;
        if (!Arrays.asList(2, 4, 16, 256).contains(length)) {
            throw new RuntimeException("not support the length of mappingColor: " + length);
        }
        this.mappingColor = mappingColor;
    }


    public void drawerHead(byte powOf2, int rowPxNum, int fileInfoByteLength) {
        // 8bit
        int[] bytes = BinPicUtils.convertByte(2, new byte[]{powOf2}, 1);
        doDrawer(bytes);
        // color draw
        for (Color color : mappingColor) {
            doDrawPixel(color);
        }
        drawer(BinPicUtils.toBytes(rowPxNum));
        drawer(BinPicUtils.toBytes(fileInfoByteLength));
    }

    private void drawer(byte[] b) {
        drawer(b, b.length);
    }

    public void drawer(byte[] b, int len) {
        Objects.requireNonNull(mappingColor, "mappingColor must not be null");
        // 将byte数组转为 mappingColor 进制数组
        int[] clr = BinPicUtils.convertByte(mappingColor.length, b, len);
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
     * @param color 像素颜色
     */
    private void doDrawPixel(Color color) {
        pointShift();
        // 移动至下一像素 x, y 位置
        // 绘制像素
        g2.setColor(color);
        g2.fillRect(x, y, pxSideLength, pxSideLength);
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
            x += pxSideLength;
            return;
        }
        if (y < pointYEnd) {
            y += pxSideLength;
            x = pointXStart;
            return;
        }
        throw new RuntimeException("draw pixel out of range");
    }

    /**
     * 生成图片宽度为 2 * borderLength +
     *
     * @param file     转换文件路径
     * @param rowPxNum 一行像素个数
     * @param pixelSideLength 像素宽度
     * @param margin margin长度,
     * @param powerOf2 2的指数幂, 可选值为 1, 2, 4, 8, 以及8的倍数,
     *                 像素颜色类型为 2 ^ powerOf2 个,
     *                 如果为 1, 像素颜色为  2种, 每 1 个bit作为一个像素,
     *                 如果为 2, 像素颜色为  4种, 每 2 个bit作为一个像素存储.
     *                 如果为 4, 像素颜色为 16种, 每 4 个bit作为一个像素存储.
     *                 如果为 8, 像素颜色为256种, 每 8 个bit作为一个像素存储.
     *                 如果为8n, 像素颜色为 2 ^ 8n 种, 每 8n 个bit作为一个像素存储.
     * @throws IOException 写入文件和读取文件流异常
     */
    public static BinPicGenerator convertFileToBinPic(File file, int rowPxNum, int pixelSideLength, int margin, byte powerOf2) throws IOException {
        Objects.requireNonNull(file);
        BinPicUtils.isTrue(file.isFile(), "file is not a file: %s", file.getAbsolutePath());
        BinPicUtils.isTrue(margin > 0, "margin should > 0: %s", margin);
        BinPicUtils.isTrue(pixelSideLength > 0, "the width of width should > 0, pixelSideLength: %s", pixelSideLength);
        // 行像素个数 需要 大于 0
        BinPicUtils.isTrue(rowPxNum > 0, "the number of pixel a row should > 0, rowPxNum: %s", rowPxNum);
        BinPicUtils.isTrue(powerOf2 == 1 || powerOf2 == 2 || powerOf2 == 4 || powerOf2 == 8, "not found the powerOf2: %s", powerOf2);

        // 像素类型数量
        int pxTypeCnt = (int) Math.pow(2, powerOf2);
        // 初始化文件头信息
        BinPicHeader header = new BinPicHeader();
        header.setTag(file.getName());
        header.setContentLength(file.length());
        byte[] dataByte = BinPicUtils.file2Byte(file);
        header.setContentMd5(BinPicUtils.encrypt2ToMd5(dataByte));
        final String json = header.toJson();
        final byte[] headBytes = json.getBytes(StandardCharsets.UTF_8);
        BinPicUtils.logInfo("head\t" + json);

        // 像素总个数
        long pxTotalSize = (8 + pxTypeCnt) + (4 + 4 + headBytes.length + file.length()) * 8 / powerOf2;
        // 绘制区域宽度
        int areaWidth = rowPxNum * pixelSideLength;
        // 绘制区域高度
        int areaHeight = (int)Math.ceil((double) pxTotalSize / rowPxNum) * pixelSideLength;
        // pic 长度 = 边缘长度 + 定位区长度
        int borderLength = margin + pixelSideLength;
        BinPicUtils.logInfo("pxTotalSize : {}, draw area: [width, height]:[{}, {}], border margin: {}", pxTotalSize, areaWidth, areaHeight, margin);
        BinPicGenerator binPicGenerator = new BinPicGenerator(areaWidth + borderLength * 2, areaHeight + borderLength * 2,
                new Rectangle(borderLength, borderLength, areaWidth, areaHeight), pixelSideLength);

        // 写入定位区
        binPicGenerator.drawerPosition();
        binPicGenerator.setMappingColor(BinPicUtils.getPxType(powerOf2));
        // 写入像素信息, 行像素量, 头长度信息
        binPicGenerator.drawerHead(powerOf2, rowPxNum, headBytes.length);
        // 写入 fileInfo
        binPicGenerator.drawer(headBytes);
        // 写入文件
        try (FileInputStream in = new FileInputStream(file)){
            byte[] buf = new byte[8 * 1024];
            int bytes;
            while ((bytes = in.read(buf, 0, buf.length)) != -1) {
                binPicGenerator.drawer(buf, bytes);
            }
        }
        return binPicGenerator;
    }

    /**
     * @param filePath 文件路径
     * @param savePath 存储路径
     * @param rowPxNum 一行像素个数
     * @param pxWidth 像素宽度
     * @param margin margin长度
     * @param powerOf2 2的指数幂, 可选值为 1, 2, 4, 8, 以及8的倍数,
     *                 像素颜色类型为 2 ^ powerOf2 个,
     *                 如果为 1, 像素颜色为  2种, 每 1 个bit作为一个像素,
     *                 如果为 2, 像素颜色为  4种, 每 2 个bit作为一个像素存储.
     *                 如果为 4, 像素颜色为 16种, 每 4 个bit作为一个像素存储.
     *                 如果为 8, 像素颜色为256种, 每 8 个bit作为一个像素存储.
     *                 如果为8n, 像素颜色为 2 ^ 8n 种, 每 8n 个bit作为一个像素存储.
     * @throws IOException 写入文件和读取文件流异常
     */
    public static void convertFileToBinPic(String filePath, String savePath, int rowPxNum, int pxWidth, int margin, byte powerOf2) throws IOException {
        File file = new File(filePath);
        BinPicGenerator binPicGenerator = convertFileToBinPic(file, rowPxNum, pxWidth, margin, powerOf2);
        BinPicUtils.logInfo( "pixel size " + binPicGenerator.getNum());
        // 保存图片 JPEG表示保存格式
        ImageIO.write(binPicGenerator.getImage(), "png", new FileOutputStream(savePath));
        BinPicUtils.logInfo( "pixel end " + filePath);
    }

    /**
     * @param filePath 文件路径
     * @param rowPxNum 一行像素个数
     * @param pxWidth 像素宽度
     * @param margin margin长度
     * @param powerOf2 2的指数幂, 可选值为 1, 2, 4, 8, 以及8的倍数,
     *                 像素颜色类型为 2 ^ powerOf2 个,
     *                 如果为 1, 像素颜色为  2种, 每 1 个bit作为一个像素,
     *                 如果为 2, 像素颜色为  4种, 每 2 个bit作为一个像素存储.
     *                 如果为 4, 像素颜色为 16种, 每 4 个bit作为一个像素存储.
     *                 如果为 8, 像素颜色为256种, 每 8 个bit作为一个像素存储.
     *                 如果为8n, 像素颜色为 2 ^ 8n 种, 每 8n 个bit作为一个像素存储.
     * @throws IOException 写入文件和读取文件流异常
     */
    public static void convertFileToBinPic(String filePath, int rowPxNum, int pxWidth, int margin, byte powerOf2) throws IOException {
        convertFileToBinPic(filePath, filePath + ".png", rowPxNum, pxWidth, margin, powerOf2);
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getNum() {
        return num;
    }

}