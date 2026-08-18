// org/apache/commons/collections4/trie/UnmodifiableTrieTest.java
public void testUnmodifiableTrieNullWithMessage() {
    try {
        UnmodifiableTrie.unmodifiableTrie(null);
        fail("Expecting IllegalArgumentException for null trie.");
    } catch (final IllegalArgumentException ex) {
        assertEquals("Trie must not be null", ex.getMessage());
    }
}
