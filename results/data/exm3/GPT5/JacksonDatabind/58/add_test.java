// com/fasterxml/jackson/databind/misc/AccessFixTest.java::testCauseDeserializationIgnoral
public void testCauseDeserializationIgnoral() throws Exception {
        final SecurityManager origSecMan = System.getSecurityManager();
        try {
            System.setSecurityManager(new CauseBlockingSecurityManager());
            ObjectMapper mapper = new ObjectMapper();
            // Should not trigger forced access to Throwable.cause
            mapper.readValue("{\"cause\":null}", Throwable.class);
        } finally {
            System.setSecurityManager(origSecMan);
        }
    }