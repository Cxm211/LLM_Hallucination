// org/apache/commons/collections4/TrieUtilsTest.java
public void testUnmodifiableTrieWithAlreadyUnmodifiableFromOtherFactory() {
        final Trie<String, Object> base = new PatriciaTrie<Object>();
        final Trie<String, Object> viaUnmodifiableClass = UnmodifiableTrie.unmodifiableTrie(base);
        assertTrue(viaUnmodifiableClass instanceof UnmodifiableTrie);
        assertSame("Should return same instance when already unmodifiable (created via UnmodifiableTrie factory)",
            viaUnmodifiableClass, TrieUtils.unmodifiableTrie(viaUnmodifiableClass));
    }