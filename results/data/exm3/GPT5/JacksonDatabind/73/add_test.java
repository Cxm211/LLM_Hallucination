// com/fasterxml/jackson/databind/deser/ReadOrWriteOnlyTest.java
public void testOnlyWriteOnlySerialization() throws Exception
    {
        class OnlyWriteOnly {
            private int y = 3;
            @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
            public void setY(int v) { y = v; }
        }
        String json = MAPPER.writeValueAsString(new OnlyWriteOnly());
        assertEquals("{}", json);
        OnlyWriteOnly r = MAPPER.readValue("{\"y\":7}", OnlyWriteOnly.class);
        assertNotNull(r);
    }