// org/apache/commons/math/linear/OpenMapRealMatrixTest.java::testOverflowByRows
public void testOverflowByRows() {
        try {
            new OpenMapRealMatrix(Integer.MAX_VALUE, 3);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }