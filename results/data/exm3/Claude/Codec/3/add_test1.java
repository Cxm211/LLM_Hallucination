// org/apache/commons/codec/language/DoubleMetaphone2Test.java
@Test
public void testDoubleMetaphoneAlternateAGGR() {
    String word = "BAGGRO";
    String result = doubleMetaphone.doubleMetaphone(word, true);
    assertNotNull("Result should not be null", result);
    assertTrue("Result should contain alternate encoding for AGGR", result.length() > 0);
}