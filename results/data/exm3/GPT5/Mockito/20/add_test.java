// org/mockitousage/constructor/CreatingMocksWithConstructorTest.java::exception_message_when_constructor_for_interface
@Test
public void exception_message_when_constructor_for_interface() {
    try {
        mock(java.util.List.class, withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS));
        fail();
    } catch (MockitoException e) {
        assertEquals("Unable to create mock instance of type 'List'", e.getMessage());
    }
}