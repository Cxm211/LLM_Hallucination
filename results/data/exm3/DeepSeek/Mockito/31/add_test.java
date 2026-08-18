// org/mockito/internal/stubbing/defaultanswers/ReturnsSmartNullsTest.java
@Test
    public void shouldPrintTheParametersWhenCallingAMethodWithNullArg() throws Throwable {
        Answer<Object> answer = new ReturnsSmartNulls();
        Foo smartNull = (Foo) answer.answer(invocationOf(Foo.class, "withArgs", null, "lumpa"));
        assertEquals("SmartNull returned by unstubbed withArgs(null, lumpa) method on mock", smartNull + "");
    }
