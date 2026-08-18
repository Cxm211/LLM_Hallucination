// org/apache/commons/codec/net/QuotedPrintableCodecTest.java
@Test
    public void testDecodeSoftLineBreakAtStart() throws Exception {
        String qpdata = "=\r\nHello";
        String expected = "Hello";
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        assertEquals(expected, qpcodec.decode(qpdata));
    }
