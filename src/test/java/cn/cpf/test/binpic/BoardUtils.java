package cn.cpf.test.binpic;

import sun.awt.datatransfer.DataTransferer;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

/**
 * <b>Description : </b>
 *
 * @author CPF
 * @date 2020/11/3 10:56
 **/
public class BoardUtils {

    public static void main(String[] args) throws IOException, UnsupportedFlavorException {
        final Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        System.out.println(systemClipboard.getName());
        final Transferable contents = systemClipboard.getContents(null);

        final byte[] bytes = DataTransferer.getInstance().translateTransferable(contents, DataFlavor.allHtmlFlavor, 0);
        final Object o = DataTransferer.getInstance().translateBytes(bytes, DataFlavor.allHtmlFlavor, 0, null);

        if (systemClipboard.isDataFlavorAvailable(DataFlavor.allHtmlFlavor)) {
            final String data = (String) systemClipboard.getData(DataFlavor.allHtmlFlavor);
        } else if (systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            final String data = (String) systemClipboard.getData(DataFlavor.stringFlavor);
            System.out.println(data);
        } else if (systemClipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
            final Object data = (Image) systemClipboard.getData(DataFlavor.imageFlavor);
            System.out.println(data);
        }

        systemClipboard.setContents(new StringSelection("fjdkfjdkfjdk"), null);
    }

    private static void judge(Clipboard systemClipboard, DataFlavor dataFlavor) {

    }


}
