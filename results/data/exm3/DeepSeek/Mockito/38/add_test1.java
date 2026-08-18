// org/mockitousage/bugs/ActualInvocationHasNullArgumentNPEBugTest.java
@Test
    public void shouldAllowPassingNullArgumentWithNullMatcher() {
        org.mockitousage.bugs.Fun mockFun = org.mockito.Mockito.mock(org.mockitousage.bugs.Fun.class);
        org.mockito.Mockito.when(mockFun.doFun((String) org.mockito.ArgumentMatchers.anyObject())).thenReturn("value");
        mockFun.doFun(null);
        org.mockito.Mockito.verify(mockFun).doFun(org.mockito.ArgumentMatchers.isNull());
    }
