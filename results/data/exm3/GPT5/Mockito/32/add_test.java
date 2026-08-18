// org/mockitousage/bugs/SpyShouldHaveNiceNameTest.java
@Test
public void shouldIncludeFieldNameInNoMoreInteractionsMessage() {
    veryCoolSpy.add(1);
    try {
        verifyNoMoreInteractions(veryCoolSpy);
        fail();
    } catch (org.mockito.exceptions.verification.NoInteractionsWanted e) {
        Assertions.assertThat(e.getMessage()).contains("veryCoolSpy");
    }
}
