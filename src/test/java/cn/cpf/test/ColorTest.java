package cn.cpf.test;

import com.github.cosycode.bdmp.BdmpUtils;
import org.junit.Test;

import java.awt.*;
import java.util.Arrays;

public class ColorTest {

    public void main() {
        final Color[] pxType = BdmpUtils.getPxType((byte) 8);
        if (pxType.length == 256) {
            final int[] ints = Arrays.stream(pxType).mapToInt(Color::getRGB).toArray();
            int[][] rArr = new int[4][3];
            int[][] gArr = new int[8][3];
            int[][] bArr = new int[8][3];
            int i = 0;
            for (int r = 0; r < 4; r++) {
                for (int g = 0; g < 8; g++) {
                    for (int b = 0; b < 8; b++) {
                        final int anInt = ints[i++];
                        dispose(rArr[r], (anInt >> 16) & 0xFF, i);
                        dispose(gArr[g], (anInt >> 8) & 0xFF, i);
                        dispose(bArr[b], anInt & 0xFF, i);
                    }
                }
            }
            int[][] valArr = new int[3][8];
            for (int i1 = 0; i1 < rArr.length; i1++) {
                valArr[2][i1] = rArr[i1][0];
            }
            for (int i1 = 0; i1 < gArr.length; i1++) {
                valArr[1][i1] = gArr[i1][0];
            }
            for (int i1 = 0; i1 < bArr.length; i1++) {
                valArr[0][i1] = bArr[i1][0];
            }
            for (int i1 = 0; i1 < valArr.length; i1++) {
                System.out.println(Arrays.toString(valArr[i1]));
            }
        }
    }


    public static void dispose(int[] arr, int a, int cnt) {
        if (arr[2] == 0 || arr[0] - a < 16) {
            arr[1] += a;
            arr[2] ++;
            arr[0] = arr[1] / arr[2];
        } else {
            throw new RuntimeException();
        }
    }

}
