// com/fasterxml/jackson/databind/ser/TestJsonSerialize.java
public void testSpecializedAnnotation() throws Exception
    {
        // Test case where currRaw is assignable from serClass (specialization)
        class BaseClass { }
        class SubClass extends BaseClass { }
        class Container {
            @JsonSerialize(as = SubClass.class)
            public BaseClass value = new SubClass();
        }
        
        String json = serializeAsString(MAPPER, new Container());
        assertNotNull(json);
    }