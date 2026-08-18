// com/fasterxml/jackson/core/util/TestDefaultPrettyPrinter.java
public void testInvalidAnonymousSubClass() throws Exception {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter() { };
        try {
            pp.createInstance();
            fail("Should not pass");
        } catch (IllegalStateException e) {
            verifyException(e, "does not override");
        }
    }