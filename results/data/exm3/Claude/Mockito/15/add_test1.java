// org/mockitousage/bugs/InjectMocksShouldTryPropertySettersFirstBeforeFieldAccessTest.java
@Test
public void shouldReturnFalseWhenMultipleMocksProvided() {
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
    mocks.add("mock1");
    mocks.add("mock2");
    
    OngoingInjecter injecter = filterCandidate(mocks, field, instance);
    assertFalse(injecter.thenInject());
}