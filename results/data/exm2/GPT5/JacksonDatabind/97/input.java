    public final void serialize(JsonGenerator gen, SerializerProvider ctxt) throws IOException
    {
        if (_value == null) {
            ctxt.defaultSerializeNull(gen);
        } else if (_value instanceof JsonSerializable) {
            ((JsonSerializable) _value).serialize(gen, ctxt);
        } else {
            // 25-May-2018, tatu: [databind#1991] do not call via generator but through context;
            //    this to preserve contextual information
            gen.writeObject(_value);
        }
    }

// trigger testcase
public void testPOJONodeCustomSer() throws Exception
    {
      Data data = new Data();
      data.aStr = "Hello";

      Map<String, Object> mapTest = new HashMap<>();
      mapTest.put("data", data);

      ObjectNode treeTest = MAPPER.createObjectNode();
      treeTest.putPOJO("data", data);

      final String EXP = "{\"data\":{\"aStr\":\"The value is: Hello!\"}}";
      
      String mapOut = MAPPER.writer().withAttribute("myAttr", "Hello!").writeValueAsString(mapTest);
      assertEquals(EXP, mapOut);

      String treeOut = MAPPER.writer().withAttribute("myAttr", "Hello!").writeValueAsString(treeTest);
      assertEquals(EXP, treeOut);
    }
