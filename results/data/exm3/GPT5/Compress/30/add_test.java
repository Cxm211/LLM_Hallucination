// org/apache/commons/compress/compressors/bzip2/BZip2CompressorInputStreamTest.java::readOfLength0ShouldReturn0
@Test
public void readOfLength0AtEOFShouldReturn0() throws Exception {
    byte[] rawData = new byte[4096];
    for (int i = 0; i < rawData.length; ++i) {
        rawData[i] = (byte) (i & 0xFF);
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BZip2CompressorOutputStream bzipOut = new BZip2CompressorOutputStream(baos);
    bzipOut.write(rawData);
    bzipOut.close();

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    BZip2CompressorInputStream bzipIn = new BZip2CompressorInputStream(bais);

    byte[] buffer = new byte[1024];
    while (bzipIn.read(buffer, 0, buffer.length) != -1) {
        // drain to EOF
    }
    // At EOF, a zero-length read must return 0, not -1
    Assert.assertEquals(0, bzipIn.read(buffer, 0, 0));
    bzipIn.close();
}