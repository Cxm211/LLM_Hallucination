// com/fasterxml/jackson/databind/seq/ReadValuesTest.java::testRootBeans
public void testReadValuesWithOffset() throws Exception {
        ObjectMapper m = new ObjectMapper();
        ObjectReader r = m.readerFor(Integer.class);

        byte[] data = "123".getBytes("UTF-8");
        byte[] prefix = "xxx".getBytes("UTF-8"); // invalid JSON to ensure failure if offset ignored
        byte[] buffer = new byte[prefix.length + data.length];
        System.arraycopy(prefix, 0, buffer, 0, prefix.length);
        System.arraycopy(data, 0, buffer, prefix.length, data.length);

        MappingIterator<Integer> it = r.readValues(buffer, prefix.length, data.length);
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(123), it.next());
        assertFalse(it.hasNext());
    }