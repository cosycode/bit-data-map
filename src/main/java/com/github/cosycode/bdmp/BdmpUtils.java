package com.github.cosycode.bdmp;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <b>Description : </b> 图片工具类
 *
 * @author CPF
 * Date: 2020/5/21 9:35
 */
public class BdmpUtils {

    private BdmpUtils() {
    }

    /**
     * 获取不同种类的像素数量
     *
     * @param powerOf2 2的指数幂, 可选值为 1, 2, 4, 8, 以及8的倍数,
     *                 像素颜色类型为 2 ^ powerOf2 个,
     *                 如果为 1, 像素颜色为  2种, 每 1 个bit作为一个像素,
     *                 如果为 2, 像素颜色为  4种, 每 2 个bit作为一个像素存储.
     *                 如果为 4, 像素颜色为 16种, 每 4 个bit作为一个像素存储.
     *                 如果为 8, 像素颜色为256种, 每 8 个bit作为一个像素存储.
     *                 如果为8n, 像素颜色为 2 ^ 8n 种, 每 8n 个bit作为一个像素存储.
     * @return 像素颜色数组
     */
    @SuppressWarnings({"java:S3776", "all"})
    public static Color[] getPxType(int powerOf2) {
        Color[] colors;
        switch (powerOf2) {
            case 1:
                colors = new Color[]{Color.RED, Color.BLUE};
                break;
            case 2:
                colors = new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW};
                break;
            case 4: {
                int[] rArr = {0x10, 0xe0};
                int[] gArr = rArr;
                int[] bArr = {0x00, 0x50, 0xa0, 0xf0};
                colors = new Color[rArr.length * gArr.length * bArr.length];
                int n = 0;
                for (int r : rArr) {
                    for (int g : gArr) {
                        for (int b : bArr) {
                            colors[n++] = new Color(r, g, b);
                        }
                    }
                }
            }
            break;
            case 8: {
                // 4进制
                int[] rArr = {0x00, 0x50, 0xa0, 0xf0};
                // 8进制
                int[] gArr = {0x10, 0x30, 0x50, 0x70, 0x90, 0xb0, 0xd0, 0xf0};
                int[] bArr = gArr;
                colors = new Color[rArr.length * gArr.length * bArr.length];
                int n = 0;
                for (int r : rArr) {
                    for (int g : gArr) {
                        for (int b : bArr) {
                            colors[n++] = new Color(r, g, b);
                        }
                    }
                }
            }
            break;
            default:
                throw new RuntimeException("不支持的类型");
        }
        return colors;
    }


    /**
     * 将int转换为byte数组
     *
     * @param number 整形数据
     * @return 转换后的byte数组
     */
    public static byte[] toBytes(int number) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte) number;
        bytes[2] = (byte) (number >>> 8);
        bytes[1] = (byte) (number >>> 16);
        bytes[0] = (byte) (number >>> 24);
        return bytes;
    }

    /**
     * 将一个byte数组, 按照进制基数, 转换为另一个byte数组
     * <p>
     * eg: 当powOf2 为4, 将 [bbbbbbbb] 转换为 [0000bbbb, 0000bbbb], b代表有含义的数
     *
     * @param powOf2 {@link BdmpUtils#deCodeToByte(int, byte[])}
     */
    @SuppressWarnings("java:S127")
    public static int[] convertByte(int powOf2, byte[] b, int len) {
        int[] r = null;
        if (powOf2 == 2) {
            r = new int[len * 8];
            for (int i = 0, j = 0; i < len; i++) {
                r[j++] = ((b[i] & 0b10000000) >>> 7);
                r[j++] = (byte) ((b[i] & 0b01000000) >>> 6);
                r[j++] = (byte) ((b[i] & 0b00100000) >>> 5);
                r[j++] = (byte) ((b[i] & 0b00010000) >>> 4);
                r[j++] = (byte) ((b[i] & 0b00001000) >>> 3);
                r[j++] = (byte) ((b[i] & 0b00000100) >>> 2);
                r[j++] = (byte) ((b[i] & 0b00000010) >>> 1);
                r[j++] = (byte) (b[i] & 0b00000001);
            }
        } else if (powOf2 == 4) {
            r = new int[len * 4];
            for (int i = 0, j = 0; i < len; i++) {
                r[j++] = (byte) ((b[i] & 0b11000000) >>> 6);
                r[j++] = (byte) ((b[i] & 0b00110000) >>> 4);
                r[j++] = (byte) ((b[i] & 0b00001100) >>> 2);
                r[j++] = (byte) (b[i] & 0b00000011);
            }
        } else if (powOf2 == 16) {
            r = new int[len * 2];
            for (int i = 0, j = 0; i < len; i++) {
                r[j++] = (byte) ((b[i] & 0b11110000) >>> 4);
                r[j++] = (byte) (b[i] & 0b00001111);
            }
        } else if (powOf2 == 256) {
            r = new int[len];
            for (int i = 0; i < len; i++) {
                r[i] = Byte.toUnsignedInt(b[i]);
            }
        }
        return r;
    }

    /**
     * 将一个byte数组, 按照进制基数, 转换为另一个byte数组
     * <p>
     * eg: 当powOf2 为4, 将 [bbbbbbbb] 转换为 [0000bbbb, 0000bbbb], b代表有含义的数
     *
     * @param bit {@link BdmpUtils#deCodeToByte(int, byte[])}
     */
    @SuppressWarnings("java:S127")
    public static int[] concatByte(int bit, byte[] b, int len) {
        int[] r = null;
        if (bit == 1) {
            r = new int[len * 8];
            for (int i = 0, j = 0; i < len; i++) {
                r[j++] = ((b[i] & 0b10000000) >>> 7);
                r[j++] = (byte) ((b[i] & 0b01000000) >>> 6);
                r[j++] = (byte) ((b[i] & 0b00100000) >>> 5);
                r[j++] = (byte) ((b[i] & 0b00010000) >>> 4);
                r[j++] = (byte) ((b[i] & 0b00001000) >>> 3);
                r[j++] = (byte) ((b[i] & 0b00000100) >>> 2);
                r[j++] = (byte) ((b[i] & 0b00000010) >>> 1);
                r[j++] = (byte) (b[i] & 0b00000001);
            }
        } else if (bit == 2) {
            r = new int[len * 4];
            for (int i = 0, j = 0; i < len; i++) {
                r[j++] = (byte) ((b[i] & 0b11000000) >>> 6);
                r[j++] = (byte) ((b[i] & 0b00110000) >>> 4);
                r[j++] = (byte) ((b[i] & 0b00001100) >>> 2);
                r[j++] = (byte) (b[i] & 0b00000011);
            }
        } else if (bit == 4) {
            r = new int[len * 2];
            for (int i = 0, j = 0; i < len; i++) {
                r[j++] = (byte) ((b[i] & 0b11110000) >>> 4);
                r[j++] = (byte) (b[i] & 0b00001111);
            }
        } else if (bit == 8) {
            r = new int[len];
            for (int i = 0; i < len; i++) {
                r[i] = Byte.toUnsignedInt(b[i]);
            }
        }
        return r;
    }


    /**
     * 将 byte 数组 按 powOf2 bit 整合数组
     * eg: 当powOf2 为4, 将 [0000bbbb, 0000bbbb] 转换为 [bbbbbbbb], b代表有含义的数
     *
     * @param powOf2 bit位
     * @param valArr 值数组
     * @return 转换后的数组
     */
    public static byte[] deCodeToByte(int powOf2, byte[] valArr) {
        int len = valArr.length;
        int loop = len * powOf2 / 8;
        byte[] bytes = new byte[loop];
        int n = 0;
        if (powOf2 == 1) {
            for (int i = 0; i < loop; i++) {
                bytes[i] |= valArr[n++] << 7;
                bytes[i] |= valArr[n++] << 6;
                bytes[i] |= valArr[n++] << 5;
                bytes[i] |= valArr[n++] << 4;
                bytes[i] |= valArr[n++] << 3;
                bytes[i] |= valArr[n++] << 2;
                bytes[i] |= valArr[n++] << 1;
                bytes[i] |= valArr[n++];
            }
        } else if (powOf2 == 2) {
            for (int i = 0; i < loop; i++) {
                bytes[i] |= valArr[n++] << 6;
                bytes[i] |= valArr[n++] << 4;
                bytes[i] |= valArr[n++] << 2;
                bytes[i] |= valArr[n++];
            }
        } else if (powOf2 == 4) {
            for (int i = 0; i < loop; i++) {
                bytes[i] |= valArr[n++] << 4;
                bytes[i] |= valArr[n++];
            }
        } else if (powOf2 == 8) {
            for (int i = 0; i < loop; i++) {
                bytes[i] |= valArr[n++];
            }
        } else {
            throw new RuntimeException();
        }
        return bytes;
    }

    public static String encrypt2ToMd5(byte[] data) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
        byte[] digest = md5.digest(data);
        return new BigInteger(1, digest).toString(16);
    }

    /**
     * 将文件转换成byte数组
     */
    public static byte[] file2Byte(File tradeFile) {
        try (FileInputStream fis = new FileInputStream(tradeFile);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] b = new byte[8 * 1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * copy by {@link org.apache.commons.lang3.Validate#isTrue(boolean, String, Object...)}
     */
    public static void isTrue(final boolean expression, final String message, final Object... values) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(message, values));
        }
    }

    /**
     * 从系统中加载图片
     */
    public static BufferedImage load(String picPath) throws IOException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(picPath))) {
            final BufferedImage image = ImageIO.read(is);
            if (image == null) {
                throw new RuntimeException("图片读取失败, 请检查文件格式是否正确");
            }
            return image;
        }
    }
}
