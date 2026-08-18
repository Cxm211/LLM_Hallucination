// buggy function
        public ObjectNode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            if (p.getCurrentToken() == JsonToken.START_OBJECT) {
                p.nextToken();
                return deserializeObject(p, ctxt, ctxt.getNodeFactory());
            }
            // 23-Sep-2015, tatu: Ugh. We may also be given END_OBJECT (similar to FIELD_NAME),
            //    if caller has advanced to the first token of Object, but for empty Object
            if (p.getCurrentToken() == JsonToken.FIELD_NAME) {
                return deserializeObject(p, ctxt, ctxt.getNodeFactory());
            }
            throw ctxt.mappingException(ObjectNode.class);
         }

// trigger testcase
// com/fasterxml/jackson/databind/node/TestObjectNode.java::testIssue941
public void testIssue941() throws Exception
    {
        ObjectNode object = MAPPER.createObjectNode();

        String json = MAPPER.writeValueAsString(object);
        System.out.println("json: "+json);

        ObjectNode de1 = MAPPER.readValue(json, ObjectNode.class);  // this works
        System.out.println("Deserialized to ObjectNode: "+de1);

        MyValue de2 = MAPPER.readValue(json, MyValue.class);  // but this throws exception
        System.out.println("Deserialized to MyValue: "+de2);
    }
