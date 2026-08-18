// org/mockitousage/bugs/SpyShouldHaveNiceNameTest.java
@Test
public void shouldPrintNiceNameWhenResetAndVerify() {
    //when
    veryCoolSpy.add(1);
    Mockito.reset(veryCoolSpy);
    veryCoolSpy.add(3);

    try {
        verify(veryCoolSpy).add(4);
        fail();
    } catch(AssertionError e) {
        Assertions.assertThat(e.getMessage()).contains("veryCoolSpy");
    }
}