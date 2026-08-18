// com/fasterxml/jackson/databind/convert/TestUpdateValue.java::testRootDeserializerMismatch
public void testRootDeserializerMismatch() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(DataA.class, new DataADeserializer());
        mapper.registerModule(module);

        DataB db = new DataB();
        db.da.i = 11;
        db.k = 13;
        String jsonBString = mapper.writeValueAsString(db);

        // Create a reader pre-configured for JsonNode (which may cache a root deserializer)
        ObjectReader r = mapper.readerFor(JsonNode.class);
        // Then switch to a different target type; must not reuse the cached JsonNode deserializer
        ObjectReader r2 = r.forType(DataB.class);

        DataB result = r2.readValue(jsonBString);
        assertEquals(5, result.da.i);
        assertEquals(13, result.k);
    }