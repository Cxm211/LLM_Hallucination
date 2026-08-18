// com/fasterxml/jackson/databind/misc/AccessFixTest.java
public void testCauseOfThrowableIgnoralSubclass() throws Exception
    {
        final SecurityManager origSecMan = System.getSecurityManager();
        try {
            System.setSecurityManager(new CauseBlockingSecurityManager());
            ObjectMapper mapper = new ObjectMapper();
            class MyException extends Throwable {
                private static final long serialVersionUID = 1L;
                public String message;
            }
            MyException ex = mapper.readValue("{\"message\":\"test\"}", MyException.class);
            assertEquals("test", ex.message);
        } finally {
            System.setSecurityManager(origSecMan);
        }
    }