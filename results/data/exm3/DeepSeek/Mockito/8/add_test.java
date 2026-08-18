// org/mockito/internal/util/reflection/GenericMetadataSupportTest.java
@Test
    public void wildcard_unbounded() {
        class Example {
            public java.util.List<?> get() { return null; }
        }
        GenericMetadataSupport genericMetadata = inferFrom(Example.class).resolveGenericReturnType(firstNamedMethod("get", Example.class));
        assertThat(genericMetadata.rawType()).isEqualTo(java.util.List.class);
    }
