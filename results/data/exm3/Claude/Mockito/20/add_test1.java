// org/mockitousage/constructor/CreatingMocksWithConstructorTest.java
@Test
public void exception_message_contains_correct_class_name_for_abstract_class() {
    try {
        mock(AbstractClassWithNoDefaultConstructor.class, withSettings().useConstructor("arg").defaultAnswer(CALLS_REAL_METHODS));
        fail();
    } catch (MockitoException e) {
        assertEquals("Unable to create mock instance of type 'AbstractClassWithNoDefaultConstructor'", e.getMessage());
        assertNotNull(e.getCause());
    }
}

abstract class AbstractClassWithNoDefaultConstructor {
    public AbstractClassWithNoDefaultConstructor(String arg) {}
    abstract String getMessage();
}