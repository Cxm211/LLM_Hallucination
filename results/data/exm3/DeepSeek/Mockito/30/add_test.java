// org/mockito/internal/stubbing/defaultanswers/ReturnsSmartNullsTest.java
@Test
    public void shouldIncludeMethodNameWhenNoParameters() throws Throwable {
        Answer<Object> answer = new ReturnsSmartNulls();
        Foo smartNull = (Foo) answer.answer(invocationOf(Foo.class, "noArgs"));
        try {
            smartNull.get();
            fail();
        } catch (SmartNullPointerException ex) {
            String message = ex.getMessage();
            assertTrue("Exception message should include method name 'noArgs', but was: " + message,
                    message.contains("noArgs"));
        }
    }
