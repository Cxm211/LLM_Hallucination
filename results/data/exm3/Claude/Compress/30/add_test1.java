// org/apache/commons/compress/compressors/bzip2/BZip2CompressorInputStreamTest.java
@Test
public void readOfLength0AtStartShouldReturn0() throws Exception {
    byte[] rawData = new byte[100];
    for (int i = 0; i < rawData.length; ++i) {
        rawData[i] = (byte) (i % 256);
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BZip2CompressorOutputStream bzipOut = new BZip2CompressorOutputStream(baos);
    bzipOut.write(rawData);
    bzipOut.flush();
    bzipOut.close();
    baos.flush();
    baos.close();

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    BZip2CompressorInputStream bzipIn = new BZip2CompressorInputStream(bais);
    byte[] buffer = new byte[50];
    
    Assert.assertEquals(0, bzipIn.read(buffer, 0, 0));
    Assert.assertEquals(50, bzipIn.read(buffer, 0, 50));
    Assert.assertEquals(0, bzipIn.read(buffer, 25, 0));
    bzipIn.close();
}