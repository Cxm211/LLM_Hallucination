// org/mockitousage/stubbing/DeepStubsSerializableTest.java::should_serialize_and_deserialize_deep_stubbed_child_mock
@Test
public void should_serialize_and_deserialize_deep_stubbed_child_mock() throws Exception {
    SampleClass sampleClass = mock(SampleClass.class, withSettings().defaultAnswer(Mockito.RETURNS_DEEP_STUBS).serializable());
    when(sampleClass.getSample().isSth()).thenReturn(true);

    Object serializedChild = SimpleSerializationUtil.serializeAndBack(sampleClass.getSample());

    assertThat(serializedChild).isInstanceOf(SampleClass.Sample.class);
    SampleClass.Sample deserializedChild = (SampleClass.Sample) serializedChild;
    assertThat(deserializedChild.isSth()).isTrue();
}