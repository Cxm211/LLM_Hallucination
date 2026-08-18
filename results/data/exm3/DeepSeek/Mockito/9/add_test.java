// org/mockitousage/constructor/CreatingMocksWithConstructorTest.java
@Test
    public void testCallsRealInterfaceMethodInt() {
        List<?> list = mock(List.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        assertEquals(0, list.size());
    }
