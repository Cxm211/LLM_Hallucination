// buggy function
    public boolean hasSameMethod(Invocation candidate) {        
        //not using method.equals() for 1 good reason:
        //sometimes java generates forwarding methods when generics are in play see JavaGenericsForwardingMethodsTest
        Method m1 = invocation.getMethod();
        Method m2 = candidate.getMethod();
        
        	/* Avoid unnecessary cloning */
        return m1.equals(m2);
    }

// trigger testcase
// org/mockitousage/bugs/InheritedGenericsPolimorphicCallTest.java::shouldStubbingWork
@Test
    public void shouldStubbingWork() {
        Mockito.when(iterable.iterator()).thenReturn(myIterator);
        Assert.assertNotNull(((Iterable) iterable).iterator());
        Assert.assertNotNull(iterable.iterator());
    }

// org/mockitousage/bugs/InheritedGenericsPolimorphicCallTest.java::shouldVerificationWorks
@Test
    public void shouldVerificationWorks() {
        iterable.iterator();
        
        verify(iterable).iterator();
        verify((Iterable) iterable).iterator();
    }
