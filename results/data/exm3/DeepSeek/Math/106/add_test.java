// org/apache/commons/math/fraction/FractionFormatTest.java
public void testParseProperInvalidZeroDenominator() {
        String source = "2 2 / 0";
        try {
            Fraction c = properFormat.parse(source);
            fail("zero denominator in proper fraction.");
        } catch (ParseException ex) {
            // expected
        }
    }
