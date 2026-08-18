// org/apache/commons/lang3/RandomStringUtilsTest.java
public void testLANG807_WithCharsArrayStartEqualsLength() {
        try {
            char[] chars = new char[]{'a', 'b', 'c'};
            RandomStringUtils.random(3, 3, 3, false, false, chars, new Random());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            final String msg = ex.getMessage();
            assertTrue("Message (" + msg + ") must contain 'start'", msg.contains("start"));
        }
    }