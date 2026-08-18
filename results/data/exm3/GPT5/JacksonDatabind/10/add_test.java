// com/fasterxml/jackson/databind/ser/TestAnyGetter.java::testAnyGetterWithAnnotatedCustomSerializer
public void testAnyGetterWithAnnotatedCustomSerializer() throws Exception {
        class AnnSer extends JsonSerializer<Map<String,Object>> {
            @Override
            public void serialize(Map<String,Object> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeFieldName("ann");
                gen.writeString("ok");
            }
        }
        class BeanAnnot {
            private final Map<String,Object> stuff = new LinkedHashMap<String,Object>();
            @JsonAnyGetter
            @JsonSerialize(using = AnnSer.class)
            public Map<String,Object> any() { return stuff; }
        }
        ObjectMapper m = new ObjectMapper();
        String json = m.writeValueAsString(new BeanAnnot());
        assertEquals("{\"ann\":\"ok\"}", json);
    }