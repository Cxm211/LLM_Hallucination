// org/apache/commons/collections4/IteratorUtilsTest.java
@Test
    public void testCollatedIteratorWithNoIterators() {
        Iterator<Object> it = IteratorUtils.collatedIterator(null);
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }
