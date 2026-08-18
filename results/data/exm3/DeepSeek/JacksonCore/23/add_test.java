// com/fasterxml/jackson/core/util/TestDefaultPrettyPrinter.java
public void testInvalidSubClassAnonymous() throws Exception {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter() {};
        try {
            pp.createInstance();
            fail("Should not pass");
        } catch (IllegalStateException e) {
            verifyException(e, "does not override");
        }
    }
