// com/fasterxml/jackson/databind/ser/TestJsonSerialize.java
public void testGeneralizedAnnotation() throws Exception
    {
        // Test case where serClass is assignable from currRaw (generalization)
        class SubClass { }
        class Container {
            @JsonSerialize(as = Object.class)
            public SubClass value = new SubClass();
        }
        
        String json = serializeAsString(MAPPER, new Container());
        assertNotNull(json);
    }