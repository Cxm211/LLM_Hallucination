// com/fasterxml/jackson/databind/misc/AccessFixTest.java
public void testNonCausePropertyOfThrowable() throws Exception
    {
        final SecurityManager origSecMan = System.getSecurityManager();
        try {
            System.setSecurityManager(new CauseBlockingSecurityManager());
            ObjectMapper mapper = new ObjectMapper();
            class CustomThrowable extends Throwable {
                private static final long serialVersionUID = 1L;
                public String customField;
                public void setCustomField(String value) {
                    this.customField = value;
                }
            }
            CustomThrowable ex = mapper.readValue("{\"customField\":\"value\"}", CustomThrowable.class);
            assertEquals("value", ex.customField);
        } finally {
            System.setSecurityManager(origSecMan);
        }
    }