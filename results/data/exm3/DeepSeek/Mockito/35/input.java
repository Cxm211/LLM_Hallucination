// buggy function
    public static <T> T isA(Class<T> clazz) {
        return reportMatcher(new InstanceOf(clazz)).<T>returnNull();
    }

    public static <T> T eq(T value) {
        return reportMatcher(new Equals(value)).<T>returnNull();
    }  

    public static <T> T same(T value) {
        return reportMatcher(new Same(value)).<T>returnNull();
    }

// trigger testcase
// org/mockitousage/bugs/NPEWithCertainMatchersTest.java::shouldNotThrowNPEWhenIntPassed
@Test
    public void shouldNotThrowNPEWhenIntPassed() {
        mock.intArgumentMethod(100);
        
        verify(mock).intArgumentMethod(isA(Integer.class));
    }

// org/mockitousage/bugs/NPEWithCertainMatchersTest.java::shouldNotThrowNPEWhenIntegerPassed
@Test
    public void shouldNotThrowNPEWhenIntegerPassed() {
        mock.intArgumentMethod(100);

        verify(mock).intArgumentMethod(isA(Integer.class));
    }

// org/mockitousage/bugs/NPEWithCertainMatchersTest.java::shouldNotThrowNPEWhenIntegerPassedToEq
@Test
    public void shouldNotThrowNPEWhenIntegerPassedToEq() {
        mock.intArgumentMethod(100);
        
        verify(mock).intArgumentMethod(eq(new Integer(100)));
    }

// org/mockitousage/bugs/NPEWithCertainMatchersTest.java::shouldNotThrowNPEWhenIntegerPassedToSame
@Test
    public void shouldNotThrowNPEWhenIntegerPassedToSame() {
        mock.intArgumentMethod(100);

        verify(mock, never()).intArgumentMethod(same(new Integer(100)));
    }
