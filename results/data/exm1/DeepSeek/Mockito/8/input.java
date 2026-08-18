// buggy code
    protected void registerTypeVariablesOn(Type classType) {
        if (!(classType instanceof ParameterizedType)) {
            return;
        }
        ParameterizedType parameterizedType = (ParameterizedType) classType;
        TypeVariable[] typeParameters = ((Class<?>) parameterizedType.getRawType()).getTypeParameters();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        for (int i = 0; i < actualTypeArguments.length; i++) {
            TypeVariable typeParameter = typeParameters[i];
            Type actualTypeArgument = actualTypeArguments[i];

            if (actualTypeArgument instanceof WildcardType) {
                contextualActualTypeParameters.put(typeParameter, boundsOf((WildcardType) actualTypeArgument));
            } else {
                contextualActualTypeParameters.put(typeParameter, actualTypeArgument);
            }
            // logger.log("For '" + parameterizedType + "' found type variable : { '" + typeParameter + "(" + System.identityHashCode(typeParameter) + ")" + "' : '" + actualTypeArgument + "(" + System.identityHashCode(typeParameter) + ")" + "' }");
        }
    }

// relevant test
// org.mockito.internal.stubbing.defaultanswers.ReturnsGenericDeepStubsTest::generic_deep_mock_frenzy__look_at_these_chained_calls
    public void generic_deep_mock_frenzy__look_at_these_chained_calls() throws Exception {
        GenericsNest<?> mock = mock(GenericsNest.class, RETURNS_DEEP_STUBS);

        Set<? extends Map.Entry<? extends Cloneable, Set<Number>>> entries = mock.entrySet();
        Iterator<? extends Map.Entry<? extends Cloneable,Set<Number>>> entriesIterator = mock.entrySet().iterator();
        Map.Entry<? extends Cloneable, Set<Number>> nextEntry = mock.entrySet().iterator().next();

        Cloneable cloneableKey = mock.entrySet().iterator().next().getKey();
        Comparable<?> comparableKey = mock.entrySet().iterator().next().getKey();

        Set<Number> value = mock.entrySet().iterator().next().getValue();
        Iterator<Number> numbersIterator = mock.entrySet().iterator().next().getValue().iterator();
        Number number = mock.entrySet().iterator().next().getValue().iterator().next();
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsGenericDeepStubsTest::can_create_mock_from_multiple_type_variable_bounds_when_return_type_of_parameterized_method_is_a_parameterizedtype_that_is_referencing_a_typevar_on_class
    public void can_create_mock_from_multiple_type_variable_bounds_when_return_type_of_parameterized_method_is_a_parameterizedtype_that_is_referencing_a_typevar_on_class() throws Exception {
        GenericsNest<?> mock = mock(GenericsNest.class, RETURNS_DEEP_STUBS);

        Cloneable cloneable_bound_that_is_declared_on_typevar_K_in_the_class_which_is_referenced_by_typevar_O_declared_on_the_method =
                mock.paramTypeWithTypeParams().get(0);
        Comparable<?> comparable_bound_that_is_declared_on_typevar_K_in_the_class_which_is_referenced_by_typevar_O_declared_on_the_method =
                mock.paramTypeWithTypeParams().get(0);
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsGenericDeepStubsTest::can_create_mock_from_multiple_type_variable_bounds_when_method_return_type_is_referencing_a_typevar_on_class
    public void can_create_mock_from_multiple_type_variable_bounds_when_method_return_type_is_referencing_a_typevar_on_class() throws Exception {
        GenericsNest<?> mock = mock(GenericsNest.class, RETURNS_DEEP_STUBS);

        Cloneable cloneable_bound_of_typevar_K = mock.returningK();
        Comparable<?> comparable_bound_of_typevar_K = mock.returningK();
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsGenericDeepStubsTest::can_create_mock_from_multiple_type_variable_bounds_when_return_type_of_parameterized_method_is_a_typevar_that_is_referencing_a_typevar_on_class
    public void can_create_mock_from_multiple_type_variable_bounds_when_return_type_of_parameterized_method_is_a_typevar_that_is_referencing_a_typevar_on_class() throws Exception {
        GenericsNest<?> mock = mock(GenericsNest.class, RETURNS_DEEP_STUBS);

        Cloneable cloneable_bound_of_typevar_K_referenced_by_typevar_O = (Cloneable) mock.typeVarWithTypeParams();
        Comparable<?> comparable_bound_of_typevar_K_referenced_by_typevar_O = (Comparable) mock.typeVarWithTypeParams();
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsGenericDeepStubsTest::can_create_mock_from_return_types_declared_with_a_bounded_wildcard
    public void can_create_mock_from_return_types_declared_with_a_bounded_wildcard() throws Exception {
        GenericsNest<?> mock = mock(GenericsNest.class, RETURNS_DEEP_STUBS);

        List<? super Integer> objects = mock.returningWildcard();
        Number type_that_is_the_upper_bound_of_the_wildcard = (Number) mock.returningWildcard().get(45);
        type_that_is_the_upper_bound_of_the_wildcard.floatValue();
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsGenericDeepStubsTest::can_still_work_with_raw_type_in_the_return_type
    public void can_still_work_with_raw_type_in_the_return_type() throws Exception {
        GenericsNest<?> mock = mock(GenericsNest.class, RETURNS_DEEP_STUBS);

        Number the_raw_type_that_should_be_returned = mock.returnsNormalType();
        the_raw_type_that_should_be_returned.floatValue();
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsGenericDeepStubsTest::will_return_default_value_on_non_mockable_nested_generic
    public void will_return_default_value_on_non_mockable_nested_generic() throws Exception {
        GenericsNest<?> genericsNest = mock(GenericsNest.class, RETURNS_DEEP_STUBS);
        ListOfInteger listOfInteger = mock(ListOfInteger.class, RETURNS_DEEP_STUBS);

        assertThat(genericsNest.returningNonMockableNestedGeneric().keySet().iterator().next()).isNull();
        assertThat(listOfInteger.get(25)).isEqualTo(0);
    }

// org.mockito.internal.stubbing.defaultanswers.ReturnsGenericDeepStubsTest::as_expected_fail_with_a_CCE_on_callsite_when_erasure_takes_place_for_example___StringBuilder_is_subject_to_erasure
    public void as_expected_fail_with_a_CCE_on_callsite_when_erasure_takes_place_for_example___StringBuilder_is_subject_to_erasure() throws Exception {
        GenericsNest<?> mock = mock(GenericsNest.class, RETURNS_DEEP_STUBS);

        
        StringBuilder stringBuilder_assignment_that_should_throw_a_CCE =
                mock.twoTypeParams(new StringBuilder()).append(2).append(3);
    }

// org.mockito.internal.util.reflection.GenericMetadataSupportTest::typeVariable_of_self_type
    public void typeVariable_of_self_type() {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsSelfReference.class).resolveGenericReturnType(firstNamedMethod("self", GenericsSelfReference.class));

        assertThat(genericMetadata.rawType()).isEqualTo(GenericsSelfReference.class);
    }

// org.mockito.internal.util.reflection.GenericMetadataSupportTest::can_get_raw_type_from_Class
    public void can_get_raw_type_from_Class() throws Exception {
        assertThat(inferFrom(ListOfAnyNumbers.class).rawType()).isEqualTo(ListOfAnyNumbers.class);
        assertThat(inferFrom(ListOfNumbers.class).rawType()).isEqualTo(ListOfNumbers.class);
        assertThat(inferFrom(GenericsNest.class).rawType()).isEqualTo(GenericsNest.class);
        assertThat(inferFrom(StringList.class).rawType()).isEqualTo(StringList.class);
    }

// org.mockito.internal.util.reflection.GenericMetadataSupportTest::can_get_raw_type_from_ParameterizedType
    public void can_get_raw_type_from_ParameterizedType() throws Exception {
        assertThat(inferFrom(ListOfAnyNumbers.class.getGenericInterfaces()[0]).rawType()).isEqualTo(List.class);
        assertThat(inferFrom(ListOfNumbers.class.getGenericInterfaces()[0]).rawType()).isEqualTo(List.class);
        assertThat(inferFrom(GenericsNest.class.getGenericInterfaces()[0]).rawType()).isEqualTo(Map.class);
        assertThat(inferFrom(StringList.class.getGenericSuperclass()).rawType()).isEqualTo(ArrayList.class);
    }

// org.mockito.internal.util.reflection.GenericMetadataSupportTest::can_get_type_variables_from_Class
    public void can_get_type_variables_from_Class() throws Exception {
        assertThat(inferFrom(GenericsNest.class).actualTypeArguments().keySet()).hasSize(1).onProperty("name").contains("K");
        assertThat(inferFrom(ListOfNumbers.class).actualTypeArguments().keySet()).isEmpty();
        assertThat(inferFrom(ListOfAnyNumbers.class).actualTypeArguments().keySet()).hasSize(1).onProperty("name").contains("N");
        assertThat(inferFrom(Map.class).actualTypeArguments().keySet()).hasSize(2).onProperty("name").contains("K", "V");
        assertThat(inferFrom(Serializable.class).actualTypeArguments().keySet()).isEmpty();
        assertThat(inferFrom(StringList.class).actualTypeArguments().keySet()).isEmpty();
    }

// org.mockito.internal.util.reflection.GenericMetadataSupportTest::can_get_type_variables_from_ParameterizedType
    public void can_get_type_variables_from_ParameterizedType() throws Exception {
        assertThat(inferFrom(GenericsNest.class.getGenericInterfaces()[0]).actualTypeArguments().keySet()).hasSize(2).onProperty("name").contains("K", "V");
        assertThat(inferFrom(ListOfAnyNumbers.class.getGenericInterfaces()[0]).actualTypeArguments().keySet()).hasSize(1).onProperty("name").contains("E");
        assertThat(inferFrom(Integer.class.getGenericInterfaces()[0]).actualTypeArguments().keySet()).hasSize(1).onProperty("name").contains("T");
        assertThat(inferFrom(StringBuilder.class.getGenericInterfaces()[0]).actualTypeArguments().keySet()).isEmpty();
        assertThat(inferFrom(StringList.class).actualTypeArguments().keySet()).isEmpty();
    }

// org.mockito.internal.util.reflection.GenericMetadataSupportTest::typeVariable_return_type_of____iterator____resolved_to_Iterator_and_type_argument_to_String
    public void typeVariable_return_type_of____iterator____resolved_to_Iterator_and_type_argument_to_String() throws Exception {
        GenericMetadataSupport genericMetadata = inferFrom(StringList.class).resolveGenericReturnType(firstNamedMethod("iterator", StringList.class));

        assertThat(genericMetadata.rawType()).isEqualTo(Iterator.class);
        assertThat(genericMetadata.actualTypeArguments().values()).contains(String.class);
    }

// org.mockito.internal.util.reflection.GenericMetadataSupportTest::typeVariable_return_type_of____get____resolved_to_Set_and_type_argument_to_Number
    public void typeVariable_return_type_of____get____resolved_to_Set_and_type_argument_to_Number() throws Exception {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsNest.class).resolveGenericReturnType(firstNamedMethod("get", GenericsNest.class));

        assertThat(genericMetadata.rawType()).isEqualTo(Set.class);
        assertThat(genericMetadata.actualTypeArguments().values()).contains(Number.class);
    }

// org.mockito.internal.util.reflection.GenericMetadataSupportTest::bounded_typeVariable_return_type_of____returningK____resolved_to_Comparable_and_with_BoundedType
    public void bounded_typeVariable_return_type_of____returningK____resolved_to_Comparable_and_with_BoundedType() throws Exception {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsNest.class).resolveGenericReturnType(firstNamedMethod("returningK", GenericsNest.class));

        assertThat(genericMetadata.rawType()).isEqualTo(Comparable.class);
        GenericMetadataSupport extraInterface_0 = inferFrom(genericMetadata.extraInterfaces().get(0));
        assertThat(extraInterface_0.rawType()).isEqualTo(Cloneable.class);
    }

// org.mockito.internal.util.reflection.GenericMetadataSupportTest::fixed_ParamType_return_type_of____remove____resolved_to_Set_and_type_argument_to_Number
    public void fixed_ParamType_return_type_of____remove____resolved_to_Set_and_type_argument_to_Number() throws Exception {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsNest.class).resolveGenericReturnType(firstNamedMethod("remove", GenericsNest.class));

        assertThat(genericMetadata.rawType()).isEqualTo(Set.class);
        assertThat(genericMetadata.actualTypeArguments().values()).contains(Number.class);
    }

// org.mockito.internal.util.reflection.GenericMetadataSupportTest::paramType_return_type_of____values____resolved_to_Collection_and_type_argument_to_Parameterized_Set
    public void paramType_return_type_of____values____resolved_to_Collection_and_type_argument_to_Parameterized_Set() throws Exception {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsNest.class).resolveGenericReturnType(firstNamedMethod("values", GenericsNest.class));

        assertThat(genericMetadata.rawType()).isEqualTo(Collection.class);
        GenericMetadataSupport fromTypeVariableE = inferFrom(typeVariableValue(genericMetadata.actualTypeArguments(), "E"));
        assertThat(fromTypeVariableE.rawType()).isEqualTo(Set.class);
        assertThat(fromTypeVariableE.actualTypeArguments().values()).contains(Number.class);
    }

// org.mockito.internal.util.reflection.GenericMetadataSupportTest::paramType_with_type_parameters_return_type_of____paramType_with_type_params____resolved_to_Collection_and_type_argument_to_Parameterized_Set
    public void paramType_with_type_parameters_return_type_of____paramType_with_type_params____resolved_to_Collection_and_type_argument_to_Parameterized_Set() throws Exception {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsNest.class).resolveGenericReturnType(firstNamedMethod("paramType_with_type_params", GenericsNest.class));

        assertThat(genericMetadata.rawType()).isEqualTo(List.class);
        Type firstBoundOfE = ((GenericMetadataSupport.TypeVarBoundedType) typeVariableValue(genericMetadata.actualTypeArguments(), "E")).firstBound();
        assertThat(inferFrom(firstBoundOfE).rawType()).isEqualTo(Comparable.class);
    }

