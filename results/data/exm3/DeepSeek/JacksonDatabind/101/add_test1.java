// com/fasterxml/jackson/databind/struct/TestUnwrapped.java
public void testUnwrappedFieldOnlyAfterLastCreatorProperty() throws Exception
    {
        Issue2088Bean bean = MAPPER.readValue("{\"x\":1,\"y\":3,\"b\":4}", Issue2088Bean.class);
        assertEquals(1, bean.x);
        assertEquals(3, bean.y);
        assertEquals(4, bean.w.b);
    }
