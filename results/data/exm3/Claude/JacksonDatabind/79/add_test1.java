// com/fasterxml/jackson/databind/objectid/AlwaysAsReferenceFirstTest.java
public void testObjectIdReferenceWithFalseFlag() throws Exception
    {
        @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
        @JsonIdentityReference(alwaysAsId=false)
        class NotAlwaysAsIdClass {
            public int value = 99;
        }
        
        String json = MAPPER.writeValueAsString(new NotAlwaysAsIdClass());
        assertTrue(json.contains("@id"));
        assertTrue(json.contains("value"));
    }