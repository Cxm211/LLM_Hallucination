// com/fasterxml/jackson/databind/deser/TestJDKAtomicTypes.java
public void testNonAbsentWithPresentAtomicReference() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

        Issue1256Bean bean = new Issue1256Bean();
        bean.ref = new AtomicReference<String>();
        bean.ref.set("present");
        
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"ref\":\"present\"}", json);
    }