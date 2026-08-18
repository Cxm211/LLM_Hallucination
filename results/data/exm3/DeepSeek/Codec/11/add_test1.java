// org/apache/commons/codec/net/QuotedPrintableCodecTest.java
@Test
    public void testEncodeLineLengthLimit() throws Exception {
        String plain = new String(new char[77]).replace('\0', 'A'); // 77 'A's
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        String encoded = qpcodec.encode(plain);
        assertTrue("Encoded string should contain soft line break", encoded.contains("=\r\n"));
    }
