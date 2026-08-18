// com/fasterxml/jackson/databind/deser/TestJdkTypes.java
public void testInetSocketAddressHostInvalidPort() throws IOException {
        try {
            MAPPER.readValue(quote("example.com:xyz"), InetSocketAddress.class);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // expected
        }
    }
