// org/mockitousage/bugs/ListenersLostOnResetMockTest.java
@Test
public void customAnswerPreservedAfterReset() throws Exception {
    Answer<String> customAnswer = new Answer<String>() {
        public String answer(InvocationOnMock invocation) {
            return "custom";
        }
    };

    List mockedList = mock(List.class, withSettings().defaultAnswer(customAnswer));
    when(mockedList.get(0)).thenReturn("original");
    reset(mockedList);

    assertEquals("custom", mockedList.get(0));
}