// org/apache/commons/collections4/collection/UnmodifiableBoundedCollectionTest.java
public void testDecorateFactoryWithUnmodifiableButNotWrapper() {
        class DummyUnmodifiableBoundedCollection<T> extends java.util.AbstractCollection<T> implements BoundedCollection<T>, Unmodifiable {
            private final java.util.Collection<T> data = new java.util.ArrayList<>();
            @Override public java.util.Iterator<T> iterator() { return data.iterator(); }
            @Override public int size() { return data.size(); }
            @Override public boolean add(T e) { throw new UnsupportedOperationException(); }
            @Override public boolean isFull() { return false; }
            @Override public int maxSize() { return 100; }
        }
        final BoundedCollection<Object> coll = new DummyUnmodifiableBoundedCollection<>();
        assertSame(coll, UnmodifiableBoundedCollection.unmodifiableBoundedCollection(coll));
    }