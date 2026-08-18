// org/mockito/internal/stubbing/defaultanswers/ReturnsSmartNullsTest.java
@Test
    public void shouldIncludeParametersIncludingNull() throws Throwable {
        Answer<Object> answer = new ReturnsSmartNulls();
        Foo smartNull = (Foo) answer.answer(invocationOf(Foo.class, "withMixedArgs", "test", 42, null));
        try {
            smartNull.get();
            fail();
        } catch (SmartNullPointerException ex) {
            String message = ex.getMessage();
            assertTrue("Message should contain 'test'", message.contains("test"));
            assertTrue("Message should contain '42'", message.contains("42"));
            assertTrue("Message should contain 'null'", message.contains("null"));
        }
    }
