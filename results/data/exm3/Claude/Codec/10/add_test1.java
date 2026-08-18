// org/apache/commons/codec/language/CaverphoneTest.java
public void testMbAtStart() throws EncoderException {
    String[][] data = {{"mba", "MP1111111"}, {"mbomb", "MPAN111111"}};
    this.checkEncodings(data);
}