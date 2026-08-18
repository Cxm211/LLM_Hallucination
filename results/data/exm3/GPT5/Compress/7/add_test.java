// org/apache/commons/compress/archivers/tar/TarUtilsTest.java::testRoundTripNames
public void testRoundTripNames(){
        byte[] buf = new byte[] {(byte)0xFF, 0};
        String s = parseName(buf, 0, 2);
        org.junit.Assert.assertEquals("\u00FF", s);
    }