// org/mockitousage/stubbing/DeepStubsSerializableTest.java
@Test
public void should_serialize_deep_stub_without_explicit_stubbing() throws Exception {
    // given
    SampleClass sampleClass = mock(SampleClass.class, withSettings().defaultAnswer(Mockito.RETURNS_DEEP_STUBS).serializable());
    SampleClass sample = sampleClass.getSample();

    // when
    Object o = SimpleSerializationUtil.serializeAndBack(sampleClass);

    // then
    assertThat(o).isInstanceOf(SampleClass.class);
    SampleClass deserializedSample = (SampleClass) o;
    assertThat(deserializedSample.getSample()).isNotNull();
}