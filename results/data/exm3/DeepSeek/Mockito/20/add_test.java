// org/mockitousage/constructor/CreatingMocksWithConstructorTest.java
@Test
    public void exception_message_when_constructor_is_private() {
        class PrivateClass {
            private PrivateClass() {}
        }
        try {
            mock(PrivateClass.class, withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS));
            fail();
        } catch (MockitoException e) {
            assertEquals("Unable to create mock instance of type 'PrivateClass'", e.getMessage());
        }
    }
