// com/fasterxml/jackson/core/util/TestDefaultPrettyPrinter.java
public void testInvalidSubClassMultiLevel() throws Exception {
        class Level1 extends DefaultPrettyPrinter {}
        class Level2 extends Level1 {}
        DefaultPrettyPrinter pp = new Level2();
        try {
            pp.createInstance();
            fail("Should not pass");
        } catch (IllegalStateException e) {
            verifyException(e, "does not override");
        }
    }
