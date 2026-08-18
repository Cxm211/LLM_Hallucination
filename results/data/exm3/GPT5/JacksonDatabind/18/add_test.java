// com/fasterxml/jackson/databind/seq/ReadRecoveryTest.java::testSimpleArrayRecovery
public void testArrayRecoveryAtEnd() throws Exception
    {
        final String JSON = aposToQuotes("[{'a':3},{'x':1}]");

        MappingIterator<Bean> it = MAPPER.readerFor(Bean.class).readValues(JSON);
        Bean bean = it.nextValue();
        assertNotNull(bean);
        assertEquals(3, bean.a);

        try {
            it.nextValue();
            fail("Should not have succeeded");
        } catch (JsonMappingException e) {
            // expected
        }

        assertFalse(it.hasNextValue());
        it.close();
    }