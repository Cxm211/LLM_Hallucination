// org/apache/commons/codec/language/DoubleMetaphone2Test.java
@Test
public void testDoubleMetaphoneAlternateOGGR() {
    String word = "POGGRO";
    String result = doubleMetaphone.doubleMetaphone(word, true);
    assertNotNull("Result should not be null", result);
    assertTrue("Result should contain alternate encoding for OGGR", result.length() > 0);
}