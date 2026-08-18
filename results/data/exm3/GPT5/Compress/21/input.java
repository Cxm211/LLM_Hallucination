// buggy function
    private void writeBits(final DataOutput header, final BitSet bits, final int length) throws IOException {
        int cache = 0;
        int shift = 7;
        for (int i = 0; i < length; i++) {
            cache |= ((bits.get(i) ? 1 : 0) << shift);
            --shift;
            if (shift == 0) {
                header.write(cache);
                shift = 7;
                cache = 0;
            }
        }
        if (length > 0 && shift > 0) {
            header.write(cache);
        }
    }

// trigger testcase
// org/apache/commons/compress/archivers/sevenz/SevenZOutputFileTest.java::testEightEmptyFiles
public void testEightEmptyFiles() throws Exception {
        testCompress252(8, 0);
    }

// org/apache/commons/compress/archivers/sevenz/SevenZOutputFileTest.java::testEightFilesSomeNotEmpty
public void testEightFilesSomeNotEmpty() throws Exception {
        testCompress252(8, 2);
    }

// org/apache/commons/compress/archivers/sevenz/SevenZOutputFileTest.java::testNineEmptyFiles
public void testNineEmptyFiles() throws Exception {
        testCompress252(9, 0);
    }

// org/apache/commons/compress/archivers/sevenz/SevenZOutputFileTest.java::testNineFilesSomeNotEmpty
public void testNineFilesSomeNotEmpty() throws Exception {
        testCompress252(9, 2);
    }

// org/apache/commons/compress/archivers/sevenz/SevenZOutputFileTest.java::testSevenEmptyFiles
public void testSevenEmptyFiles() throws Exception {
        testCompress252(7, 0);
    }

// org/apache/commons/compress/archivers/sevenz/SevenZOutputFileTest.java::testSevenFilesSomeNotEmpty
public void testSevenFilesSomeNotEmpty() throws Exception {
        testCompress252(7, 2);
    }

// org/apache/commons/compress/archivers/sevenz/SevenZOutputFileTest.java::testSixEmptyFiles
public void testSixEmptyFiles() throws Exception {
        testCompress252(6, 0);
    }

// org/apache/commons/compress/archivers/sevenz/SevenZOutputFileTest.java::testSixFilesSomeNotEmpty
public void testSixFilesSomeNotEmpty() throws Exception {
        testCompress252(6, 2);
    }
