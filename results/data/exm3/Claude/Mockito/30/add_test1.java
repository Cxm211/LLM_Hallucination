// org/mockito/internal/stubbing/defaultanswers/ReturnsSmartNullsTest.java
@Test
public void shouldPrintTheParametersOnSmartNullPointerExceptionMessageWithSingleArgument() throws Throwable {
    Answer<Object> answer = new ReturnsSmartNulls();

    Foo smartNull = (Foo) answer.answer(invocationOf(Foo.class, "withArgs", "single"));

    try {
        smartNull.get();
        fail();
    } catch (SmartNullPointerException ex) {
        String message = ex.getMessage();
        assertTrue("Exception message should include single argument, but was: " + message,
                message.contains("single"));
    }
}