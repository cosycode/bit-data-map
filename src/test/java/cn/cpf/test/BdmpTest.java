package cn.cpf.test;

import com.github.cosycode.bdmp.*;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * <b>Description : </b> 二进制像素图片生成测试工具类
 * <p>
 * <b>created in </b> 2020/11/18
 *
 * @author CPF
 * @since 1.0
 */
public class BdmpTest {

    public static final String SOURCE_PATH = "D:\\Users\\CPF\\Desktop\\out\\realtime\\应用系统开发管理手册（2020年版）分册.part11.rar";

    /**
     * 生成二级制数据像素图片方式0
     *
     * @throws IOException 读取文件异常
     */
//    @Test
    public void geneBdmp0() throws IOException {
        // 转换后的文件存放位置
        final String bdmpFilePath = SOURCE_PATH + ".png";

        BdmpHandle.convertFileToBdmp(SOURCE_PATH, bdmpFilePath, 800, 2, 20, (byte) 8);
    }

    /**
     * 生成二级制数据像素图片方式1
     *
     * @throws IOException 读取文件异常
     */
    @Test
    public void geneBdmp1() throws IOException {
        // 转换后的文件存放位置
        final String bdmpFilePath = SOURCE_PATH + ".png";

        // 数据源
        final BdmpSource bdmpSource = BdmpSource.geneByFile(new File(SOURCE_PATH));
        // 二进制数据图片配置
        BdmpGeneConfig config = new BdmpGeneConfig();
        // 设置图片边缘为 20px
        config.setMargin(20);
        // 设置图片一行点阵数量为 800 个
        config.setRowPixelCnt(800);
        // 设置每个点阵宽度为1像素
        config.setPixelSideWidth(1);
        // 设置每个点阵高度为1像素
        config.setPixelSideHeight(1);
        // 设置 每个点阵表示 8 bit, 每个点阵有 2^8 = 256种颜色
        config.setMappingColor(BdmpUtils.getPxType(8));
        // 生成的bit-data-map的生成信息类
        final BdmpGeneInfo bdmpGeneInfo = new BdmpGeneInfo(config, bdmpSource);
        // 生成图片
        final BufferedImage image = PixelPngDrawer.geneRatePixelPng(bdmpGeneInfo);
        // 保存图片 png表示保存格式
        ImageIO.write(image, "png", new FileOutputStream(bdmpFilePath));
    }

//    @Test
    public void geneBdmp2() throws IOException {
        final String t = "二进制数据图片";
        // 数据源
        final BdmpSource bdmpSource = BdmpSource.geneByClipboard("", t.getBytes());
        // 二进制数据图片配置
        BdmpGeneConfig config = new BdmpGeneConfig();
        config.setMargin(20);
        config.setRowPixelCnt(80);
        config.setPixelSideWidth(8);
        config.setPixelSideHeight(8);
        config.setMappingColor(BdmpUtils.getPxType(8));
        // 生成的信息
        final BdmpGeneInfo bdmpGeneInfo = new BdmpGeneInfo(config, bdmpSource);
        final BufferedImage image = PixelPngDrawer.geneRatePixelPng(bdmpGeneInfo);

        // 保存图片 JPEG表示保存格式
        ImageIO.write(image, "png", new FileOutputStream(SOURCE_PATH + ".png"));
    }

    @Test
    public void recBdmp() throws IOException {
        // 待转换的图片文件
        final String SOURCE_PATH = "D:\\Users\\CPF\\Desktop\\out\\realtime\\tmp\\应用系统开发管理手册（2020年版）分册.part11.rar.png";
        // 转换后的文件存放文件夹
        final String recDirPath = "D:\\Users\\CPF\\Desktop\\";

        BdmpHandle.convertBdmpToFile(SOURCE_PATH, recDirPath);
    }

}
