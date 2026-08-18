// org/apache/commons/lang3/math/NumberUtilsTest.java
@Test
public void testCreateNumberAdditional() {
    // Test empty numeric for long suffix
    try {
        NumberUtils.createNumber("L");
        fail("Expected NumberFormatException for 'L'");
    } catch (NumberFormatException e) {
        // expected
    }
    try {
        NumberUtils.createNumber("l");
        fail("Expected NumberFormatException for 'l'");
    } catch (NumberFormatException e) {
        // expected
    }

    // Test double suffix with small exponent (float zero but double non-zero)
    Number d = NumberUtils.createNumber("1.0e-1000d");
    assertTrue("Should return Double for 1.0e-1000d", d instanceof Double);
    assertEquals(Double.valueOf(1.0e-1000), d);

    Number d2 = NumberUtils.createNumber("2.0e-1000d");
    assertTrue("Should return Double for 2.0e-1000d", d2 instanceof Double);
    assertEquals(Double.valueOf(2.0e-1000), d2);
}
