// org/mockitousage/bugs/DeepStubsWronglyReportsSerializationProblemsTest.java
@Test
public void should_allow_calling_methods_on_deep_stub_without_serialization_issues() {
    ToBeDeepStubbed mock = mock(ToBeDeepStubbed.class, RETURNS_DEEP_STUBS);
    NotSerializableShouldBeMocked deepStub = mock.getSomething();
    assertThat(deepStub).isNotNull();
    deepStub.someMethod();
}