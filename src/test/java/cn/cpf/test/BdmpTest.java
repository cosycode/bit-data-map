package cn.cpf.test;

import com.github.cosycode.bdmp.*;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * <b>Description : </b>
 *
 * @author CPF
 * @date 2020/11/18 9:36
 **/
public class BdmpTest {

    public static final String SOURCE_PATH = "D:\\Users\\CPF\\Desktop\\pp.pdf";

    @Test
    public void geneBdmp() throws IOException {
        // 数据源
        final BdmpSource bdmpSource = BdmpSource.geneByFile(new File(SOURCE_PATH));
        // 二进制数据图片配置
        BdmpGeneConfig config = new BdmpGeneConfig();
        config.setMargin(20);
        config.setRowPixelCnt(400);
        config.setPixelSideWidth(1);
        config.setPixelSideHeight(1);
        config.setMappingColor(BdmpUtils.getPxType(8));
        // 生成的信息
        final BdmpGeneInfo bdmpGeneInfo = new BdmpGeneInfo(config, bdmpSource);
        final BufferedImage image = PixelPngDrawer.geneRatePixelPng(bdmpGeneInfo);

        // 保存图片 JPEG表示保存格式
        ImageIO.write(image, "png", new FileOutputStream(SOURCE_PATH + ".png"));
    }

    @Test
    public void recBdmpTest() throws IOException {
        BdmpHandle.convertBdmpToFile(SOURCE_PATH + ".png", "D:\\Users\\CPF\\Desktop\\");
    }

    @Test
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


}