// org.mockito.internal.util.reflection.GenericMetadataSupportTest::typeVariable_with_type_parameters_return_type_of____typeVar_with_type_params____resolved_K_hence_to_Comparable_and_with_BoundedType
    public void typeVariable_with_type_parameters_return_type_of____typeVar_with_type_params____resolved_K_hence_to_Comparable_and_with_BoundedType() throws Exception {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsNest.class).resolveGenericReturnType(firstNamedMethod("typeVar_with_type_params", GenericsNest.class));

        assertThat(genericMetadata.rawType()).isEqualTo(Comparable.class);
        GenericMetadataSupport extraInterface_0 = inferFrom(genericMetadata.extraInterfaces().get(0));
        assertThat(extraInterface_0.rawType()).isEqualTo(Cloneable.class);
    }

// org.mockito.internal.util.reflection.GenericMetadataSupportTest::class_return_type_of____append____resolved_to_StringBuilder_and_type_arguments
    public void class_return_type_of____append____resolved_to_StringBuilder_and_type_arguments() throws Exception {
        GenericMetadataSupport genericMetadata = inferFrom(StringBuilder.class).resolveGenericReturnType(firstNamedMethod("append", StringBuilder.class));

        assertThat(genericMetadata.rawType()).isEqualTo(StringBuilder.class);
        assertThat(genericMetadata.actualTypeArguments()).isEmpty();
    }

