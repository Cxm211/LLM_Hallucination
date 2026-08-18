// com/fasterxml/jackson/databind/objectid/AlwaysAsReferenceFirstTest.java
public void testObjectIdWithoutReferenceAnnotation() throws Exception
    {
        @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
        class SimpleIdClass {
            public int value = 42;
        }
        
        String json = MAPPER.writeValueAsString(new SimpleIdClass());
        assertTrue(json.contains("@id"));
        assertTrue(json.contains("value"));
    }