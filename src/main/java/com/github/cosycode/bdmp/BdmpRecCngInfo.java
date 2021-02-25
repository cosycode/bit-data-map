package com.github.cosycode.bdmp;

import lombok.Data;

import java.awt.*;

/**
 * <b>Description : </b> 像素图片识别出的配置信息
 * <p>
 * <b>created in </b> 2020/11/18
 *
 * @author CPF
 * @since 1.0
 */
@Data
public class BdmpRecCngInfo {
    /**
     * 左上方标记点
     */
    private Point leftTopPoint;
    /**
     * 右上方标记点
     */
    private Point rightTopPoint;
    /**
     * 左下方标记点
     */
    private Point leftBottomPoint;
    /**
     * 左下方标记点
     */
    private Point rightBottomPoint;

    private int[] xArr;

    private int[] yArr;

}
