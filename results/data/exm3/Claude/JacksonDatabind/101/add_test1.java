// com/fasterxml/jackson/databind/struct/TestUnwrapped.java
public void testIssue2088UnwrappedFieldsBeforeAndAfterFirstCreatorProp() throws Exception
    {
        Issue2088Bean bean = MAPPER.readValue("{\"a\":2,\"x\":1,\"b\":4,\"y\":3}", Issue2088Bean.class);
        assertEquals(1, bean.x);
        assertEquals(2, bean.w.a);
        assertEquals(3, bean.y);
        assertEquals(4, bean.w.b);
    }