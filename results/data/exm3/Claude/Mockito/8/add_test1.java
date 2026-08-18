// org/mockito/internal/util/reflection/GenericMetadataSupportTest.java
@Test
public void typeVariable_of_multiple_nested_generics() {
    GenericMetadataSupport genericMetadata = inferFrom(MultipleNestedGenerics.class).resolveGenericReturnType(firstNamedMethod("getMap", MultipleNestedGenerics.class));
    assertThat(genericMetadata.rawType()).isEqualTo(Map.class);
}

static class MultipleNestedGenerics<T extends Comparable<T>> {
    public Map<T, List<T>> getMap() { return null; }
}