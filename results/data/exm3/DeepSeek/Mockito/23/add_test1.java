// org/mockitousage/stubbing/DeepStubsSerializableTest.java
@Test
    public void should_serialize_and_deserialize_deep_stub_with_extra_interfaces() throws Exception {
        interface InterfaceA {
            String methodA();
        }
        interface InterfaceB {
            String methodB();
        }
        class Sample<T extends InterfaceA & InterfaceB> {
            T getT() { return null; }
        }
        Sample<?> sample = mock(Sample.class, withSettings().defaultAnswer(Mockito.RETURNS_DEEP_STUBS).serializable());
        InterfaceA mockA = (InterfaceA) sample.getT();
        when(mockA.methodA()).thenReturn("A");
        InterfaceB mockB = (InterfaceB) sample.getT();
        when(mockB.methodB()).thenReturn("B");
        Object o = SimpleSerializationUtil.serializeAndBack(sample);
        assertThat(o).isInstanceOf(Sample.class);
        Sample<?> deserialized = (Sample<?>) o;
        assertThat(((InterfaceA) deserialized.getT()).methodA()).isEqualTo("A");
        assertThat(((InterfaceB) deserialized.getT()).methodB()).isEqualTo("B");
    }
