// org/mockito/internal/matchers/EqualityTest.java
@Test
    public void shouldHandleReferenceEqualityForBrokenEquals() {
        class AlwaysFalse {
            @Override public boolean equals(Object obj) { return false; }
        }
        AlwaysFalse obj = new AlwaysFalse();
        assertTrue(areEqual(obj, obj));
    }
