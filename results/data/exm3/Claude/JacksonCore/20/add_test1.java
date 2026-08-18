// com/fasterxml/jackson/core/main/TestGeneratorMisc.java
public void testAsEmbeddedInArray() throws Exception
{
    JsonGenerator g;

    StringWriter sw = new StringWriter();
    g = JSON_F.createGenerator(sw);
    g.writeStartArray();
    g.writeEmbeddedObject(null);
    g.writeEmbeddedObject(new byte[]{1, 2, 3});
    g.writeEndArray();
    g.close();
    String result = sw.toString();
    assertTrue(result.startsWith("[null,\""));
    assertTrue(result.endsWith("\"]"));
}