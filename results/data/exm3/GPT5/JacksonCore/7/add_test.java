// com/fasterxml/jackson/core/json/GeneratorFailTest.java::testFailOnWritingNumberNotFieldName
public void testFailOnWritingNumberNotFieldName() throws Exception {
        java.io.StringWriter w = new java.io.StringWriter();
        com.fasterxml.jackson.core.JsonGenerator g = F.createGenerator(w);
        g.writeStartObject();
        try {
            g.writeNumber(123);
            fail("Should have failed when trying to write a number value instead of a field name");
        } catch (com.fasterxml.jackson.core.JsonGenerationException e) {
            // expected
        } finally {
            g.close();
        }
    }