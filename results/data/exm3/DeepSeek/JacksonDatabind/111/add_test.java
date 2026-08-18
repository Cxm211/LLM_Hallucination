// com/fasterxml/jackson/databind/deser/jdk/JDKAtomicTypesDeserTest.java
public void testNullWithinNestedString() throws Exception {
    final ObjectReader r = MAPPER.readerFor(BeanNestedString.class);
    BeanNestedString bean = r.readValue("{\"refRef\": null}");
    assertNotNull(bean.refRef);
    assertNotNull(bean.refRef.get());
    assertNull(bean.refRef.get().get());
}

static class BeanNestedString {
    public AtomicReference<AtomicReference<String>> refRef;
}
