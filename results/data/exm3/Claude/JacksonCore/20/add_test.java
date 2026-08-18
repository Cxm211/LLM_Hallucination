// com/fasterxml/jackson/core/base64/Base64GenerationTest.java
public void testBinaryAsEmbeddedObjectEmpty() throws Exception
{
    JsonGenerator g;

    StringWriter sw = new StringWriter();
    g = JSON_F.createGenerator(sw);
    g.writeEmbeddedObject(new byte[0]);
    g.close();
    assertEquals("\"\"", sw.toString());

    ByteArrayOutputStream bytes = new ByteArrayOutputStream(100);
    g = JSON_F.createGenerator(bytes);
    g.writeEmbeddedObject(new byte[0]);
    g.close();
    assertEquals("\"\"", bytes.toString("UTF-8"));
}