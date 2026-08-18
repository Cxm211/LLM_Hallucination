// org/apache/commons/lang3/RandomStringUtilsTest.java
public void testLANG807_StartGreaterThanEnd() {
        try {
            RandomStringUtils.random(3,10,5,false,false);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            final String msg = ex.getMessage();
            assertTrue("Message (" + msg + ") must contain 'start'", msg.contains("start"));
            assertTrue("Message (" + msg + ") must contain 'end'", msg.contains("end"));
        }
    }