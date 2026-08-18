// org/mockitousage/bugs/DeepStubsWronglyReportsSerializationProblemsTest.java
public interface ToBeDeepStubbedWithExtra {
        ExtraInterface getSomethingWithExtra();
    }

    public interface ExtraInterface {
        // empty
    }

    @Test
    public void should_not_raise_serialization_exception_for_deep_stub_with_extra_interfaces() {
        ExtraInterface deepStub = mock(ToBeDeepStubbedWithExtra.class, RETURNS_DEEP_STUBS).getSomethingWithExtra();
        assertThat(deepStub).isNotNull();
    }
