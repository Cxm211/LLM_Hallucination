// org/apache/commons/collections4/collection/UnmodifiableBoundedCollectionTest.java
@Test
public void testDecorateFactoryWithAlreadyUnmodifiable() {
    final BoundedCollection<E> coll = makeFullCollection();
    final BoundedCollection<E> unmodColl = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(coll);
    final BoundedCollection<E> doubleUnmodColl = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(unmodColl);
    assertSame(unmodColl, doubleUnmodColl);
}