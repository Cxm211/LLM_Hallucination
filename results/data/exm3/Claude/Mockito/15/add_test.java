// org/mockitousage/bugs/InjectMocksShouldTryPropertySettersFirstBeforeFieldAccessTest.java
@Test
public void shouldReturnFalseWhenPropertySetterThrowsRuntimeException() {
    class TestClass {
        private String value;
        public void setValue(String value) {
            throw new RuntimeException("Setter failed");
        }
    }
    
    TestClass instance = new TestClass();
    Field field;
    try {
        field = TestClass.class.getDeclaredField("value");
    } catch (NoSuchFieldException e) {
        throw new RuntimeException(e);
    }
    
    Collection<Object> mocks = new ArrayList<Object>();
    mocks.add("testValue");
    
    OngoingInjecter injecter = filterCandidate(mocks, field, instance);
    assertFalse(injecter.thenInject());
}