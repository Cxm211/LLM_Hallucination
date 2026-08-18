// com/fasterxml/jackson/databind/deser/ReadOrWriteOnlyTest.java
public void testAutoWithInferMutatorsAndNonVisibleSetter() throws Exception {
        // Define a local class with a getter and a setter marked with @JsonIgnore
        class LocalBean {
            private int value = 42;
            public int getValue() { return value; }
            @com.fasterxml.jackson.annotation.JsonIgnore
            public void setValue(int v) { value = v; }
        }
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.enable(com.fasterxml.jackson.databind.MapperFeature.INFER_PROPERTY_MUTATORS);
        LocalBean bean = new LocalBean();
        String json = mapper.writeValueAsString(bean);
        // Deserialize with property present; setter should be ignored
        LocalBean result = mapper.readValue("{\"value\":100}", LocalBean.class);
        // Since setter is ignored, value should remain 42
        assertEquals(42, result.value);
    }
