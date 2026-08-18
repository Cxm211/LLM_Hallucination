// org/mockito/internal/matchers/EqualityTest.java
@Test
    public void shouldNotTreatNonArrayAsEqualToArray() {
        class ArrayLover {
            @Override public boolean equals(Object obj) {
                return obj != null && obj.getClass().isArray();
            }
        }
        ArrayLover lover = new ArrayLover();
        int[] array = new int[] {1, 2, 3};
        assertFalse(areEqual(lover, array));
    }