// org.mockito.internal.util.reflection.GenericMetadataSupportTest::paramType_with_wildcard_return_type_of____returning_wildcard_with_class_lower_bound____resolved_to_List_and_type_argument_to_Integer
    public void paramType_with_wildcard_return_type_of____returning_wildcard_with_class_lower_bound____resolved_to_List_and_type_argument_to_Integer() throws Exception {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsNest.class).resolveGenericReturnType(firstNamedMethod("returning_wildcard_with_class_lower_bound", GenericsNest.class));

        assertThat(genericMetadata.rawType()).isEqualTo(List.class);
        GenericMetadataSupport.BoundedType boundedType = (GenericMetadataSupport.BoundedType) typeVariableValue(genericMetadata.actualTypeArguments(), "E");
        assertThat(boundedType.firstBound()).isEqualTo(Integer.class);
        assertThat(boundedType.interfaceBounds()).isEmpty();
    }

// org.mockito.internal.util.reflection.GenericMetadataSupportTest::paramType_with_wildcard_return_type_of____returning_wildcard_with_typeVar_lower_bound____resolved_to_List_and_type_argument_to_Integer
    public void paramType_with_wildcard_return_type_of____returning_wildcard_with_typeVar_lower_bound____resolved_to_List_and_type_argument_to_Integer() throws Exception {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsNest.class).resolveGenericReturnType(firstNamedMethod("returning_wildcard_with_typeVar_lower_bound", GenericsNest.class));

        assertThat(genericMetadata.rawType()).isEqualTo(List.class);
        GenericMetadataSupport.BoundedType boundedType = (GenericMetadataSupport.BoundedType) typeVariableValue(genericMetadata.actualTypeArguments(), "E");

        assertThat(inferFrom(boundedType.firstBound()).rawType()).isEqualTo(Comparable.class);
        assertThat(boundedType.interfaceBounds()).contains(Cloneable.class);    }

// org.mockito.internal.util.reflection.GenericMetadataSupportTest::paramType_with_wildcard_return_type_of____returning_wildcard_with_typeVar_upper_bound____resolved_to_List_and_type_argument_to_Integer
    public void paramType_with_wildcard_return_type_of____returning_wildcard_with_typeVar_upper_bound____resolved_to_List_and_type_argument_to_Integer() throws Exception {
        GenericMetadataSupport genericMetadata = inferFrom(GenericsNest.class).resolveGenericReturnType(firstNamedMethod("returning_wildcard_with_typeVar_upper_bound", GenericsNest.class));

        assertThat(genericMetadata.rawType()).isEqualTo(List.class);
        GenericMetadataSupport.BoundedType boundedType = (GenericMetadataSupport.BoundedType) typeVariableValue(genericMetadata.actualTypeArguments(), "E");

        assertThat(inferFrom(boundedType.firstBound()).rawType()).isEqualTo(Comparable.class);
        assertThat(boundedType.interfaceBounds()).contains(Cloneable.class);
    }

// org.mockitousage.bugs.DeepStubsWronglyReportsSerializationProblemsTest::should_not_raise_a_mockito_exception_about_serialization_when_accessing_deep_stub
    public void should_not_raise_a_mockito_exception_about_serialization_when_accessing_deep_stub() {
        NotSerializableShouldBeMocked the_deep_stub = mock(ToBeDeepStubbed.class, RETURNS_DEEP_STUBS).getSomething();
        assertThat(the_deep_stub).isNotNull();
    }

// org.mockitousage.serialization.DeepStubsSerializableTest::should_serialize_and_deserialize_mock_created_with_deep_stubs
    public void should_serialize_and_deserialize_mock_created_with_deep_stubs() throws Exception {
        
        SampleClass sampleClass = mock(SampleClass.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS).serializable());
        when(sampleClass.getSample().isFalse()).thenReturn(true);
        when(sampleClass.getSample().number()).thenReturn(999);

        
        SampleClass deserializedSample = serializeAndBack(sampleClass);

        
        assertThat(deserializedSample.getSample().isFalse()).isEqualTo(true);
        assertThat(deserializedSample.getSample().number()).isEqualTo(999);
    }

// org.mockitousage.serialization.DeepStubsSerializableTest::should_serialize_and_deserialize_parameterized_class_mocked_with_deep_stubs
	public void should_serialize_and_deserialize_parameterized_class_mocked_with_deep_stubs() throws Exception {
		
		ListContainer deep_stubbed = mock(ListContainer.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS).serializable());
		when(deep_stubbed.iterator().next().add("yes")).thenReturn(true);

		
		ListContainer deserialized_deep_stub = serializeAndBack(deep_stubbed);
		
		
		assertThat(deserialized_deep_stub.iterator().next().add("not stubbed but mock already previously resolved")).isEqualTo(false);
        assertThat(deserialized_deep_stub.iterator().next().add("yes")).isEqualTo(true);
	}

// org.mockitousage.serialization.DeepStubsSerializableTest::should_discard_generics_metadata_when_serialized_then_disabling_deep_stubs_with_generics
	public void should_discard_generics_metadata_when_serialized_then_disabling_deep_stubs_with_generics() throws Exception {
		
		ListContainer deep_stubbed = mock(ListContainer.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS).serializable());
		when(deep_stubbed.iterator().hasNext()).thenReturn(true);

		ListContainer deserialized_deep_stub = serializeAndBack(deep_stubbed);

		
        when(deserialized_deep_stub.iterator().next().get(42)).thenReturn("no");

		
	}

// org.mockitousage.stubbing.DeepStubbingTest::myTest
    public void myTest() throws Exception {
        SocketFactory sf = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf.createSocket(anyString(), eq(80))).thenReturn(null);
        sf.createSocket("what", 80);
    }

// org.mockitousage.stubbing.DeepStubbingTest::simpleCase
    public void simpleCase() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        Socket socket = mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(out);

        assertSame(out, socket.getOutputStream());
    }

// org.mockitousage.stubbing.DeepStubbingTest::oneLevelDeep
    public void oneLevelDeep() throws Exception {
        OutputStream out = new ByteArrayOutputStream();

        SocketFactory socketFactory = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(socketFactory.createSocket().getOutputStream()).thenReturn(out);

        assertSame(out, socketFactory.createSocket().getOutputStream());
    }

// org.mockitousage.stubbing.DeepStubbingTest::interactions
    public void interactions() throws Exception {
        OutputStream out1 = new ByteArrayOutputStream();
        OutputStream out2 = new ByteArrayOutputStream();

        SocketFactory sf1 = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf1.createSocket().getOutputStream()).thenReturn(out1);

        SocketFactory sf2 = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf2.createSocket().getOutputStream()).thenReturn(out2);

        assertSame(out1, sf1.createSocket().getOutputStream());
        assertSame(out2, sf2.createSocket().getOutputStream());
    }

// org.mockitousage.stubbing.DeepStubbingTest::withArguments
    public void withArguments() throws Exception {
        OutputStream out1 = new ByteArrayOutputStream();
        OutputStream out2 = new ByteArrayOutputStream();
        OutputStream out3 = new ByteArrayOutputStream();

        SocketFactory sf = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf.createSocket().getOutputStream()).thenReturn(out1);
        when(sf.createSocket("google.com", 80).getOutputStream()).thenReturn(out2);
        when(sf.createSocket("stackoverflow.com", 80).getOutputStream()).thenReturn(out3);

        assertSame(out1, sf.createSocket().getOutputStream());
        assertSame(out2, sf.createSocket("google.com", 80).getOutputStream());
        assertSame(out3, sf.createSocket("stackoverflow.com", 80).getOutputStream());
    }

// org.mockitousage.stubbing.DeepStubbingTest::withAnyPatternArguments
    public void withAnyPatternArguments() throws Exception {
        OutputStream out = new ByteArrayOutputStream();

        
        SocketFactory sf = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf.createSocket(anyString(), anyInt()).getOutputStream()).thenReturn(out);

        assertSame(out, sf.createSocket("google.com", 80).getOutputStream());
        assertSame(out, sf.createSocket("stackoverflow.com", 8080).getOutputStream());
    }

// org.mockitousage.stubbing.DeepStubbingTest::withComplexPatternArguments
    public void withComplexPatternArguments() throws Exception {
        OutputStream out1 = new ByteArrayOutputStream();
        OutputStream out2 = new ByteArrayOutputStream();

        SocketFactory sf = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf.createSocket(anyString(), eq(80)).getOutputStream()).thenReturn(out1);
        when(sf.createSocket(anyString(), eq(8080)).getOutputStream()).thenReturn(out2);

        assertSame(out2, sf.createSocket("stackoverflow.com", 8080).getOutputStream());
        assertSame(out1, sf.createSocket("google.com", 80).getOutputStream());
        assertSame(out2, sf.createSocket("google.com", 8080).getOutputStream());
        assertSame(out1, sf.createSocket("stackoverflow.com", 80).getOutputStream());
    }

// org.mockitousage.stubbing.DeepStubbingTest::withSimplePrimitive
    public void withSimplePrimitive() throws Exception {
        int a = 32;

        SocketFactory sf = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf.createSocket().getPort()).thenReturn(a);

        assertEquals(a, sf.createSocket().getPort());
    }

// org.mockitousage.stubbing.DeepStubbingTest::withPatternPrimitive
    public void withPatternPrimitive() throws Exception {
        int a = 12, b = 23, c = 34;

        SocketFactory sf = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf.createSocket(eq("stackoverflow.com"), eq(80)).getPort()).thenReturn(a);
        when(sf.createSocket(eq("google.com"), anyInt()).getPort()).thenReturn(b);
        when(sf.createSocket(eq("stackoverflow.com"), eq(8080)).getPort()).thenReturn(c);

        assertEquals(b, sf.createSocket("google.com", 80).getPort());
        assertEquals(c, sf.createSocket("stackoverflow.com", 8080).getPort());
        assertEquals(a, sf.createSocket("stackoverflow.com", 80).getPort());
    }

// org.mockitousage.stubbing.DeepStubbingTest::shouldStubbingBasicallyWorkFine
    public void shouldStubbingBasicallyWorkFine() throws Exception {
        
        given(person.getAddress().getStreet().getName()).willReturn("Norymberska");
        
        
        String street = person.getAddress().getStreet().getName();
        
        
        assertEquals("Norymberska", street);
    }

// org.mockitousage.stubbing.DeepStubbingTest::shouldVerificationBasicallyWorkFine
    public void shouldVerificationBasicallyWorkFine() throws Exception {
        
        person.getAddress().getStreet().getName();
        
        
        verify(person.getAddress().getStreet()).getName();
    }

// org.mockitousage.stubbing.DeepStubbingTest::verification_work_with_argument_Matchers_in_nested_calls
	public void verification_work_with_argument_Matchers_in_nested_calls() throws Exception {
		
    	person.getAddress("111 Mock Lane").getStreet();
    	person.getAddress("111 Mock Lane").getStreet(Locale.ITALIAN).getName();

		
    	verify(person.getAddress(anyString())).getStreet();
    	verify(person.getAddress(anyString()).getStreet(Locale.CHINESE), never()).getName();
    	verify(person.getAddress(anyString()).getStreet(eq(Locale.ITALIAN))).getName();
	}

// org.mockitousage.stubbing.DeepStubbingTest::deep_stub_return_same_mock_instance_if_invocation_matchers_matches
    public void deep_stub_return_same_mock_instance_if_invocation_matchers_matches() throws Exception {
        when(person.getAddress(anyString()).getStreet().getName()).thenReturn("deep");

        person.getAddress("the docks").getStreet().getName();

        assertSame(person.getAddress("the docks").getStreet(), person.getAddress(anyString()).getStreet());
        assertSame(person.getAddress(anyString()).getStreet(), person.getAddress(anyString()).getStreet());
        assertSame(person.getAddress("the docks").getStreet(), person.getAddress("the docks").getStreet());
        assertSame(person.getAddress(anyString()).getStreet(), person.getAddress("the docks").getStreet());
        assertSame(person.getAddress("111 Mock Lane").getStreet(), person.getAddress("the docks").getStreet());
    }

// org.mockitousage.stubbing.DeepStubbingTest::times_never_atLeast_atMost_verificationModes_should_work
    public void times_never_atLeast_atMost_verificationModes_should_work() throws Exception {
        when(person.getAddress(anyString()).getStreet().getName()).thenReturn("deep");

        person.getAddress("the docks").getStreet().getName();
        person.getAddress("the docks").getStreet().getName();
        person.getAddress("the docks").getStreet().getName();
        person.getAddress("the docks").getStreet(Locale.ITALIAN).getName();

        verify(person.getAddress("the docks").getStreet(), times(3)).getName();
        verify(person.getAddress("the docks").getStreet(Locale.CHINESE), never()).getName();
        verify(person.getAddress("the docks").getStreet(Locale.ITALIAN), atMost(1)).getName();
    }

// org.mockitousage.stubbing.DeepStubbingTest::inOrder_only_work_on_the_very_last_mock_but_it_works
    public void inOrder_only_work_on_the_very_last_mock_but_it_works() throws Exception {
        when(person.getAddress(anyString()).getStreet().getName()).thenReturn("deep");
        when(person.getAddress(anyString()).getStreet(Locale.ITALIAN).getName()).thenReturn("deep");
        when(person.getAddress(anyString()).getStreet(Locale.CHINESE).getName()).thenReturn("deep");

        person.getAddress("the docks").getStreet().getName();
        person.getAddress("the docks").getStreet().getLongName();
        person.getAddress("the docks").getStreet(Locale.ITALIAN).getName();
        person.getAddress("the docks").getStreet(Locale.CHINESE).getName();

        InOrder inOrder = inOrder(
                person.getAddress("the docks").getStreet(),
                person.getAddress("the docks").getStreet(Locale.CHINESE),
                person.getAddress("the docks").getStreet(Locale.ITALIAN)
        );
        inOrder.verify(person.getAddress("the docks").getStreet(), times(1)).getName();
        inOrder.verify(person.getAddress("the docks").getStreet()).getLongName();
        inOrder.verify(person.getAddress("the docks").getStreet(Locale.ITALIAN), atLeast(1)).getName();
        inOrder.verify(person.getAddress("the docks").getStreet(Locale.CHINESE)).getName();
    }

// org.mockitousage.stubbing.DeepStubbingTest::verificationMode_only_work_on_the_last_returned_mock
    public void verificationMode_only_work_on_the_last_returned_mock() throws Exception {
        
        when(person.getAddress("the docks").getStreet().getName()).thenReturn("deep");

        
        person.getAddress("the docks").getStreet().getName();
        
        
        verify(person.getAddress("the docks").getStreet()).getName();

        try {
            verify(person.getAddress("the docks"), times(1)).getStreet();
            fail();
        } catch (TooManyActualInvocations e) {
            Assertions.assertThat(e.getMessage())
                    .contains("Wanted 1 time")
                    .contains("But was 3 times");
        }
    }

// org.mockitousage.stubbing.DeepStubbingTest::shouldFailGracefullyWhenClassIsFinal
    public void shouldFailGracefullyWhenClassIsFinal() throws Exception {
        
        FinalClass value = new FinalClass();
        given(person.getFinalClass()).willReturn(value);
        
        
        assertEquals(value, person.getFinalClass());
    }
