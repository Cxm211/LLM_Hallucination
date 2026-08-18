    public DefaultPrettyPrinter createInstance() {
        return new DefaultPrettyPrinter(this);
    }

// trigger testcase
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
