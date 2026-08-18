// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
@Test
public void testRoundTripNamesWithNegativeBytes(){
    checkName("\u0080");
    checkName("\u00FF");
    checkName("\u0081\u0082\u0083");
    checkName("Test\u00E9\u00F1");
}