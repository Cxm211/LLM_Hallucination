// org/apache/commons/math/fraction/FractionFormatTest.java
public void testParseProperInvalidMinusWholeNegativeNumeratorNegative() {
        String source = "-2 -2 / 3";
        try {
            Fraction c = properFormat.parse(source);
            fail("invalid minus in numerator with negative whole.");
        } catch (ParseException ex) {
            // expected
        }
    }
