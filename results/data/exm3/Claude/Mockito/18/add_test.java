// org/mockito/internal/stubbing/defaultanswers/ReturnsEmptyValuesTest.java
@Test
public void should_return_null_for_unknown_type() throws Exception {
    assertNull(values.returnValueFor(java.io.Serializable.class));
}