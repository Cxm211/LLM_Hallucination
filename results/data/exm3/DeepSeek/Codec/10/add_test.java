// org/apache/commons/codec/language/CaverphoneTest.java
public void testMbEdgeCases() throws EncoderException {
    String[][] data = {{"amb", "AM11111111"}, {"mbat", "MPT1111111"}, {"coughmb", "cFM1111111"}, {"jamb", "YM11111111"}};
    this.checkEncodings(data);
}
