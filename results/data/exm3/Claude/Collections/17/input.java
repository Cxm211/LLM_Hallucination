// buggy function
    public EqualPredicate(T object) {
        // do not use the DefaultEquator to keep backwards compatibility
        // the DefaultEquator returns also true if the two object references are equal
        this(object, new DefaultEquator<T>());
    }

    public boolean evaluate(T object) {
            return equator.equate(iValue, object);
    }

// trigger testcase
// org/apache/commons/collections/functors/TestEqualPredicate.java::objectFactoryUsesEqualsForTest
@Test
    public void objectFactoryUsesEqualsForTest() throws Exception {
        Predicate<EqualsTestObject> predicate = equalPredicate(FALSE_OBJECT);
        assertFalse(predicate, FALSE_OBJECT);
        assertTrue(equalPredicate(TRUE_OBJECT), TRUE_OBJECT);
    }
