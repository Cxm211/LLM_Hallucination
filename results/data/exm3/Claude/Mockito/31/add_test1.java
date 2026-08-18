// org/mockito/internal/stubbing/defaultanswers/ReturnsSmartNullsTest.java
@Test
public void shouldPrintMethodCallWithSingleArg() throws Throwable {
	Answer<Object> answer = new ReturnsSmartNulls();

	Foo smartNull = (Foo) answer.answer(invocationOf(Foo.class, "singleArg", "hello"));

	assertEquals("SmartNull returned by unstubbed singleArg(hello) method on mock", smartNull + "");
}