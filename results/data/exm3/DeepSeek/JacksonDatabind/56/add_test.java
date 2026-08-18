// com/fasterxml/jackson/databind/deser/TestJdkTypes.java
public void testInetSocketAddressIPv6InvalidPort() throws IOException {
        try {
            MAPPER.readValue(quote("[::1]:abc"), InetSocketAddress.class);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // expected
        }
    }
