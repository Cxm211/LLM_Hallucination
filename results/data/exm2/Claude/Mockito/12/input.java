    public Class getGenericType(Field field) {        
        Type generic = field.getGenericType();
        if (generic != null && generic instanceof ParameterizedType) {
            Type actual = ((ParameterizedType) generic).getActualTypeArguments()[0];
                return (Class) actual;
                //in case of nested generics we don't go deep
        }
        
        return Object.class;
    }

// trigger testcase
@Test
    public void shouldDealWithNestedGenerics() throws Exception {
        assertEquals(Set.class, m.getGenericType(field("nested")));
        assertEquals(Set.class, m.getGenericType(field("multiNested")));
    }

@Test
    public void shouldCaptureGenericList() {
        //given
        List<String> list = new LinkedList<String>();
        mock.listArgMethod(list);
                
        //when
        verify(mock).listArgMethod(genericListCaptor.capture());
        
        //then
        assertSame(list, genericListCaptor.getValue());
    }

@Test
    public void shouldUseAnnotatedCaptor() {
        //when
        createPerson("Wes", "Williams");
        
        //then
        verify(peopleRepository).save(captor.capture());
        assertEquals("Wes", captor.getValue().getName());
        assertEquals("Williams", captor.getValue().getSurname());
    }

@Test
    public void shouldUseCaptorInOrdinaryWay() {
        //when
        createPerson("Wes", "Williams");
        
        //then
        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        verify(peopleRepository).save(captor.capture());
        assertEquals("Wes", captor.getValue().getName());
        assertEquals("Williams", captor.getValue().getSurname());
    }

@Test
    public void shouldUseGenericlessAnnotatedCaptor() {
        //when
        createPerson("Wes", "Williams");
        
        //then
        verify(peopleRepository).save((Person) genericLessCaptor.capture());
        assertEquals("Wes", ((Person) genericLessCaptor.getValue()).getName());
        assertEquals("Williams", ((Person) genericLessCaptor.getValue()).getSurname());
    }

@Test
    public void shouldLookForAnnotatedCaptorsInSuperClasses() throws Exception {
        Sub sub = new Sub();
        MockitoAnnotations.initMocks(sub);

        assertNotNull(sub.getCaptor());
        assertNotNull(sub.getBaseCaptor());
        assertNotNull(sub.getSuperBaseCaptor());
    }

@Test
    public void shouldScreamWhenInitializingCaptorsForNullClass() throws Exception {
        try {
            MockitoAnnotations.initMocks(null);
            fail();
        } catch (MockitoException e) {
        }
    }

@Test
    public void shouldScreamWhenMoreThanOneMockitoAnnotaton() {
        try {
            MockitoAnnotations.initMocks(new ToManyAnnotations());
            fail();
        } catch (MockitoException e) {
            assertContains("missingGenericsField", e.getMessage());
            assertContains("multiple Mockito annotations", e.getMessage());            
        }
    }

@Test
    public void shouldScreamWhenWrongTypeForCaptor() {
        try {
            MockitoAnnotations.initMocks(new WrongType());
            fail();
        } catch (MockitoException e) {}
    }

@Test
    public void testNormalUsage() {

        MockitoAnnotations.initMocks(this);

        // check if assigned correctly
        assertNotNull(finalCaptor);
        assertNotNull(genericsCaptor);
        assertNotNull(nonGenericCaptorIsAllowed);
        assertNull(notAMock);

        // use captors in the field to be sure they are cool
        String argForFinalCaptor = "Hello";
        ArrayList<List<String>> argForGenericsCaptor = new ArrayList<List<String>>();

        mockInterface.testMe(argForFinalCaptor, argForGenericsCaptor);

        Mockito.verify(mockInterface).testMe(finalCaptor.capture(), genericsCaptor.capture());

        assertEquals(argForFinalCaptor, finalCaptor.getValue());
        assertEquals(argForGenericsCaptor, genericsCaptor.getValue());

    }
