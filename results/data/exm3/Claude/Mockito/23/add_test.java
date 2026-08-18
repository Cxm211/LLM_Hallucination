// org/mockitousage/stubbing/DeepStubsSerializableTest.java
@Test
public void should_serialize_deep_stub_with_multiple_levels() throws Exception {
    // given
    SampleClass sampleClass = mock(SampleClass.class, withSettings().defaultAnswer(Mockito.RETURNS_DEEP_STUBS).serializable());
    when(sampleClass.getSample().getSample().isSth()).thenReturn(true);

    // when
    Object o = SimpleSerializationUtil.serializeAndBack(sampleClass);

    // then
    assertThat(o).isInstanceOf(SampleClass.class);
    SampleClass deserializedSample = (SampleClass) o;
    assertThat(deserializedSample.getSample().getSample().isSth()).isEqualTo(true);
}