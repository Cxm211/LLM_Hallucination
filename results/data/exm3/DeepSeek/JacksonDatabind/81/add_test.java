// com/fasterxml/jackson/databind/introspect/TypeCoercion1592Test.java
public void testPrimitiveWrapperKeyAndContent() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Key type: primitive int to wrapper Integer
        // Content type: primitive boolean to wrapper Boolean
        @JsonSerialize(keyAs = Integer.class, contentAs = Boolean.class)
        @JsonDeserialize(keyAs = Integer.class, contentAs = Boolean.class)
        class Bean {
            public Map<Integer, Boolean> map;
            public Bean() {
                map = new HashMap<>();
                map.put(1, true);
            }
        }
        Bean bean = new Bean();
        String json = mapper.writeValueAsString(bean);
        Bean result = mapper.readValue(json, Bean.class);
        assertNotNull(result);
        assertNotNull(result.map);
        assertEquals(Boolean.TRUE, result.map.get(1));
    }
