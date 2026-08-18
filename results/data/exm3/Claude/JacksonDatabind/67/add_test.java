// com/fasterxml/jackson/databind/module/TestCustomEnumKeyDeserializer.java
public void testEnumKeyWithoutModifier() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();

        // Test enum as key without any modifier
        EnumMap<KeyEnum,String> map = mapper.readValue(
                aposToQuotes("{'replacements':'value1'}"),
                new TypeReference<EnumMap<KeyEnum,String>>() { });
        assertEquals(1, map.size());
        assertSame(KeyEnum.replacements, map.keySet().iterator().next());
        assertEquals("value1", map.get(KeyEnum.replacements));
    }