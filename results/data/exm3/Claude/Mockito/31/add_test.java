// org/mockito/internal/stubbing/defaultanswers/ReturnsSmartNullsTest.java
@Test
public void shouldPrintMethodCallWithNoArgs() throws Throwable {
	Answer<Object> answer = new ReturnsSmartNulls();

	Foo smartNull = (Foo) answer.answer(invocationOf(Foo.class, "noArgs"));

	assertEquals("SmartNull returned by unstubbed noArgs() method on mock", smartNull + "");
}