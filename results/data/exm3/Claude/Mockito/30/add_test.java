// org/mockito/internal/stubbing/defaultanswers/ReturnsSmartNullsTest.java
@Test
public void shouldPrintTheParametersOnSmartNullPointerExceptionMessageWithNoArguments() throws Throwable {
    Answer<Object> answer = new ReturnsSmartNulls();

    Foo smartNull = (Foo) answer.answer(invocationOf(Foo.class, "withArgs"));

    try {
        smartNull.get();
        fail();
    } catch (SmartNullPointerException ex) {
        String message = ex.getMessage();
        assertNotNull("Exception message should not be null", message);
        assertTrue("Exception message should contain method information", message.contains("withArgs"));
    }
}