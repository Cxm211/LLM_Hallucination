// org/apache/commons/lang3/math/NumberUtilsTest.java
@Test
public void testCreateNumberAdditional() {
    // Test negative hex with 8 digits returns Integer
    Number num = NumberUtils.createNumber("-0x12345678");
    assertTrue(num instanceof Integer);
    assertEquals(Integer.valueOf(-0x12345678), num);

    // Test positive hex with 8 digits returns Integer
    num = NumberUtils.createNumber("0x12345678");
    assertTrue(num instanceof Integer);
    assertEquals(Integer.valueOf(0x12345678), num);

    // Test invalid strings with both 'e' and 'E'
    try {
        NumberUtils.createNumber("1e2E3");
        fail("Expected NumberFormatException for \"1e2E3\"");
    } catch (NumberFormatException nfe) {
        // expected
    }

    // Test suffix only strings
    String[] suffixOnly = {"L", "l", "F", "f", "D", "d"};
    for (String s : suffixOnly) {
        try {
            NumberUtils.createNumber(s);
            fail("Expected NumberFormatException for \"" + s + "\"");
        } catch (NumberFormatException nfe) {
            // expected
        }
    }
}
