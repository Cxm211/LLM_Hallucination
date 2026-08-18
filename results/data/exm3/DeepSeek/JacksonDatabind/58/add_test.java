// com/fasterxml/jackson/databind/misc/AccessFixTest.java
public void testCauseOfThrowableIgnoralWithOverrideDisabled() throws Exception {
        final SecurityManager origSecMan = System.getSecurityManager();
        try {
            System.setSecurityManager(new CauseBlockingSecurityManager());
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS);
            // Attempt to deserialize a Throwable subclass that has cause property
            // This should not throw a SecurityException because fixAccess should be skipped.
            mapper.readValue("{\"message\":\"test\"}", Throwable.class);
        } finally {
            System.setSecurityManager(origSecMan);
        }
    }
