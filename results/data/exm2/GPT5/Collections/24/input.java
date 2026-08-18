    public static <E> BoundedCollection<E> unmodifiableBoundedCollection(final BoundedCollection<? extends E> coll) {
        return new UnmodifiableBoundedCollection<E>(coll);
    }

// trigger testcase
public void testDecorateFactory() {
        final BoundedCollection<E> coll = makeFullCollection();
        assertSame(coll, UnmodifiableBoundedCollection.unmodifiableBoundedCollection(coll));

        try {
            UnmodifiableBoundedCollection.unmodifiableBoundedCollection(null);
            fail();
        } catch (final IllegalArgumentException ex) {}
    }

public void testUnmodifiable() {
        assertTrue(makeObject() instanceof Unmodifiable);
        assertTrue(makeFullCollection() instanceof Unmodifiable);
    }
