// com/fasterxml/jackson/core/sym/TestByteBasedSymbols.java
public void testLargeSpilloverArea() throws Exception
{
    ByteQuadsCanonicalizer nc = ByteQuadsCanonicalizer.createRoot(999999);
    Field byteSymbolCanonicalizerField = JsonFactory.class.getDeclaredField("_byteSymbolCanonicalizer");
    byteSymbolCanonicalizerField.setAccessible(true);
    JsonFactory jsonF = new JsonFactory();
    byteSymbolCanonicalizerField.set(jsonF, nc);

    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("{\n");
    stringBuilder.append("    \"field0\": null");
    for (int i = 1; i < 100; ++i) {
        stringBuilder.append(",\n    \"").append("field" + i).append("\": null");
    }
    stringBuilder.append("\n}");

    JsonParser p = jsonF.createParser(stringBuilder.toString().getBytes("UTF-8"));
    while (p.nextToken() != null) { }
    p.close();
}