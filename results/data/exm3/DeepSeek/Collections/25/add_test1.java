// org/apache/commons/collections4/IteratorUtilsTest.java
@Test
    public void testCollatedIteratorWithEmptyCollection() {
        Collection<Iterator<Object>> empty = new ArrayList<>();
        Iterator<Object> it = IteratorUtils.collatedIterator(null, empty);
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }
