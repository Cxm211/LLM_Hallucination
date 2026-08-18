// org/mockito/internal/stubbing/defaultanswers/ReturnsGenericDeepStubsTest.java
@Test
    public void can_invoke_method_on_second_bound_of_type_variable() throws Exception {
        GenericsNest<?> mock = mock(GenericsNest.class, RETURNS_DEEP_STUBS);
        Comparable<?> comparable = mock.returningK();
        // This should not throw ClassCastException and should return default value for compareTo
        assertThat(comparable.compareTo(null)).isEqualTo(0);
    }
