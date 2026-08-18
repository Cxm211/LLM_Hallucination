// org/mockito/internal/stubbing/defaultanswers/ReturnsSmartNullsTest.java
@Test
public void shouldPrintMethodCallWithMultipleArgs() throws Throwable {
	Answer<Object> answer = new ReturnsSmartNulls();

	Foo smartNull = (Foo) answer.answer(invocationOf(Foo.class, "multiArgs", "first", "second", "third"));

	assertEquals("SmartNull returned by unstubbed multiArgs(first, second, third) method on mock", smartNull + "");
}