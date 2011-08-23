package com.inadco.hbl.util;

import java.util.Arrays;
import java.util.List;

import com.inadco.hbl.api.Cuboid;

public class HblUtil {

    static char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /**
     * insert byte sources as hex representation into hbase composite key at a
     * given offset.
     * 
     * @param srcBytes
     *            src bytes to insert
     * @param srcOffset
     *            offset in src bytes
     * @param srcLen
     *            src length
     * @param holder
     *            target composite key
     * @param holderOffset
     *            offset in the target composite key
     * @return composite key (same as holder)
     */
    public static byte[] fillCompositeKeyWithHex(byte[] srcBytes,
                                                 int srcOffset,
                                                 int srcLen,
                                                 byte[] holder,
                                                 int holderOffset) {
        for (int i = 0; i < srcLen; i++) {
            holder[holderOffset++] = (byte) (hexArray[(srcBytes[srcOffset] & 0xf0) >>> 4]);
            holder[holderOffset++] = (byte) (hexArray[srcBytes[srcOffset++] & 0xf]);
        }
        return holder;
    }

    /**
     * test whether string is a valid hex representation of a byte array key of
     * length <code>keylen</code> Not terribly efficient, so probably use is not
     * encouraged.
     * 
     * @param hexString
     * @param keylen
     * @return
     */
    public static boolean isValidIDInHex(String hexString, int keylen) {

        if (hexString == null)
            return false;
        hexString = hexString.trim();
        if (hexString.length() == 0)
            return false;

        int l = hexString.length();
        int unpaddedLen = 0;
        for (int i = 0; i < l; i++) {
            char c = hexString.charAt(i);
            int v;
            if ((v = Character.digit(c, 16)) == -1)
                return false;
            if (v > 0 || unpaddedLen > 0)
                unpaddedLen++;
        }
        return (unpaddedLen + 1 >> 1) > keylen ? false : true;
    }

    /**
     * read hex-encoded part of composite key into holder
     * 
     * @param composite
     *            the src composite key
     * @param offset
     *            offset in the src composite key to start reading from
     * @param holder
     *            the buffer to read bytes into
     * @param holderOffset
     *            offset in the holder to read the bytes into
     * @param holderLen
     *            number of bytes to read into holder
     * @return
     */
    public static byte[] readCompositeKeyHex(byte[] composite,
                                             int offset,
                                             byte[] holder,
                                             int holderOffset,
                                             int holderLen) {

        for (int i = 0; i < holderLen; i++) {
            int c1 = composite[offset++] & 0xff, c2 = composite[offset++] & 0xff;
            c1 = (c1 - '0' < 10) ? c1 - '0' : c1 - 'A';
            c2 = (c2 - '0' < 10) ? c2 - '0' : c2 - 'A';

            holder[holderOffset++] = (byte) ((c1 << 4) + c2);
        }

        return holder;
    }

    public static String encodeCuboidPath(Cuboid cuboid) {
        String r = null;
        for (String dim : cuboid.getCuboidPath())
            r = r == null ? dim : r + "/" + dim;
        return r;
    }

    public static List<String> decodeCuboidPath(String path) {
        return Arrays.asList(path.split("/"));
    }

    public static byte[] fillCompositeKeyWithDec(int src, int decimalLen, byte[] holder, int holderOffset) {
        for (int i = holderOffset + decimalLen - 1; i >= holderOffset; i--)
            holder[i] = (byte) ('0' + (src - (src /= 10) * 10));
        return holder;
    }

    public static int readCompositeKeyDec(byte[] composite, int offset, int decimalLen) {
        int result = 0;
        for (int i = 0; i < decimalLen; i++)
            result = 10 * result + (composite[offset++] - '0');
        return result;
    }

}