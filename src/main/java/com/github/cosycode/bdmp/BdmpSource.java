package com.github.cosycode.bdmp;

import com.github.cosycode.common.base.SupplierWithThrow;
import lombok.Getter;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * <b>Description : </b> 图片源
 * <p>
 * <b>created in </b> 2020/11/18
 *
 * @author CPF
 * @since 1.0
 */
@Getter
public class BdmpSource {

    /**
     * 图片源类型
     */
    private final SourceType sourceType;
    /**
     * 图片源名称, 如果源是文件的话, 那么此处为文件的名字
     */
    private final String name;
    /**
     * 源内容
     */
    private byte[] content;
    private final SupplierWithThrow<byte[], IOException> dataSupplier;

    public BdmpSource(SourceType sourceType, String name, SupplierWithThrow<byte[], IOException> dataSupplier) {
        this.sourceType = sourceType;
        this.name = name;
        this.dataSupplier = dataSupplier;
    }

    public static BdmpSource geneByFile(File file) {
        return new BdmpSource(SourceType.TYPE_FILE, file.getName(), () -> {
            final long length = file.length();
            Validate.isTrue(length < Integer.MAX_VALUE, "文件过大");
            byte[] bytes = new byte[(int) length];
            // 写入文件
            try (FileInputStream in = new FileInputStream(file)) {
                final int read = in.read(bytes);
                Validate.isTrue(read == (int) length, "读取错误");
            }
            return bytes;
        });
    }

    public static BdmpSource geneByClipboard(String name, byte[] content) {
        return new BdmpSource(SourceType.TYPE_CLIPBOARD, name, () -> content);
    }

    public void checkWithThrow() throws IOException {
        Objects.requireNonNull(getContent(), "无内容");
        Objects.requireNonNull(getSourceType(), "无SourceType");
    }

    public byte[] getContent() throws IOException {
        if (content == null) {
            content = dataSupplier.get();
        }
        return content;
    }

    /**
     * 源类型: {文件地址, 网络地址, 二进制流, 剪贴板}
     */
    public enum SourceType {
        TYPE_FILE, TYPE_CONTENT, TYPE_CLIPBOARD
    }

}
