// org/mockitousage/constructor/CreatingMocksWithConstructorTest.java
@Test
    public void testCallsRealInterfaceMethodBoolean() {
        Iterator<?> iterator = mock(Iterator.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        assertFalse(iterator.hasNext());
    }
