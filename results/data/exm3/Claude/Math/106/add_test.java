// org/apache/commons/math/fraction/FractionFormatTest.java
public void testParseProperValidPositive() {
    String source = "2 3 / 4";
    try {
        Fraction c = properFormat.parse(source);
        assertEquals(11, c.getNumerator());
        assertEquals(4, c.getDenominator());
    } catch (ParseException ex) {
        fail("Should parse valid proper fraction.");
    }
}