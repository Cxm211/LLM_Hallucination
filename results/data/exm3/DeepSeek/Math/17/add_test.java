// org/apache/commons/math3/dfp/DfpTest.java
@Test
    public void testMultiplyAdditional() {
        // 1. ninf * -1 -> pinf
        test(ninf.multiply(-1),
             pinf,
             0, "Multiply Additional #1");
        // 2. pinf * -1 -> ninf
        test(pinf.multiply(-1),
             ninf,
             0, "Multiply Additional #2");
        // 3. large positive * -2 -> ninf with overflow
        test(field.newDfp("5e131071").multiply(-2),
             ninf,
             DfpField.FLAG_OVERFLOW, "Multiply Additional #3");
        // 4. nan * 0 -> nan
        test(nan.multiply(0),
             nan,
             0, "Multiply Additional #4");
    }
