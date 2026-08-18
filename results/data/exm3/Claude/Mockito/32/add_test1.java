// org/mockitousage/bugs/SpyShouldHaveNiceNameTest.java
@Test
public void shouldPrintNiceNameWithMultipleCalls() {
    //when
    veryCoolSpy.add(1);
    veryCoolSpy.add(2);
    veryCoolSpy.add(3);

    try {
        verify(veryCoolSpy, times(2)).add(1);
        fail();
    } catch(AssertionError e) {
        Assertions.assertThat(e.getMessage()).contains("veryCoolSpy");
    }
}