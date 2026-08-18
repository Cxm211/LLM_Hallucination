// buggy function
    public static <E> BoundedCollection<E> unmodifiableBoundedCollection(final BoundedCollection<? extends E> coll) {
        return new UnmodifiableBoundedCollection<E>(coll);
    }

// trigger testcase
// org/apache/commons/collections4/collection/UnmodifiableBoundedCollectionTest.java::testDecorateFactory
public void testDecorateFactory() {
        final BoundedCollection<E> coll = makeFullCollection();
        assertSame(coll, UnmodifiableBoundedCollection.unmodifiableBoundedCollection(coll));

        try {
            UnmodifiableBoundedCollection.unmodifiableBoundedCollection(null);
            fail();
        } catch (final IllegalArgumentException ex) {}
    }

// org/apache/commons/collections4/collection/UnmodifiableBoundedCollectionTest.java::testUnmodifiable
public void testUnmodifiable() {
        assertTrue(makeObject() instanceof Unmodifiable);
        assertTrue(makeFullCollection() instanceof Unmodifiable);
    }
