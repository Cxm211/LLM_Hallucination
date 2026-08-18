// buggy function
    public static <K, V> UnmodifiableTrie<K, V> unmodifiableTrie(final Trie<K, ? extends V> trie) {
        return new UnmodifiableTrie<K, V>(trie);
    }

// trigger testcase
// org/apache/commons/collections4/TrieUtilsTest.java::testUnmodifiableTrie
public void testUnmodifiableTrie() {
        Trie<String, Object> trie = TrieUtils.unmodifiableTrie(new PatriciaTrie<Object>());
        assertTrue("Returned object should be an UnmodifiableTrie.",
            trie instanceof UnmodifiableTrie);
        try {
            TrieUtils.unmodifiableTrie(null);
            fail("Expecting IllegalArgumentException for null trie.");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
        
        assertSame("UnmodifiableTrie shall not be decorated", trie, TrieUtils.unmodifiableTrie(trie));
    }

// org/apache/commons/collections4/trie/UnmodifiableTrieTest.java::testDecorateFactory
public void testDecorateFactory() {
        final Trie<String, V> trie = makeFullMap();
        assertSame(trie, UnmodifiableTrie.unmodifiableTrie(trie));

        try {
            UnmodifiableTrie.unmodifiableTrie(null);
            fail();
        } catch (final IllegalArgumentException ex) {}
    }
