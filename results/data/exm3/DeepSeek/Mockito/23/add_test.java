// org/mockitousage/stubbing/DeepStubsSerializableTest.java
@Test
    public void should_serialize_and_deserialize_deep_stub_with_non_mockable_return_type() throws Exception {
        class Sample {
            String getValue() { return null; }
        }
        Sample sample = mock(Sample.class, withSettings().defaultAnswer(Mockito.RETURNS_DEEP_STUBS).serializable());
        when(sample.getValue()).thenReturn("stubbed");
        Object o = SimpleSerializationUtil.serializeAndBack(sample);
        assertThat(o).isInstanceOf(Sample.class);
        Sample deserialized = (Sample) o;
        assertThat(deserialized.getValue()).isEqualTo("stubbed");
    }
