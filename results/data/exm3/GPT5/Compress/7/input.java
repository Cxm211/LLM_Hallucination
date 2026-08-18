// buggy function
    public static String parseName(byte[] buffer, final int offset, final int length) {
        StringBuffer result = new StringBuffer(length);
        int          end = offset + length;

        for (int i = offset; i < end; ++i) {
            if (buffer[i] == 0) {
                break;
            }
            result.append((char) buffer[i]);
        }

        return result.toString();
    }

// trigger testcase
// org/apache/commons/compress/archivers/tar/TarUtilsTest.java::testRoundTripNames
public void testRoundTripNames(){
        checkName("");
        checkName("The quick brown fox\n");
        checkName("\177");
        // checkName("\0"); // does not work, because NUL is ignored
        // COMPRESS-114
        checkName("0302-0601-3���F06�W220�ZB�LALALA����������CAN��DC���04�060302�MOE.model");
    }
