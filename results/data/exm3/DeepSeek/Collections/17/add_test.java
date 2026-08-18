// org/apache/commons/collections/functors/TestEqualPredicate.java
@Test
    public void testEqualPredicateWithDistinctButEqualObjects() {
        String s1 = new String("hello");
        String s2 = new String("hello");
        assertTrue(equalPredicate(s1), s2);
    }
