// com/fasterxml/jackson/databind/node/EmptyContentAsTreeTest.java
public void testNullFromEOFAfterConsuming() throws Exception
{
    try (JsonParser p = MAPPER.getFactory().createParser("{}")) {
        // consume all tokens to reach EOF
        while (p.nextToken() != null) {
            // do nothing
        }
        _assertNullTree(MAPPER.reader().readTree(p));
    }
}
