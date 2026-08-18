// com/fasterxml/jackson/core/json/RawValueWithSurrogatesTest.java
public void testRawWithSurrogatesSplitSegments() throws Exception {
        com.fasterxml.jackson.core.JsonFactory f = new com.fasterxml.jackson.core.JsonFactory();
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        com.fasterxml.jackson.core.JsonGenerator gen = f.createGenerator(out, com.fasterxml.jackson.core.JsonEncoding.UTF8);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append('a');
        }
        int pos = 5000;
        sb.insert(pos, '\uD83D');
        sb.insert(pos+1, '\uDE00');
        gen.writeRaw(sb.toString());
        gen.close();
    }
