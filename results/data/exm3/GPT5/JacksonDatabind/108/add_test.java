// com/fasterxml/jackson/databind/node/EmptyContentAsTreeTest.java::testNullFromEOFWithCharArrayParser
public void testNullFromEOFWithCharArrayParser() throws Exception {
        try (JsonParser p = MAPPER.getFactory().createParser(new char[0])) {
            _assertNullTree(MAPPER.reader().readTree(p));
        }
        char[] oneSpace = new char[] { ' ' };
        try (JsonParser p = MAPPER.getFactory().createParser(oneSpace, 0, oneSpace.length)) {
            _assertNullTree(MAPPER.reader().readTree(p));
        }
    }