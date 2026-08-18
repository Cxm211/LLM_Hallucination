// buggy function
    public JsonNode setAll(Map<String,? extends JsonNode> properties)
    {
        for (Map.Entry<String,? extends JsonNode> en : properties.entrySet()) {
            JsonNode n = en.getValue();
            if (n == null) {
                n = nullNode();
            }
            _children.put(en.getKey(), n);
        }
        return this;
    }

// trigger testcase
// com/fasterxml/jackson/databind/introspect/TestNamingStrategyStd.java::testNamingWithObjectNode
public void testNamingWithObjectNode() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE);
        ClassWithObjectNodeField result =
            m.readValue(
                "{ \"id\": \"1\", \"json\": { \"foo\": \"bar\", \"baz\": \"bing\" } }",
                ClassWithObjectNodeField.class);
        assertNotNull(result);
        assertEquals("1", result.id);
        assertNotNull(result.json);
        assertEquals(2, result.json.size());
        assertEquals("bing", result.json.path("baz").asText());
    }
