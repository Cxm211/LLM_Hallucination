// org/mockito/internal/util/reflection/GenericMetadataSupportTest.java
@Test
public void typeVariable_of_nested_parameterized_type() {
    GenericMetadataSupport genericMetadata = inferFrom(NestedParameterizedType.class).resolveGenericReturnType(firstNamedMethod("get", NestedParameterizedType.class));
    assertThat(genericMetadata.rawType()).isEqualTo(List.class);
}

static class NestedParameterizedType {
    public List<String> get() { return null; }
}