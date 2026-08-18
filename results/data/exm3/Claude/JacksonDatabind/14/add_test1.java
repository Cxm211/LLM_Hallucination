// com/fasterxml/jackson/databind/convert/TestUpdateValue.java
public void testUpdateWithEmptyArray() throws IOException
{
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(DataA.class, new DataADeserializer());
    mapper.registerModule(module);
    
    JsonNode emptyArray = mapper.createArrayNode();
    DataB db = new DataB();
    db.da.i = 10;
    db.k = 20;
    
    ObjectReader reader = mapper.readerForUpdating(db);
    try {
        reader.readValue(emptyArray);
    } catch (Exception e) {
        // Expected to handle empty array case
    }
}