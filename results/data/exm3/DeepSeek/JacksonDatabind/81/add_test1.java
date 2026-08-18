// com/fasterxml/jackson/databind/introspect/TypeCoercion1592Test.java
public void testPrimitiveWrapperArrayContent() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Content type: primitive int to wrapper Integer in array
        @JsonSerialize(contentAs = Integer.class)
        @JsonDeserialize(contentAs = Integer.class)
        class Bean {
            public int[] values;
            public Bean() {
                values = new int[] {1, 2, 3};
            }
        }
        Bean bean = new Bean();
        String json = mapper.writeValueAsString(bean);
        Bean result = mapper.readValue(json, Bean.class);
        assertNotNull(result);
        assertArrayEquals(new int[] {1, 2, 3}, result.values);
    }
