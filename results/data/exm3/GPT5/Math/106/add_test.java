// org/apache/commons/math/fraction/FractionFormatTest.java::testParseProperInvalidMinusNoSlash
public void testParseProperInvalidMinusNoSlash() {
        String source = "2 -2";
        try {
            Fraction c = properFormat.parse(source);
            fail("invalid minus in proper fraction without slash.");
        } catch (ParseException ex) {
            // expected
        }
    }