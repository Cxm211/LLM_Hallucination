// org/mockitousage/bugs/NPEWithCertainMatchersTest.java
@Test
    public void shouldNotThrowNPEWhenOtherPrimitivesPassedToMatchers() {
        interface PrimitiveMethods {
            void longMethod(long l);
            void doubleMethod(double d);
            void booleanMethod(boolean b);
        }
        PrimitiveMethods mock = Mockito.mock(PrimitiveMethods.class);
        
        mock.longMethod(100L);
        mock.doubleMethod(3.14);
        mock.booleanMethod(true);
        
        verify(mock).longMethod(isA(Long.class));
        verify(mock).doubleMethod(isA(Double.class));
        verify(mock).booleanMethod(isA(Boolean.class));
        
        verify(mock).longMethod(eq(100L));
        verify(mock).doubleMethod(eq(3.14));
        verify(mock).booleanMethod(eq(true));
        
        verify(mock, never()).longMethod(same(100L));
        verify(mock, never()).doubleMethod(same(3.14));
        verify(mock, never()).booleanMethod(same(true));
    }
