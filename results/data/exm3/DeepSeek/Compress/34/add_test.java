// org/apache/commons/compress/archivers/zip/X7875_NewUnixTest.java
@Test
    public void testGetCentralDirectoryLength() {
        X7875_NewUnix xf = new X7875_NewUnix();
        long[][] cases = {
            {0, 0, 3},
            {1, 0, 4},
            {0, 1, 4},
            {1, 1, 5},
            {256, 256, 7},
            {65536, 65536, 9},
            {0x100000000L, 0, 8},
            {0x100000000L, 0x100000000L, 13},
            {Long.MAX_VALUE, Long.MAX_VALUE, 19}
        };
        for (long[] cas : cases) {
            long uid = cas[0];
            long gid = cas[1];
            int expected = (int) cas[2];
            xf.setUID(uid);
            xf.setGID(gid);
            assertEquals(new ZipShort(expected), xf.getCentralDirectoryLength());
        }
    }
