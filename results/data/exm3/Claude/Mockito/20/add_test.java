// org/mockitousage/annotation/SpyAnnotationTest.java
@Test
public void should_report_instantiation_exception_with_correct_class_name() throws Exception {
    class FailingSpyWithSpecificName {
        @Spy
        ThrowingConstructor throwingConstructor;
    }

    try {
        MockitoAnnotations.initMocks(new FailingSpyWithSpecificName());
        fail();
    } catch (MockitoException e) {
        Assertions.assertThat(e.getMessage()).contains("Unable to create mock instance of type 'ThrowingConstructor'");
    }
}