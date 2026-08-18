// org/apache/commons/compress/compressors/bzip2/BZip2CompressorInputStreamTest.java
@Test
public void readOfLength0ShouldReturn0AtStreamEnd() throws Exception {
    byte[] rawData = new byte[10];
    for (int i = 0; i < rawData.length; ++i) {
        rawData[i] = (byte) i;
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
    byte[] buffer = new byte[20];
    
    int totalRead = 0;
    int n;
    while ((n = bzipIn.read(buffer, totalRead, buffer.length - totalRead)) > 0) {
        totalRead += n;
    }
    
    Assert.assertEquals(10, totalRead);
    Assert.assertEquals(0, bzipIn.read(buffer, 0, 0));
    Assert.assertEquals(-1, bzipIn.read(buffer, 0, buffer.length));
    bzipIn.close();
}