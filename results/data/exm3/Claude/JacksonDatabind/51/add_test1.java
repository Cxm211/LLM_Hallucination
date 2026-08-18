// com/fasterxml/jackson/databind/jsontype/TestCustomTypeIdResolver.java
public void testPolymorphicTypeViaCustomWithDifferentTypeClass() throws Exception {
        String json = "{\"b\":{\"@type\":\"base1270\",\"val\":\"some value\",\"options\":{\"@type\":\"poly1\",\"val\":\"optionValue\"}}}";
        Top1270 itemRead = MAPPER.readValue(json, Top1270.class);
        assertNotNull(itemRead);
        assertNotNull(itemRead.b);
        assertEquals("some value", itemRead.b.val);
        assertNotNull(itemRead.b.options);
        assertTrue(itemRead.b.options instanceof Poly1);
        assertEquals("optionValue", ((Poly1)itemRead.b.options).val);
    }