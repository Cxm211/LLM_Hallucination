// org/apache/commons/lang3/RandomStringUtilsTest.java
public void testLANG807_WithCharsArrayEndExceedsLength() {
        try {
            char[] chars = new char[]{'a', 'b', 'c'};
            RandomStringUtils.random(3, 0, 5, false, false, chars, new Random());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            final String msg = ex.getMessage();
            assertTrue("Message (" + msg + ") must contain 'end'", msg.contains("end"));
        }
    }