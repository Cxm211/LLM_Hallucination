// org/apache/commons/math/fraction/FractionFormatTest.java
public void testParseProperWithoutSlash() {
    String source = "2 3";
    try {
        Fraction c = properFormat.parse(source);
        assertEquals(3, c.getNumerator());
        assertEquals(1, c.getDenominator());
    } catch (ParseException ex) {
        fail("Should parse fraction without slash.");
    }
}