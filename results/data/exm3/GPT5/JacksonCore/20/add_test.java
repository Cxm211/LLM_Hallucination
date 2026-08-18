// com/fasterxml/jackson/core/base64/Base64GenerationTest.java::testBinaryAsEmbeddedObject
public void testEmptyBinaryAsEmbeddedObject() throws Exception
    {
        JsonGenerator g;

        StringWriter sw = new StringWriter();
        g = JSON_F.createGenerator(sw);
        g.writeEmbeddedObject(new byte[0]);
        g.close();
        assertEquals(quote(""), sw.toString());

        ByteArrayOutputStream bytes =  new ByteArrayOutputStream(10);
        g = JSON_F.createGenerator(bytes);
        g.writeEmbeddedObject(new byte[0]);
        g.close();
        assertEquals(quote(""), bytes.toString("UTF-8"));
    }