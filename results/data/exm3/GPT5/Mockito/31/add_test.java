// org/mockito/internal/stubbing/defaultanswers/ReturnsSmartNullsTest.java::shouldPrintSingleNullParameterWhenCallingAMethodWithArg
@Test
public void shouldPrintSingleNullParameterWhenCallingAMethodWithArg() throws Throwable {
    Answer<Object> answer = new ReturnsSmartNulls();
    Foo smartNull = (Foo) answer.answer(invocationOf(Foo.class, "withArg", (Object) null));
    assertEquals("SmartNull returned by unstubbed withArg(null) method on mock", smartNull + "");
}