// org/mockitousage/constructor/CreatingMocksWithConstructorTest.java
@Test
public void interfaceMethodWithBooleanReturnType() {
    BooleanProvider provider = mock(BooleanProvider.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
    assertEquals(false, provider.isEnabled());
}

interface BooleanProvider {
    boolean isEnabled();
}