// com/fasterxml/jackson/databind/struct/TestUnwrapped.java::testIssue2088OnlyUnwrappedAfterCreators
public void testIssue2088OnlyUnwrappedAfterCreators() throws Exception
    {
        Issue2088Bean bean = MAPPER.readValue("{\"x\":1,\"y\":3,\"a\":2,\"b\":4}", Issue2088Bean.class);
        assertEquals(1, bean.x);
        assertEquals(3, bean.y);
        assertEquals(2, bean.w.a);
        assertEquals(4, bean.w.b);
    }