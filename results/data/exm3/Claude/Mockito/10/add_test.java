// org/mockitousage/bugs/DeepStubsWronglyReportsSerializationProblemsTest.java
@Test
public void should_not_raise_serialization_exception_when_chaining_multiple_deep_stubs() {
    NotSerializableShouldBeMocked deepStub1 = mock(ToBeDeepStubbed.class, RETURNS_DEEP_STUBS).getSomething();
    NotSerializableShouldBeMocked deepStub2 = mock(ToBeDeepStubbed.class, RETURNS_DEEP_STUBS).getSomething();
    assertThat(deepStub1).isNotNull();
    assertThat(deepStub2).isNotNull();
}