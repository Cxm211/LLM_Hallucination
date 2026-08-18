// org/mockito/internal/invocation/InvocationTest.java
@Test
public void shouldAllowCallingRealMethodOnConcreteClass() throws Throwable {
    //given
    class ConcreteClass {
        public String doSomething() {
            return "real";
        }
    }
    ConcreteClass mockObj = mock(ConcreteClass.class);
    Method method = ConcreteClass.class.getMethod("doSomething");
    Invocation invocation = new Invocation(mockObj, method, new Object[0], 1, method);
    
    //when
    when(mockObj.doSomething()).thenAnswer(new Answer<String>() {
        public String answer(InvocationOnMock invocation) throws Throwable {
            return (String) invocation.callRealMethod();
        }
    });
    
    //then
    assertEquals("real", mockObj.doSomething());
}