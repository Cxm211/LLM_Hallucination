// org/mockitousage/bugs/SpyShouldHaveNiceNameTest.java
@Test
    public void shouldKeepNameAfterReset() {
        // Reset the spy by re-initializing mocks
        MockitoAnnotations.initMocks(this);
        // Now verify that the spy name appears in error message
        veryCoolSpy.add(1);
        try {
            verify(veryCoolSpy).add(2);
            fail();
        } catch (AssertionError e) {
            Assertions.assertThat(e.getMessage()).contains("veryCoolSpy");
        }
    }
