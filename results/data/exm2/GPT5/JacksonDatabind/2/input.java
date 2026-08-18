    public void writeObject(Object value) throws IOException
    {
            _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
            /* 28-May-2014, tatu: Tricky choice here; if no codec, should we
             *   err out, or just embed? For now, do latter.
             */
//          throw new JsonMappingException("No ObjectCodec configured for TokenBuffer, writeObject() called");
    }

    public void writeTree(TreeNode node) throws IOException
    {

            // as with 'writeObject()', is codec optional?
            _append(JsonToken.VALUE_EMBEDDED_OBJECT, node);
    }

// trigger testcase
public void testConversionOfPojos() throws Exception
    {
        final Issue467Bean input = new Issue467Bean(13);
        final String EXP = "{\"x\":13}";
        
        // first, sanity check
        String json = MAPPER.writeValueAsString(input);
        assertEquals(EXP, json);

        // then via conversions: should become JSON Object
        JsonNode tree = MAPPER.valueToTree(input);
        assertTrue("Expected Object, got "+tree.getNodeType(), tree.isObject());
        assertEquals(EXP, MAPPER.writeValueAsString(tree));
    }
