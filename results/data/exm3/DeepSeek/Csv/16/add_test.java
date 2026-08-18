// org/apache/commons/csv/CSVParserTest.java
@Test
    public void testIteratorDiscardWithHasNext() throws IOException {
        final String data = "1\n2\n3";
        CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(data));
        // First iterator: call hasNext() and discard
        Iterator<CSVRecord> iter1 = parser.iterator();
        iter1.hasNext(); // This should prefetch first record in buggy version
        // iter1 is not used further, effectively discarded
        // Second iterator: should start from the first record
        Iterator<CSVRecord> iter2 = parser.iterator();
        assertTrue(iter2.hasNext());
        CSVRecord record = iter2.next();
        assertEquals("1", record.get(0));
        // Continue to ensure sequence
        assertTrue(iter2.hasNext());
        record = iter2.next();
        assertEquals("2", record.get(0));
        assertTrue(iter2.hasNext());
        record = iter2.next();
        assertEquals("3", record.get(0));
        assertFalse(iter2.hasNext());
    }
