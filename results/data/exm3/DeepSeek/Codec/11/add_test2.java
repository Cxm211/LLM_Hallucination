// org/apache/commons/codec/net/QuotedPrintableCodecTest.java
@Test
    public void testDecodeInvalidHexThrows() throws Exception {
        String qpdata = "=GG";
        QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        try {
            qpcodec.decode(qpdata);
            fail("Expected DecoderException");
        } catch (DecoderException e) {
            // expected
        }
    }
