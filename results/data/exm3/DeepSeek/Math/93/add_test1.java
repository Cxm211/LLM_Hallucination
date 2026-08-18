// org/apache/commons/math/util/MathUtilsTest.java
@Test
    public void testFactorialLogNegativeN() {
        try {
            MathUtils.factorialLog(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(">= 0"));
        }
    }
