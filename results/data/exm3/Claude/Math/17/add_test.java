// org/apache/commons/math3/dfp/DfpTest.java
@Test
public void testMultiplyAdditional() {
    // Test multiply with large positive integer
    test(field.newDfp("0.5").multiply(10000),
         field.newDfp("5000"),
         0, "Multiply Additional #1");

    // Test multiply with large negative integer
    test(field.newDfp("0.5").multiply(-10000),
         field.newDfp("-5000"),
         0, "Multiply Additional #2");

    // Test multiply with negative integer on negative Dfp
    test(field.newDfp("-3").multiply(-5),
         field.newDfp("15"),
         0, "Multiply Additional #3");

    // Test multiply that causes overflow with negative integer
    test(field.newDfp("-5e131071").multiply(-2),
         pinf,
         DfpField.FLAG_OVERFLOW, "Multiply Additional #4");

    // Test multiply with very small value and integer
    test(field.newDfp("1e-100000").multiply(100),
         field.newDfp("1e-99998"),
         0, "Multiply Additional #5");
}