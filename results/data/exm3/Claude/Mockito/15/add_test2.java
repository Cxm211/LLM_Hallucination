// org/mockitousage/bugs/InjectMocksShouldTryPropertySettersFirstBeforeFieldAccessTest.java
@Test
public void shouldReturnFalseWhenNoMocksProvided() {
    class TestClass {
        private String value;
    }
    
    TestClass instance = new TestClass();
    Field field;
    try {
        field = TestClass.class.getDeclaredField("value");
    } catch (NoSuchFieldException e) {
        throw new RuntimeException(e);
    }
    
    Collection<Object> mocks = new ArrayList<Object>();
    
    OngoingInjecter injecter = filterCandidate(mocks, field, instance);
    assertFalse(injecter.thenInject());
}