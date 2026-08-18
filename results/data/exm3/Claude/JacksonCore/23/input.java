// buggy function
    public DefaultPrettyPrinter createInstance() {
        return new DefaultPrettyPrinter(this);
    }

// trigger testcase
// com/fasterxml/jackson/core/util/TestDefaultPrettyPrinter.java::testInvalidSubClass
public void testInvalidSubClass() throws Exception
    {
        DefaultPrettyPrinter pp = new MyPrettyPrinter();
        try {
            pp.createInstance();
            fail("Should not pass");
        } catch (IllegalStateException e) {
            verifyException(e, "does not override");
        }
    }
