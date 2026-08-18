// buggy code
    public boolean equals(Object o) {
            return method.equals(o);
    }

    public int hashCode() {
        return 1;
    }

// relevant test
// org.mockitousage.stubbing.CallingRealMethodTest::shouldExecuteRealMethod
    public void shouldExecuteRealMethod() {
        doCallRealMethod().when(mock).setValue(anyString());

        mock.setValue("REAL_VALUE");

        Assert.assertEquals("REAL_VALUE", mock.value);
    }

// org.mockitousage.stubbing.CallingRealMethodTest::shouldCallRealMethodByDefault
    public void shouldCallRealMethodByDefault() {
        TestedObject mock = mock(TestedObject.class, CALLS_REAL_METHODS);

        Assert.assertEquals("HARD_CODED_RETURN_VALUE", mock.getValue());
    }

// org.mockitousage.stubbing.CallingRealMethodTest::shouldNotCallRealMethodWhenStubbedLater
    public void shouldNotCallRealMethodWhenStubbedLater() {
        TestedObject mock = mock(TestedObject.class);

        when(mock.getValue()).thenCallRealMethod();
        when(mock.getValue()).thenReturn("FAKE_VALUE");

        Assert.assertEquals("FAKE_VALUE", mock.getValue());
    }

// org.mockitousage.stubbing.CloningParameterTest::shouldVerifyEvenIfArgumentsWereMutated
    public void shouldVerifyEvenIfArgumentsWereMutated() throws Exception {

        
        EmailSender emailSender = mock(EmailSender.class, new ClonesArguments());

        
        businessLogic(emailSender);

        
        verify(emailSender).sendEmail(1, new Person("Wes"));
    }

// org.mockitousage.stubbing.CloningParameterTest::shouldReturnDefaultValueWithCloningAnswer
    public void shouldReturnDefaultValueWithCloningAnswer() throws Exception {

        
        EmailSender emailSender = mock(EmailSender.class, new ClonesArguments());
        when(emailSender.getAllEmails(new Person("Wes"))).thenAnswer(new ClonesArguments());

        
        List<?> emails = emailSender.getAllEmails(new Person("Wes"));

        
        assertNotNull(emails);
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

// org.mockitousage.stubbing.DeprecatedStubbingTest::shouldEvaluateLatestStubbingFirst
    public void shouldEvaluateLatestStubbingFirst() throws Exception {
        stub(mock.objectReturningMethod(isA(Integer.class))).toReturn(100);
        stub(mock.objectReturningMethod(200)).toReturn(200);
        
        assertEquals(200, mock.objectReturningMethod(200));
        assertEquals(100, mock.objectReturningMethod(666));
        assertEquals("default behavior should return null", null, mock.objectReturningMethod("blah"));
    }

// org.mockitousage.stubbing.DeprecatedStubbingTest::shouldStubbingBeTreatedAsInteraction
    public void shouldStubbingBeTreatedAsInteraction() throws Exception {
        stub(mock.booleanReturningMethod()).toReturn(true);
        
        mock.booleanReturningMethod();
        
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.stubbing.DeprecatedStubbingTest::shouldAllowStubbingToString
    public void shouldAllowStubbingToString() throws Exception {
        IMethods mockTwo = mock(IMethods.class);
        stub(mockTwo.toString()).toReturn("test");
        
        assertContains("Mock for IMethods", mock.toString());
        assertEquals("test", mockTwo.toString());
    }

// org.mockitousage.stubbing.DeprecatedStubbingTest::shouldStubbingNotBeTreatedAsInteraction
    public void shouldStubbingNotBeTreatedAsInteraction() {
        stub(mock.simpleMethod("one")).toThrow(new RuntimeException());
        stubVoid(mock).toThrow(new RuntimeException()).on().simpleMethod("two");
        
        verifyZeroInteractions(mock);
    }

// org.mockitousage.stubbing.DeprecatedStubbingTest::shouldAllowConsecutiveStubbing
    public void shouldAllowConsecutiveStubbing() throws Exception {
        
        stub(mock.simpleMethod())
            .toReturn("100")
            .toReturn("200");
        
        
        assertEquals("100", mock.simpleMethod());
        assertEquals("200", mock.simpleMethod());
    }

// org.mockitousage.stubbing.ReturningDefaultValuesTest::shouldReturnAllKindsOfPrimitives
    public void shouldReturnAllKindsOfPrimitives() throws Exception {
        assertEquals((byte) 0, mock.byteReturningMethod());
        assertEquals((short) 0, mock.shortReturningMethod());
        assertEquals(0, mock.intReturningMethod());
        assertEquals(0L, mock.longReturningMethod());
        assertEquals(0.0F, mock.floatReturningMethod(), 0.0F);
        assertEquals(0.0D, mock.doubleReturningMethod(), 0.0D);
        assertEquals((char) 0, mock.charReturningMethod());
        assertEquals(false, mock.booleanReturningMethod());
        assertEquals(null, mock.objectReturningMethod());
    }

// org.mockitousage.stubbing.ReturningDefaultValuesTest::shouldReturnTheSameValuesForWrapperClasses
    public void shouldReturnTheSameValuesForWrapperClasses() throws Exception {
        assertEquals(new Byte((byte) 0), mock.byteObjectReturningMethod());
        assertEquals(new Short((short) 0), mock.shortObjectReturningMethod());
        assertEquals(new Integer(0), mock.integerReturningMethod());
        assertEquals(new Long(0L), mock.longObjectReturningMethod());
        assertEquals(new Float(0.0F), mock.floatObjectReturningMethod(), 0.0F);
        assertEquals(new Double(0.0D), mock.doubleObjectReturningMethod(), 0.0D);
        assertEquals(new Character((char) 0), mock.charObjectReturningMethod());
        assertEquals(new Boolean(false), mock.booleanObjectReturningMethod());
    }

// org.mockitousage.stubbing.ReturningDefaultValuesTest::shouldReturnEmptyCollections
    public void shouldReturnEmptyCollections() {
        CollectionsServer mock = Mockito.mock(CollectionsServer.class);
        
        assertTrue(mock.list().isEmpty());
        assertTrue(mock.linkedList().isEmpty());
        assertTrue(mock.map().isEmpty());
        assertTrue(mock.hashSet().isEmpty());
    }

// org.mockitousage.stubbing.ReturningDefaultValuesTest::shouldReturnMutableEmptyCollection
    public void shouldReturnMutableEmptyCollection() {
        CollectionsServer mock = Mockito.mock(CollectionsServer.class);
        
        List list = mock.list();
        list.add("test");
       
        assertTrue(mock.list().isEmpty());
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldSmartNPEPointToUnstubbedCall
    public void shouldSmartNPEPointToUnstubbedCall() throws Exception {
        IMethods methods = unstubbedMethodInvokedHere(mock); 
        try {
            methods.simpleMethod();
            fail();
        } catch (SmartNullPointerException e) {
            assertContains("unstubbedMethodInvokedHere(", e.getMessage());
        }
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldThrowSmartNPEWhenMethodReturnsClass
    public void shouldThrowSmartNPEWhenMethodReturnsClass() throws Exception {
        Foo mock = mock(Foo.class, RETURNS_SMART_NULLS);
        Foo foo = mock.getSomeClass();
        try {
            foo.boo();
            fail();
        } catch (SmartNullPointerException e) {}
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldThrowSmartNPEWhenMethodReturnsInterface
    public void shouldThrowSmartNPEWhenMethodReturnsInterface() throws Exception {
        Foo mock = mock(Foo.class, RETURNS_SMART_NULLS);
        Bar bar = mock.getSomeInterface();
        try {
            bar.boo();
            fail();
        } catch (SmartNullPointerException e) {}
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldReturnOrdinaryEmptyValuesForOrdinaryTypes
    public void shouldReturnOrdinaryEmptyValuesForOrdinaryTypes() throws Exception {
        IMethods mock = mock(IMethods.class, RETURNS_SMART_NULLS);

        assertEquals("", mock.stringReturningMethod());
        assertEquals(0, mock.intReturningMethod());
        assertEquals(true, mock.listReturningMethod().isEmpty());
        assertEquals(0, mock.arrayReturningMethod().length);
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldNotThrowSmartNullPointerOnToString
    public void shouldNotThrowSmartNullPointerOnToString() {
        Object smartNull = mock.objectReturningMethod();
        try {
            verify(mock).simpleMethod(smartNull);
            fail();
        } catch (WantedButNotInvoked e) {}
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldNotThrowSmartNullPointerOnObjectMethods
    public void shouldNotThrowSmartNullPointerOnObjectMethods() {
        Object smartNull = mock.objectReturningMethod();
        smartNull.toString();
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldShowParameters
    public void shouldShowParameters() {
        Foo foo = mock(Foo.class, RETURNS_SMART_NULLS);
        Bar smartNull = foo.getBarWithParams(10, "yes sir");

        try {
            smartNull.boo();
            fail();
        } catch (Exception e) {
            assertContains("yes sir", e.getMessage());
        }
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldShowParametersWhenParamsAreHuge
    public void shouldShowParametersWhenParamsAreHuge() {
        Foo foo = mock(Foo.class, RETURNS_SMART_NULLS);
        String longStr = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.";
        Bar smartNull = foo.getBarWithParams(10, longStr);

        try {
            smartNull.boo();
            fail();
        } catch (Exception e) {
            assertContains("Lorem Ipsum", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldReturnConsecutiveValues
    public void shouldReturnConsecutiveValues() throws Exception {
        when(mock.simpleMethod())
            .thenReturn("one")
            .thenReturn("two")
            .thenReturn("three");
        
        assertEquals("one", mock.simpleMethod());
        assertEquals("two", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldReturnConsecutiveValuesForTwoNulls
    public void shouldReturnConsecutiveValuesForTwoNulls() throws Exception {
        when(mock.simpleMethod()).thenReturn(null, (String[])null);
        
        assertNull(mock.simpleMethod());        
        assertNull(mock.simpleMethod());        
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldReturnConsecutiveValuesSetByShortenThenReturnMethod
    public void shouldReturnConsecutiveValuesSetByShortenThenReturnMethod() throws Exception {        
        when(mock.simpleMethod())
            .thenReturn("one", "two", "three");
        
        assertEquals("one", mock.simpleMethod());
        assertEquals("two", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldReturnConsecutiveValueAndThrowExceptionssSetByShortenReturnMethods
    public void shouldReturnConsecutiveValueAndThrowExceptionssSetByShortenReturnMethods()
            throws Exception {
        when(mock.simpleMethod())
            .thenReturn("zero")
            .thenReturn("one", "two")
            .thenThrow(new NullPointerException(), new RuntimeException())
            .thenReturn("three")
            .thenThrow(new IllegalArgumentException());

        assertEquals("zero", mock.simpleMethod());
        assertEquals("one", mock.simpleMethod());
        assertEquals("two", mock.simpleMethod());
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {}
        assertEquals("three", mock.simpleMethod());
        try {
            mock.simpleMethod();
            fail();
        } catch (IllegalArgumentException e) {}
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldThrowConsecutively
    public void shouldThrowConsecutively() throws Exception {
        when(mock.simpleMethod())
            .thenThrow(new RuntimeException())
            .thenThrow(new IllegalArgumentException())
            .thenThrow(new NullPointerException());

        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (IllegalArgumentException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldThrowConsecutivelySetByShortenThenThrowMethod
    public void shouldThrowConsecutivelySetByShortenThenThrowMethod() throws Exception {
        when(mock.simpleMethod())
            .thenThrow(new RuntimeException(), new IllegalArgumentException(), new NullPointerException());

        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (IllegalArgumentException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldMixConsecutiveReturnsWithExcepions
    public void shouldMixConsecutiveReturnsWithExcepions() throws Exception {
        when(mock.simpleMethod())
            .thenThrow(new IllegalArgumentException())
            .thenReturn("one")
            .thenThrow(new NullPointerException())
            .thenReturn(null);
        
        try {
            mock.simpleMethod();
            fail();
        } catch (IllegalArgumentException e) {}
        
        assertEquals("one", mock.simpleMethod());
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
        
        assertEquals(null, mock.simpleMethod());
        assertEquals(null, mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldValidateConsecutiveException
    public void shouldValidateConsecutiveException() throws Exception {
        when(mock.simpleMethod())
            .thenReturn("one")
            .thenThrow(new Exception());
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldStubVoidMethodAndContinueThrowing
    public void shouldStubVoidMethodAndContinueThrowing() throws Exception {
        stubVoid(mock)
            .toThrow(new IllegalArgumentException())
            .toReturn()
            .toThrow(new NullPointerException())
            .on().voidMethod();
        
        try {
            mock.voidMethod();
            fail();
        } catch (IllegalArgumentException e) {}
        
        mock.voidMethod();
        
        try {
            mock.voidMethod();
            fail();
        } catch (NullPointerException e) {}
        
        try {
            mock.voidMethod();
            fail();
        } catch (NullPointerException e) {}        
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldStubVoidMethod
    public void shouldStubVoidMethod() throws Exception {
        stubVoid(mock)
            .toReturn()
            .toThrow(new NullPointerException())
            .toReturn()
            .on().voidMethod();
        
        mock.voidMethod();
        
        try {
            mock.voidMethod();
            fail();
        } catch (NullPointerException e) {}
        
        mock.voidMethod();
        mock.voidMethod();
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldValidateConsecutiveExceptionForVoidMethod
    public void shouldValidateConsecutiveExceptionForVoidMethod() throws Exception {
        stubVoid(mock)
            .toReturn()
            .toThrow(new Exception())
            .on().voidMethod();
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStub
    public void shouldStub() throws Exception {
        doReturn("foo").when(mock).simpleMethod();
        doReturn("bar").when(mock).simpleMethod();
        
        assertEquals("bar", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStubWithArgs
    public void shouldStubWithArgs() throws Exception {
        doReturn("foo").when(mock).simpleMethod("foo");
        doReturn("bar").when(mock).simpleMethod(eq("one"), anyInt());
        
        assertEquals("foo", mock.simpleMethod("foo"));
        assertEquals("bar", mock.simpleMethod("one", 234));
        assertEquals(null, mock.simpleMethod("xxx", 234));
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStubWithThrowable
    public void shouldStubWithThrowable() throws Exception {
        doThrow(new FooRuntimeException()).when(mock).voidMethod();
        try {
            mock.voidMethod();
            fail();
        } catch (FooRuntimeException e) {}
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowSettingValidCheckedException
    public void shouldAllowSettingValidCheckedException() throws Exception {
        doThrow(new IOException()).when(mock).throwsIOException(0);
        
        try {
            mock.throwsIOException(0);
            fail();
        } catch (IOException e) {}
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldDetectInvalidCheckedException
    public void shouldDetectInvalidCheckedException() throws Exception {
        try {
            doThrow(new FooCheckedException()).when(mock).throwsIOException(0);
            fail();
        } catch (Exception e) {
            assertContains("Checked exception is invalid", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldScreamWhenReturnSetForVoid
    public void shouldScreamWhenReturnSetForVoid() throws Exception {
        try {
            doReturn("foo").when(mock).voidMethod();
            fail();
        } catch (MockitoException e) {
            assertContains("void method", e.getMessage());
            assertContains("cannot", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldScreamWhenNotAMockPassed
    public void shouldScreamWhenNotAMockPassed() throws Exception {
        try {
            doReturn("foo").when("foo").toString();
            fail();
        } catch (Exception e) {
            assertContains("Argument passed to when() is not a mock", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldScreamWhenNullPassed
    public void shouldScreamWhenNullPassed() throws Exception {
        try {
            doReturn("foo").when((Object) null).toString();
            fail();
        } catch (Exception e) {
            assertContains("Argument passed to when() is null", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowChainedStubbing
    public void shouldAllowChainedStubbing() {
        doReturn("foo").
        doThrow(new RuntimeException()).
        doReturn("bar")
        .when(mock).simpleMethod();
        
        assertEquals("foo", mock.simpleMethod());
        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {}
        
        assertEquals("bar", mock.simpleMethod());
        assertEquals("bar", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowDoCallRealMethodInChainedStubbing
    public void shouldAllowDoCallRealMethodInChainedStubbing() throws Exception {
        MethodsImpl methods = mock(MethodsImpl.class);
        doReturn("A").doCallRealMethod()
                .when(methods).simpleMethod();

        assertEquals("A", methods.simpleMethod());
        assertEquals(null, methods.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowChainedStubbingWithExceptionClass
    public void shouldAllowChainedStubbingWithExceptionClass() throws Exception {
        doReturn("whatever").doThrow(IllegalArgumentException.class).when(mock).simpleMethod();

        assertEquals("whatever", mock.simpleMethod());
        mock.simpleMethod();
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowChainedStubbingOnVoidMethods
    public void shouldAllowChainedStubbingOnVoidMethods() {
        doNothing().
        doNothing().
        doThrow(new RuntimeException())
        .when(mock).voidMethod();
        
        mock.voidMethod();
        mock.voidMethod();
        try {
            mock.voidMethod();
            fail();
        } catch (RuntimeException e) {}
        try {
            mock.voidMethod();
            fail();
        } catch (RuntimeException e) {}
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStubWithGenericAnswer
    public void shouldStubWithGenericAnswer() {
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return "foo";
            }
        })
        .when(mock).simpleMethod();
        
        assertEquals("foo", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldNotAllowDoNothingOnNonVoids
    public void shouldNotAllowDoNothingOnNonVoids() {
        try {
            doNothing().when(mock).simpleMethod();
            fail();
        } catch (MockitoException e) {
            assertContains("Only void methods can doNothing()", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStubbingBeTreatedAsInteraction
    public void shouldStubbingBeTreatedAsInteraction() throws Exception {
        doReturn("foo").when(mock).simpleMethod();
        mock.simpleMethod();
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldVerifyStubbedCall
    public void shouldVerifyStubbedCall() throws Exception {
        doReturn("foo").when(mock).simpleMethod();
        mock.simpleMethod();
        mock.simpleMethod();
        
        verify(mock, times(2)).simpleMethod();
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowStubbingToString
    public void shouldAllowStubbingToString() throws Exception {
        doReturn("test").when(mock).toString();
        assertEquals("test", mock.toString());
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldDetectInvalidReturnType
    public void shouldDetectInvalidReturnType() throws Exception {
        try {
            doReturn("foo").when(mock).booleanObjectReturningMethod();
            fail();
        } catch (Exception e) {
            assertContains("String cannot be returned by booleanObjectReturningMethod()" +
                    "\n" +
                    "booleanObjectReturningMethod() should return Boolean",
                    e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldDetectWhenNullAssignedToBoolean
    public void shouldDetectWhenNullAssignedToBoolean() throws Exception {
        try {
            doReturn(null).when(mock).intReturningMethod();
            fail();
        } catch (Exception e) {
            assertContains("null cannot be returned by intReturningMethod", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowStubbingWhenTypesMatchSignature
    public void shouldAllowStubbingWhenTypesMatchSignature() throws Exception {
        doReturn("foo").when(mock).objectReturningMethodNoArgs();
        doReturn("foo").when(mock).simpleMethod();
        doReturn(1).when(mock).intReturningMethod();
        doReturn(new Integer(2)).when(mock).intReturningMethod();
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shouldAnswer
    public void shouldAnswer() throws Exception {
        when(mock.simpleMethod(anyString())).thenAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) throws Throwable {
                String arg = (String) invocation.getArguments()[0];

                return invocation.getMethod().getName() + "-" + arg;
            }
        });

        assertEquals("simpleMethod-test", mock.simpleMethod("test"));
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shouldAnswerWithThenAnswerAlias
    public void shouldAnswerWithThenAnswerAlias() throws Exception {
        RecordCall recordCall = new RecordCall();
        Set mockedSet = when(mock(Set.class).isEmpty()).then(recordCall).getMock();

        mockedSet.isEmpty();

        assertTrue(recordCall.isCalled());
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shouldAnswerConsecutively
    public void shouldAnswerConsecutively() throws Exception {
        when(mock.simpleMethod())
                .thenAnswer(new Answer<String>() {
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        return invocation.getMethod().getName();
                    }
                })
                .thenReturn("Hello")
                .thenAnswer(new Answer<String>() {
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        return invocation.getMethod().getName() + "-1";
                    }
                });

        assertEquals("simpleMethod", mock.simpleMethod());
        assertEquals("Hello", mock.simpleMethod());
        assertEquals("simpleMethod-1", mock.simpleMethod());
        assertEquals("simpleMethod-1", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shoudAnswerVoidMethod
    public void shoudAnswerVoidMethod() throws Exception {
        RecordCall recordCall = new RecordCall();

        stubVoid(mock).toAnswer(recordCall).on().voidMethod();

        mock.voidMethod();
        assertTrue(recordCall.isCalled());
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shouldAnswerVoidMethodConsecutively
    public void shouldAnswerVoidMethodConsecutively() throws Exception {
        RecordCall call1 = new RecordCall();
        RecordCall call2 = new RecordCall();

        stubVoid(mock).toAnswer(call1)
                .toThrow(new UnsupportedOperationException())
                .toAnswer(call2)
                .on().voidMethod();

        mock.voidMethod();
        assertTrue(call1.isCalled());
        assertFalse(call2.isCalled());

        try {
            mock.voidMethod();
            fail();
        } catch (UnsupportedOperationException e) {
        }

        mock.voidMethod();
        assertTrue(call2.isCalled());
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shouldMakeSureTheInterfaceDoesNotChange
    public void shouldMakeSureTheInterfaceDoesNotChange() throws Exception {
        when(mock.simpleMethod(anyString())).thenAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) throws Throwable {
                assertTrue(invocation.getArguments().getClass().isArray());
                assertEquals(Method.class, invocation.getMethod().getClass());

                return "assertions passed";
            }
        });

        assertEquals("assertions passed", mock.simpleMethod("test"));
    }

// org.mockitousage.stubbing.StubbingWithExtraAnswersTest::shouldWorkAsStandardMockito
    public void shouldWorkAsStandardMockito() throws Exception {
        
        List<Integer> list = asList(1, 2, 3);
        when(mock.objectReturningMethodNoArgs()).thenAnswer(new ReturnsElementsOf(list));
        
        
        assertEquals(1, mock.objectReturningMethodNoArgs());
        assertEquals(2, mock.objectReturningMethodNoArgs());
        assertEquals(3, mock.objectReturningMethodNoArgs());
        
        assertEquals(3, mock.objectReturningMethodNoArgs());
        assertEquals(3, mock.objectReturningMethodNoArgs());
    }

// org.mockitousage.stubbing.StubbingWithExtraAnswersTest::shouldReturnNullIfNecessary
    public void shouldReturnNullIfNecessary() throws Exception {
        
        List<Integer> list = asList(1, null);
        when(mock.objectReturningMethodNoArgs()).thenAnswer(new ReturnsElementsOf(list));
        
        
        assertEquals(1, mock.objectReturningMethodNoArgs());
        assertEquals(null, mock.objectReturningMethodNoArgs());
        assertEquals(null, mock.objectReturningMethodNoArgs());
    }

// org.mockitousage.stubbing.StubbingWithExtraAnswersTest::shouldScreamWhenNullPassed
    public void shouldScreamWhenNullPassed() throws Exception {
        try {
            
            new ReturnsElementsOf(null);
            
            fail();
        } catch (MockitoException e) {}
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldStubWithThrowable
    public void shouldStubWithThrowable() throws Exception {
        IllegalArgumentException expected = new IllegalArgumentException("thrown by mock");
        when(mock.add("throw")).thenThrow(expected);
        
        try {
            mock.add("throw");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(expected, e);
        }
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldSetThrowableToVoidMethod
    public void shouldSetThrowableToVoidMethod() throws Exception {
        IllegalArgumentException expected = new IllegalArgumentException("thrown by mock");
        
        stubVoid(mock).toThrow(expected).on().clear();
        try {
            mock.clear();
            fail();
        } catch (Exception e) {
            assertEquals(expected, e);
        }
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldLastStubbingVoidBeImportant
    public void shouldLastStubbingVoidBeImportant() throws Exception {
        stubVoid(mock).toThrow(new ExceptionOne()).on().clear();
        stubVoid(mock).toThrow(new ExceptionTwo()).on().clear();
        
        try {
            mock.clear();
            fail();
        } catch (ExceptionTwo e) {}
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldFailStubbingThrowableOnTheSameInvocationDueToAcceptableLimitation
    public void shouldFailStubbingThrowableOnTheSameInvocationDueToAcceptableLimitation() throws Exception {
        when(mock.get(1)).thenThrow(new ExceptionOne());
        
        try {
            when(mock.get(1)).thenThrow(new ExceptionTwo());
            fail();
        } catch (ExceptionOne e) {}
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldAllowSettingCheckedException
    public void shouldAllowSettingCheckedException() throws Exception {
        Reader reader = mock(Reader.class);
        IOException ioException = new IOException();
        
        when(reader.read()).thenThrow(ioException);
        
        try {
            reader.read();
            fail();
        } catch (Exception e) {
            assertEquals(ioException, e);
        }
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldAllowSettingError
    public void shouldAllowSettingError() throws Exception {
        Error error = new Error();
        
        when(mock.add("quake")).thenThrow(error);
        
        try {
            mock.add("quake");
            fail();
        } catch (Error e) {
            assertEquals(error, e);
        }
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldInstantiateExceptionClassOnInteraction
    public void shouldInstantiateExceptionClassOnInteraction() {
        when(mock.add(null)).thenThrow(IllegalArgumentException.class);

        mock.add(null);
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldInstantiateExceptionClassWithOngoingStubbingOnInteraction
    public void shouldInstantiateExceptionClassWithOngoingStubbingOnInteraction() {
        Mockito.doThrow(IllegalArgumentException.class).when(mock).add(null);

        mock.add(null);
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldNotAllowSettingInvalidCheckedException
    public void shouldNotAllowSettingInvalidCheckedException() throws Exception {
        when(mock.add("monkey island")).thenThrow(new Exception());
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldNotAllowSettingNullThrowable
    public void shouldNotAllowSettingNullThrowable() throws Exception {
        when(mock.add("monkey island")).thenThrow((Throwable) null);
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldNotAllowSettingNullThrowableArray
    public void shouldNotAllowSettingNullThrowableArray() throws Exception {
        when(mock.add("monkey island")).thenThrow((Throwable[]) null);
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldMixThrowablesAndReturnsOnDifferentMocks
    public void shouldMixThrowablesAndReturnsOnDifferentMocks() throws Exception {
        when(mock.add("ExceptionOne")).thenThrow(new ExceptionOne());
        when(mock.getLast()).thenReturn("last");
        stubVoid(mock).toThrow(new ExceptionTwo()).on().clear();
        
        stubVoid(mockTwo).toThrow(new ExceptionThree()).on().clear();
        when(mockTwo.containsValue("ExceptionFour")).thenThrow(new ExceptionFour());
        when(mockTwo.get("Are you there?")).thenReturn("Yes!");

        assertNull(mockTwo.get("foo"));
        assertTrue(mockTwo.keySet().isEmpty());
        assertEquals("Yes!", mockTwo.get("Are you there?"));
        try {
            mockTwo.clear();
            fail();
        } catch (ExceptionThree e) {}
        try {
            mockTwo.containsValue("ExceptionFour");
            fail();
        } catch (ExceptionFour e) {}
        
        assertNull(mock.getFirst());
        assertEquals("last", mock.getLast());
        try {
            mock.add("ExceptionOne");
            fail();
        } catch (ExceptionOne e) {}
        try {
            mock.clear();
            fail();
        } catch (ExceptionTwo e) {}
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldStubbingWithThrowableBeVerifiable
    public void shouldStubbingWithThrowableBeVerifiable() {
        when(mock.size()).thenThrow(new RuntimeException());
        stubVoid(mock).toThrow(new RuntimeException()).on().clone();
        
        try {
            mock.size();
            fail();
        } catch (RuntimeException e) {}
        
        try {
            mock.clone();
            fail();
        } catch (RuntimeException e) {}
        
        verify(mock).size();
        verify(mock).clone();
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldStubbingWithThrowableFailVerification
    public void shouldStubbingWithThrowableFailVerification() {
        when(mock.size()).thenThrow(new RuntimeException());
        stubVoid(mock).toThrow(new RuntimeException()).on().clone();
        
        verifyZeroInteractions(mock);
        
        mock.add("test");
        
        try {
            verify(mock).size();
            fail();
        } catch (WantedButNotInvoked e) {}
        
        try {
            verify(mock).clone();
            fail();
        } catch (WantedButNotInvoked e) {}
        
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldShowDecentMessageWhenExcepionIsNaughty
    public void shouldShowDecentMessageWhenExcepionIsNaughty() throws Exception {
        when(mock.add("")).thenThrow(NaughtyException.class);
        mock.add("");
    }

// org.mockitousage.verification.AtLeastXVerificationTest::shouldVerifyAtLeastXTimes
    public void shouldVerifyAtLeastXTimes() throws Exception {
        
        mock.clear();
        mock.clear();
        mock.clear();

        
        verify(mock, atLeast(2)).clear();
    }

// org.mockitousage.verification.AtLeastXVerificationTest::shouldFailVerifiationAtLeastXTimes
    public void shouldFailVerifiationAtLeastXTimes() throws Exception {
        mock.add("one");
        verify(mock, atLeast(1)).add(anyString());

        try {
            verify(mock, atLeast(2)).add(anyString());
            fail();
        } catch (MockitoAssertionError e) {}
    }

// org.mockitousage.verification.AtLeastXVerificationTest::shouldAllowAtLeastZeroForTheSakeOfVerifyNoMoreInteractionsSometimes
    public void shouldAllowAtLeastZeroForTheSakeOfVerifyNoMoreInteractionsSometimes() throws Exception {
        
        mock.add("one");
        mock.clear();

        
        verify(mock, atLeast(0)).add("one");
        verify(mock, atLeast(0)).clear();

        verifyNoMoreInteractions(mock);        
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldVerifyAtMostXTimes
    public void shouldVerifyAtMostXTimes() throws Exception {
        mock.clear();
        mock.clear();
        
        verify(mock, atMost(2)).clear();
        verify(mock, atMost(3)).clear();
        
        try {
            verify(mock, atMost(1)).clear();
            fail();
        } catch (MockitoAssertionError e) {}
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldWorkWithArgumentMatchers
    public void shouldWorkWithArgumentMatchers() throws Exception {
        mock.add("one");
        verify(mock, atMost(5)).add(anyString());
        
        try {
            verify(mock, atMost(0)).add(anyString());
            fail();
        } catch (MockitoAssertionError e) {}
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldNotAllowNegativeNumber
    public void shouldNotAllowNegativeNumber() throws Exception {
        try {
            verify(mock, atMost(-1)).clear();
            fail();
        } catch (MockitoException e) {
            assertEquals("Negative value is not allowed here", e.getMessage());
        }
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldPrintDecentMessage
    public void shouldPrintDecentMessage() throws Exception {
        mock.clear();
        mock.clear();
        
        try {
            verify(mock, atMost(1)).clear();
            fail();
        } catch (MockitoAssertionError e) {
            assertEquals("\nWanted at most 1 time but was 2", e.getMessage());
        }
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldNotAllowInOrderMode
    public void shouldNotAllowInOrderMode() throws Exception {
        mock.clear();
        InOrder inOrder = inOrder(mock);
        
        try {
            inOrder.verify(mock, atMost(1)).clear();
            fail();
        } catch (MockitoException e) {
            assertEquals("AtMost is not implemented to work with InOrder", e.getMessage());
        }
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldMarkInteractionsAsVerified
    public void shouldMarkInteractionsAsVerified() throws Exception {
        mock.clear();
        mock.clear();
        
        verify(mock, atMost(3)).clear();
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldDetectUnverifiedInMarkInteractionsAsVerified
    public void shouldDetectUnverifiedInMarkInteractionsAsVerified() throws Exception {
        mock.clear();
        mock.clear();
        undesiredInteraction();
        
        verify(mock, atMost(3)).clear();
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch(NoInteractionsWanted e) {
            assertContains("undesiredInteraction(", e.getMessage());
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldVerifyInOrder
    public void shouldVerifyInOrder() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldVerifyInOrderUsingAtLeastOnce
    public void shouldVerifyInOrderUsingAtLeastOnce() {
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(4);
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldVerifyInOrderWhenExpectingSomeInvocationsToBeCalledZeroTimes
    public void shouldVerifyInOrderWhenExpectingSomeInvocationsToBeCalledZeroTimes() {
        inOrder.verify(mockOne, times(0)).oneArg(false);
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockTwo, times(0)).simpleMethod(22);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        inOrder.verify(mockThree, times(0)).oneArg(false);
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailWhenFirstMockCalledTwice
    public void shouldFailWhenFirstMockCalledTwice() {
        inOrder.verify(mockOne).simpleMethod(1);
        try {
            inOrder.verify(mockOne).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailWhenLastMockCalledTwice
    public void shouldFailWhenLastMockCalledTwice() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        try {
            inOrder.verify(mockOne).simpleMethod(4);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnFirstMethodBecauseOneInvocationWanted
    public void shouldFailOnFirstMethodBecauseOneInvocationWanted() {
        inOrder.verify(mockOne, times(0)).simpleMethod(1);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnFirstMethodBecauseOneInvocationWantedAgain
    public void shouldFailOnFirstMethodBecauseOneInvocationWantedAgain() {
        inOrder.verify(mockOne, times(2)).simpleMethod(1);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnSecondMethodBecauseFourInvocationsWanted
    public void shouldFailOnSecondMethodBecauseFourInvocationsWanted() {
        inOrder.verify(mockOne, times(1)).simpleMethod(1);
        try {
            inOrder.verify(mockTwo, times(4)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnSecondMethodBecauseTwoInvocationsWantedAgain
    public void shouldFailOnSecondMethodBecauseTwoInvocationsWantedAgain() {
        inOrder.verify(mockOne, times(1)).simpleMethod(1);
        try {
            inOrder.verify(mockTwo, times(0)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnLastMethodBecauseOneInvocationWanted
    public void shouldFailOnLastMethodBecauseOneInvocationWanted() {
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree, atLeastOnce()).simpleMethod(3);
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        try {
            inOrder.verify(mockOne, times(0)).simpleMethod(4);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnLastMethodBecauseOneInvocationWantedAgain
    public void shouldFailOnLastMethodBecauseOneInvocationWantedAgain() {
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree, atLeastOnce()).simpleMethod(3);
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        try {
            inOrder.verify(mockOne, times(2)).simpleMethod(4);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnFirstMethodBecauseDifferentArgsWanted
    public void shouldFailOnFirstMethodBecauseDifferentArgsWanted() {
        inOrder.verify(mockOne).simpleMethod(100);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnFirstMethodBecauseDifferentMethodWanted
    public void shouldFailOnFirstMethodBecauseDifferentMethodWanted() {
        inOrder.verify(mockOne).oneArg(true);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnSecondMethodBecauseDifferentArgsWanted
    public void shouldFailOnSecondMethodBecauseDifferentArgsWanted() {
        inOrder.verify(mockOne).simpleMethod(1);
        try {
            inOrder.verify(mockTwo, times(2)).simpleMethod(-999);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnSecondMethodBecauseDifferentMethodWanted
    public void shouldFailOnSecondMethodBecauseDifferentMethodWanted() {
        inOrder.verify(mockOne, times(1)).simpleMethod(1);
        try {
            inOrder.verify(mockTwo, times(2)).oneArg(true);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnLastMethodBecauseDifferentArgsWanted
    public void shouldFailOnLastMethodBecauseDifferentArgsWanted() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        try {
            inOrder.verify(mockOne).simpleMethod(-666);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnLastMethodBecauseDifferentMethodWanted
    public void shouldFailOnLastMethodBecauseDifferentMethodWanted() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        try {
            inOrder.verify(mockOne).oneArg(false);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailWhenLastMethodVerifiedFirst
    public void shouldFailWhenLastMethodVerifiedFirst() {
        inOrder.verify(mockOne).simpleMethod(4);
        try {
            inOrder.verify(mockOne).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailWhenMiddleMethodVerifiedFirst
    public void shouldFailWhenMiddleMethodVerifiedFirst() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        try {
            inOrder.verify(mockOne).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailWhenMiddleMethodVerifiedFirstInAtLeastOnceMode
    public void shouldFailWhenMiddleMethodVerifiedFirstInAtLeastOnceMode() {
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        try {
            inOrder.verify(mockOne).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnVerifyNoMoreInteractions
    public void shouldFailOnVerifyNoMoreInteractions() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);

        try {
            verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
            fail();
        } catch (NoInteractionsWanted e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnVerifyZeroInteractions
    public void shouldFailOnVerifyZeroInteractions() {
        verifyZeroInteractions(mockOne);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldScreamWhenNullPassed
    public void shouldScreamWhenNullPassed() {
        inOrder((Object[])null);
    }

// org.mockitousage.verification.BasicVerificationTest::shouldVerify
    public void shouldVerify() throws Exception {
        mock.clear();
        verify(mock).clear();

        mock.add("test");
        verify(mock).add("test");

        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.verification.BasicVerificationTest::shouldFailVerification
    public void shouldFailVerification() throws Exception {
        verify(mock).clear();
    }

// org.mockitousage.verification.BasicVerificationTest::shouldFailVerificationOnMethodArgument
    public void shouldFailVerificationOnMethodArgument() throws Exception {
        mock.clear();
        mock.add("foo");

        verify(mock).clear();
        try {
            verify(mock).add("bar");
            fail();
        } catch (AssertionError expected) {}
    }

// org.mockitousage.verification.BasicVerificationTest::shouldFailOnWrongMethod
    public void shouldFailOnWrongMethod() throws Exception {
        mock.clear();
        mock.clear();
        
        mockTwo.add("add");

        verify(mock, atLeastOnce()).clear();
        verify(mockTwo, atLeastOnce()).add("add");
        try {
            verify(mockTwo, atLeastOnce()).add("foo");
            fail();
        } catch (WantedButNotInvoked e) {}
    }

// org.mockitousage.verification.BasicVerificationTest::shouldDetectRedundantInvocation
    public void shouldDetectRedundantInvocation() throws Exception {
        mock.clear();
        mock.add("foo");
        mock.add("bar");

        verify(mock).clear();
        verify(mock).add("foo");

        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.BasicVerificationTest::shouldDetectWhenInvokedMoreThanOnce
    public void shouldDetectWhenInvokedMoreThanOnce() throws Exception {
        mock.add("foo");
        mock.clear();
        mock.clear();
        
        verify(mock).add("foo");

        try {
            verify(mock).clear();
            fail();
        } catch (TooManyActualInvocations e) {}
    }

// org.mockitousage.verification.BasicVerificationTest::shouldVerifyStubbedMethods
    public void shouldVerifyStubbedMethods() throws Exception {
        when(mock.add("test")).thenReturn(Boolean.FALSE);
        
        mock.add("test");
        
        verify(mock).add("test");
    }

// org.mockitousage.verification.BasicVerificationTest::shouldDetectWhenOverloadedMethodCalled
    public void shouldDetectWhenOverloadedMethodCalled() throws Exception {
        IMethods mockThree = mock(IMethods.class);
        
        mockThree.varargs((Object[]) new Object[] {});
        try {
            verify(mockThree).varargs((String[]) new String[] {});
            fail();
        } catch(WantedButNotInvoked e) {}
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldPrintVerificationInOrderErrorAndShowBothWantedAndPrevious
    public void shouldPrintVerificationInOrderErrorAndShowBothWantedAndPrevious() {
        inOrder.verify(one).simpleMethod(1);
        inOrder.verify(two, atLeastOnce()).simpleMethod(2);
        
        try {
            inOrder.verify(one, atLeastOnce()).simpleMethod(11);
            fail();
        } catch (VerificationInOrderFailure e) {
            String expected = 
                    "\n" +
                    "Verification in order failure" +
                    "\n" +
                    "Wanted but not invoked:" +
                    "\n" +
                    "iMethods.simpleMethod(11);" +
                    "\n" +
                    "-> at "; 
            
            assertContains(expected, e.getMessage());
            
            String expectedCause = 
                "\n" +
                "Wanted anywhere AFTER following interaction:" +
                "\n" +
                "iMethods.simpleMethod(2);" +
                "\n" +
                "-> at ";
            
            assertContains(expectedCause, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldPrintVerificationInOrderErrorAndShowWantedOnly
    public void shouldPrintVerificationInOrderErrorAndShowWantedOnly() {
        try {
            inOrder.verify(one).differentMethod();
            fail();
        } catch (WantedButNotInvoked e) {
            String expected = 
                    "\n" +
                    "Wanted but not invoked:" +
                    "\n" +
                    "iMethods.differentMethod();" +
                    "\n" +
                    "-> at"; 
            
            assertContains(expected, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldPrintVerificationInOrderErrorAndShowWantedAndActual
    public void shouldPrintVerificationInOrderErrorAndShowWantedAndActual() {
        try {
            inOrder.verify(one).simpleMethod(999);
            fail();
        } catch (org.mockito.exceptions.verification.junit.ArgumentsAreDifferent e) {           
            assertContains("has different arguments", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldNotSayArgumentsAreDifferent
    public void shouldNotSayArgumentsAreDifferent() {
        
        inOrder.verify(three).simpleMethod(3);
        try {
            inOrder.verify(one).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("Wanted but not invoked", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldPrintMethodThatWasNotInvoked
    public void shouldPrintMethodThatWasNotInvoked() {
        inOrder.verify(one).simpleMethod(1);
        inOrder.verify(one).simpleMethod(11);
        inOrder.verify(two, times(2)).simpleMethod(2);
        inOrder.verify(three).simpleMethod(3);
        try {
            inOrder.verify(three).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            String actualMessage = e.getMessage();
            String expectedMessage = 
                    "\n" +
                    "Verification in order failure" +
                    "\n" +
                    "Wanted but not invoked:" +
                    "\n" +
                    "iMethods.simpleMethod(999);"; 
            assertContains(expectedMessage, actualMessage);     
        }
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldPrintTooManyInvocations
    public void shouldPrintTooManyInvocations() {
        inOrder.verify(one).simpleMethod(1);
        inOrder.verify(one).simpleMethod(11);
        try {
            inOrder.verify(two, times(1)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {
            String actualMessage = e.getMessage();
            String expectedMessage = 
                    "\n" +
                    "Verification in order failure:" +
                    "\n" +
                    "iMethods.simpleMethod(2);" +
                    "\n" +
                    "Wanted 1 time:" +
                    "\n" +
                    "-> at"; 
            assertContains(expectedMessage, actualMessage);      

            String expectedCause =
                "\n" +
                "But was 2 times. Undesired invocation:" +
                "\n" +
                "-> at";
            assertContains(expectedCause, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldPrintTooLittleInvocations
    public void shouldPrintTooLittleInvocations() {
        two.simpleMethod(2);
        
        inOrder.verify(one, atLeastOnce()).simpleMethod(anyInt());
        inOrder.verify(two, times(2)).simpleMethod(2);
        inOrder.verify(three, atLeastOnce()).simpleMethod(3);
        
        try {
            inOrder.verify(two, times(2)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {
            String actualMessage = e.getMessage();
            String expectedMessage = 
                    "\n" +
                    "Verification in order failure:" +
                    "\n" +
                    "iMethods.simpleMethod(2);" +
                    "\n" +
                    "Wanted 2 times:" +
                    "\n" +
                    "-> at";
            assertContains(expectedMessage, actualMessage);
            
            String expectedCause = 
                "\n" +
                "But was 1 time:" +
                "\n" +
                "-> at";
            
            assertContains(expectedCause, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenTimesXVerificationFailsTest::shouldVerifyActualNumberOfInvocationsSmallerThanWanted
    public void shouldVerifyActualNumberOfInvocationsSmallerThanWanted() throws Exception {
        mock.clear();
        mock.clear();
        mock.clear();

        Mockito.verify(mock, times(3)).clear();
        try {
            Mockito.verify(mock, times(100)).clear();
            fail();
        } catch (TooLittleActualInvocations e) {
            assertContains("mock.clear();", e.getMessage());
            assertContains("Wanted 100 times", e.getMessage());
            assertContains("was 3", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenTimesXVerificationFailsTest::shouldVerifyActualNumberOfInvocationsLargerThanWanted
    public void shouldVerifyActualNumberOfInvocationsLargerThanWanted() throws Exception {
        mock.clear();
        mock.clear();
        mock.clear();
        mock.clear();

        Mockito.verify(mock, times(4)).clear();
        try {
            Mockito.verify(mock, times(1)).clear();
            fail();
        } catch (TooManyActualInvocations e) {
            assertContains("mock.clear();", e.getMessage());
            assertContains("Wanted 1 time", e.getMessage());
            assertContains("was 4", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_method_name
    public void should_print_method_name() {
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (WantedButNotInvoked e) {
            String actualMessage = e.getMessage();
            String expectedMessage =
                    "\n" +
                    "Wanted but not invoked:" +
                    "\n" +
                    "iMethods.simpleMethod();" +
                    "\n" +
                    "-> at";
            assertContains(expectedMessage, actualMessage);
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_method_name_and_arguments
    public void should_print_method_name_and_arguments() {
        try {
            verify(mock).threeArgumentMethod(12, new Foo(), "xx");
            fail();
        } catch (WantedButNotInvoked e) {
            assertContains("iMethods.threeArgumentMethod(12, foo, \"xx\")", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_actual_and_wanted_in_line
    public void should_print_actual_and_wanted_in_line() {
        mock.varargs(1, 2);

        try {
            verify(mock).varargs(1, 1000);
            fail();
        } catch (ArgumentsAreDifferent e) {
            String wanted =
                    "\n" +
                    "Argument(s) are different! Wanted:" +
                    "\n" +
                    "iMethods.varargs(1, 1000);";

            assertContains(wanted, e.getMessage());
            
            String actual = 
                    "\n" +
                    "Actual invocation has different arguments:" +
                    "\n" +
                    "iMethods.varargs(1, 2);";

            assertContains(actual, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_actual_and_wanted_in_multiple_lines
    public void should_print_actual_and_wanted_in_multiple_lines() {
        mock.varargs("this is very long string", "this is another very long string");

        try {
            verify(mock).varargs("x", "y", "z");
            fail();
        } catch (ArgumentsAreDifferent e) {
            String wanted =
                    "\n" +
                    "Argument(s) are different! Wanted:" +
                    "\n" +
                    "iMethods.varargs(" +
                    "\n" +
                    "    \"x\"," +
                    "\n" +
                    "    \"y\"," +
                    "\n" +
                    "    \"z\"" +
                    "\n" +
                    ");";

            assertContains(wanted, e.getMessage());

            String actual =
                    "\n" +
                    "Actual invocation has different arguments:" +
                    "\n" +
                    "iMethods.varargs(" +
                    "\n" +
                    "    \"this is very long string\"," +
                    "\n" +
                    "    \"this is another very long string\"" +
                    "\n" +
                    ");";

            assertContains(actual, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_actual_and_wanted_when_actual_method_name_and_wanted_method_name_are_the_same
    public void should_print_actual_and_wanted_when_actual_method_name_and_wanted_method_name_are_the_same() {
        mock.simpleMethod();

        try {
            verify(mock).simpleMethod(10);
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("simpleMethod(10)", e.getMessage());
            assertContains("simpleMethod()", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_actual_and_unverified_wanted_when_the_difference_is_about_arguments
    public void should_print_actual_and_unverified_wanted_when_the_difference_is_about_arguments() {
        mock.twoArgumentMethod(1, 1);
        mock.twoArgumentMethod(2, 2);

        verify(mock).twoArgumentMethod(1, 1);
        try {
            verify(mock).twoArgumentMethod(2, 1000);
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("(2, 1000)", e.getMessage());
            assertContains("(2, 2)", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_first_unexpected_invocation
    public void should_print_first_unexpected_invocation() {
        mock.oneArg(true);
        mock.oneArg(false);
        mock.threeArgumentMethod(1, "2", "3");

        verify(mock).oneArg(true);
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {
            String expectedMessage =
                    "\n" +
                    "No interactions wanted here:" +
                    "\n" +
                    "-> at";
            assertContains(expectedMessage, e.getMessage());

            String expectedCause =
                    "\n" +
                    "But found this interaction on mock '" + mock + "':" +
                    "\n" +
                    "-> at";
            assertContains(expectedCause, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_first_unexpected_invocation_when_verifying_zero_interactions
    public void should_print_first_unexpected_invocation_when_verifying_zero_interactions() {
        mock.twoArgumentMethod(1, 2);
        mock.threeArgumentMethod(1, "2", "3");

        try {
            verifyZeroInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {
            String expected =
                    "\n" +
                    "No interactions wanted here:" +
                    "\n" +
                    "-> at";

            assertContains(expected, e.getMessage());

            String expectedCause =
                "\n" +
                "But found this interaction on mock '" + mock + "':" +
                "\n" +
                "-> at";

            assertContains(expectedCause, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_method_name_when_verifying_at_least_once
    public void should_print_method_name_when_verifying_at_least_once() throws Exception {
        try {
            verify(mock, atLeastOnce()).twoArgumentMethod(1, 2);
            fail();
        } catch (WantedButNotInvoked e) {
            assertContains("twoArgumentMethod(1, 2)", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_method_when_matcher_used
    public void should_print_method_when_matcher_used() throws Exception {
        try {
            verify(mock, atLeastOnce()).twoArgumentMethod(anyInt(), eq(100));
            fail();
        } catch (WantedButNotInvoked e) {
            String actualMessage = e.getMessage();
            String expectedMessage =
                "\n" +
                "Wanted but not invoked:" +
                "\n" +
                "iMethods.twoArgumentMethod(<any>, 100);";
            assertContains(expectedMessage, actualMessage);
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_method_when_missing_invocation_with_array_matcher
    public void should_print_method_when_missing_invocation_with_array_matcher() {
        mock.oneArray(new boolean[] { true, false, false });

        try {
            verify(mock).oneArray(aryEq(new boolean[] { false, false, false }));
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("[false, false, false]", e.getMessage());
            assertContains("[true, false, false]", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_method_when_missing_invocation_with_vararg_matcher
    public void should_print_method_when_missing_invocation_with_vararg_matcher() {
        mock.varargsString(10, "xxx", "yyy", "zzz");

        try {
            verify(mock).varargsString(10, "111", "222", "333");
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("111", e.getMessage());
            assertContains("\"xxx\"", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_method_when_missing_invocation_with_matcher
    public void should_print_method_when_missing_invocation_with_matcher() {
        mock.simpleMethod("foo");

        try {
            verify(mock).simpleMethod(matches("burrito from Exmouth"));
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("matches(\"burrito from Exmouth\")", e.getMessage());
            assertContains("\"foo\"", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_null_arguments
    public void should_print_null_arguments() throws Exception {
        mock.simpleMethod(null, (Integer) null);
        try {
            verify(mock).simpleMethod("test");
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("simpleMethod(null, null);", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_say_never_wanted_but_invoked
    public void should_say_never_wanted_but_invoked() throws Exception {
        mock.simpleMethod(1);
    
        verify(mock, never()).simpleMethod(2);
        try {
            verify(mock, never()).simpleMethod(1);
            fail();
        } catch (NeverWantedButInvoked e) {
            assertContains("Never wanted here:", e.getMessage());
            assertContains("But invoked here:", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_show_right_actual_method
    public void should_show_right_actual_method() throws Exception {
        mock.simpleMethod(9191);
        mock.simpleMethod("foo");
    
        try {
            verify(mock).simpleMethod("bar");
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("bar", e.getMessage());
            assertContains("foo", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_field_name_when_annotations_used
    public void should_print_field_name_when_annotations_used() throws Exception {
        iHavefunkyName.simpleMethod(10);
    
        try {
            verify(iHavefunkyName).simpleMethod(20);
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("iHavefunkyName.simpleMethod(20)", e.getMessage());
            assertContains("iHavefunkyName.simpleMethod(10)", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_interactions_on_mock_when_ordinary_verification_fail
    public void should_print_interactions_on_mock_when_ordinary_verification_fail() throws Exception {
        mock.otherMethod();
        mock.booleanReturningMethod();
        
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (WantedButNotInvoked e) {

        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_never_break_method_string_when_no_args_in_method
    public void should_never_break_method_string_when_no_args_in_method() throws Exception {
        try {
            verify(veeeeeeeeeeeeeeeeeeeeeeeerylongNameMock).simpleMethod();
            fail();
        } catch(WantedButNotInvoked e) {
            assertContains("veeeeeeeeeeeeeeeeeeeeeeeerylongNameMock.simpleMethod()", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_method_name_and_arguments_of_other_interactions_with_different_methods
    public void should_print_method_name_and_arguments_of_other_interactions_with_different_methods() throws Exception {
        try {
            mock.arrayMethod(new String[] {"a", "b", "c"});
            mock.forByte((byte) 25);

            verify(mock).threeArgumentMethod(12, new Foo(), "xx");
            fail();
        } catch (WantedButNotInvoked e) {
            System.out.println(e);
            assertContains("iMethods.threeArgumentMethod(12, foo, \"xx\")", e.getMessage());
            assertContains("iMethods.arrayMethod([\"a\", \"b\", \"c\"])", e.getMessage());
            assertContains("iMethods.forByte(25)", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::should_print_method_name_and_arguments_of_other_interactions_of_same_method
    public void should_print_method_name_and_arguments_of_other_interactions_of_same_method() throws Exception {
        try {
            mock.forByte((byte) 25);
            mock.forByte((byte) 12);

            verify(mock).forByte((byte) 42);
            fail();
        } catch (WantedButNotInvoked e) {
            System.out.println(e);
            assertContains("iMethods.forByte(42)", e.getMessage());
            assertContains("iMethods.forByte(25)", e.getMessage());
            assertContains("iMethods.forByte(12)", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::test1
    public void test1() {
        AnInterface m = Mockito.mock(AnInterface.class);

        for (int i = 1; i <= 2; i++) {
            m.foo(i);
        }

        verify(m).foo(1);
        verify(m).foo(2);
        verify(m).foo(3); 
        verify(m).foo(4);
    }

// org.mockitousage.verification.DescriptiveMessagesWhenVerificationFailsTest::test2
    public void test2() {
        AnInterface m = Mockito.mock(AnInterface.class);

        for (int i = 1; i <= 4; i++) {
            m.foo(i);
        }

        verify(m).foo(1);
        verify(m).foo(2);
        verify(m).foo(5); 
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldDetectTooLittleActualInvocations
    public void shouldDetectTooLittleActualInvocations() throws Exception {
        mock.clear();
        mock.clear();

        verify(mock, times(2)).clear();
        try {
            verify(mock, times(100)).clear();
            fail();
        } catch (TooLittleActualInvocations e) {
            assertContains("Wanted 100 times", e.getMessage());
            assertContains("was 2", e.getMessage());
        }
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldDetectTooManyActualInvocations
    public void shouldDetectTooManyActualInvocations() throws Exception {
        mock.clear();
        mock.clear();

        verify(mock, times(2)).clear();
        try {
            verify(mock, times(1)).clear();
            fail();
        } catch (TooManyActualInvocations e) {
            assertContains("Wanted 1 time", e.getMessage());
            assertContains("was 2 times", e.getMessage());
        }
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldDetectActualInvocationsCountIsMoreThanZero
    public void shouldDetectActualInvocationsCountIsMoreThanZero() throws Exception {
        verify(mock, times(0)).clear();
        try {
            verify(mock, times(15)).clear();
            fail();
        } catch (WantedButNotInvoked e) {}
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldDetectActuallyCalledOnce
    public void shouldDetectActuallyCalledOnce() throws Exception {
        mock.clear();

        try {
            verify(mock, times(0)).clear();
            fail();
        } catch (NeverWantedButInvoked e) {
            assertContains("Never wanted here", e.getMessage());
        }
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldPassWhenMethodsActuallyNotCalled
    public void shouldPassWhenMethodsActuallyNotCalled() throws Exception {
        verify(mock, times(0)).clear();
        verify(mock, times(0)).add("yes, I wasn't called");
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldNotCountInStubbedInvocations
    public void shouldNotCountInStubbedInvocations() throws Exception {
        when(mock.add("test")).thenReturn(false);
        when(mock.add("test")).thenReturn(true);

        mock.add("test");
        mock.add("test");

        verify(mock, times(2)).add("test");
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldAllowVerifyingInteractionNeverHappened
    public void shouldAllowVerifyingInteractionNeverHappened() throws Exception {
        mock.add("one");

        verify(mock, never()).add("two");
        verify(mock, never()).clear();
        
        try {
            verify(mock, never()).add("one");
            fail();
        } catch (NeverWantedButInvoked e) {}
    }

// org.mockitousage.verification.ExactNumberOfTimesVerificationTest::shouldAllowVerifyingInteractionNeverHappenedInOrder
    public void shouldAllowVerifyingInteractionNeverHappenedInOrder() throws Exception {
        mock.add("one");
        mock.add("two");

        InOrder inOrder = inOrder(mock);
        
        inOrder.verify(mock, never()).add("xxx");
        inOrder.verify(mock).add("one");
        inOrder.verify(mock, never()).add("one");
        
        try {
            inOrder.verify(mock, never()).add("two");
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.FindingRedundantInvocationsInOrderTest::shouldWorkFineIfNoInvocatins
    public void shouldWorkFineIfNoInvocatins() throws Exception {
        
        InOrder inOrder = inOrder(mock);
        
        
        inOrder.verifyNoMoreInteractions();        
    }

// org.mockitousage.verification.FindingRedundantInvocationsInOrderTest::shouldSayNoInteractionsWanted
    public void shouldSayNoInteractionsWanted() throws Exception {
        
        mock.simpleMethod();
        
        
        InOrder inOrder = inOrder(mock);
        try {
            inOrder.verifyNoMoreInteractions();
            fail();
        } catch(VerificationInOrderFailure e) {
            assertContains("No interactions wanted", e.getMessage());
        }
    }

// org.mockitousage.verification.FindingRedundantInvocationsInOrderTest::shouldVerifyNoMoreInteractionsInOrder
    public void shouldVerifyNoMoreInteractionsInOrder() throws Exception {
        
        mock.simpleMethod();
        mock.simpleMethod(10);
        mock.otherMethod();
        
        
        InOrder inOrder = inOrder(mock);
        inOrder.verify(mock).simpleMethod(10);
        inOrder.verify(mock).otherMethod();
        inOrder.verifyNoMoreInteractions();        
    }

// org.mockitousage.verification.FindingRedundantInvocationsInOrderTest::shouldVerifyNoMoreInteractionsInOrderWithMultipleMocks
    public void shouldVerifyNoMoreInteractionsInOrderWithMultipleMocks() throws Exception {
        
        mock.simpleMethod();
        mock2.simpleMethod();
        mock.otherMethod();
        
        
        InOrder inOrder = inOrder(mock, mock2);
        inOrder.verify(mock2).simpleMethod();
        inOrder.verify(mock).otherMethod();
        inOrder.verifyNoMoreInteractions();        
    }

// org.mockitousage.verification.FindingRedundantInvocationsInOrderTest::shouldFailToVerifyNoMoreInteractionsInOrder
    public void shouldFailToVerifyNoMoreInteractionsInOrder() throws Exception {
        
        mock.simpleMethod();
        mock.simpleMethod(10);
        mock.otherMethod();
        
        
        InOrder inOrder = inOrder(mock);
        inOrder.verify(mock).simpleMethod(10);
        try {
            inOrder.verifyNoMoreInteractions();
            fail();
        } catch(VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.FindingRedundantInvocationsInOrderTest::shouldFailToVerifyNoMoreInteractionsInOrderWithMultipleMocks
    public void shouldFailToVerifyNoMoreInteractionsInOrderWithMultipleMocks() throws Exception {
        
        mock.simpleMethod();
        mock2.simpleMethod();
        mock.otherMethod();
        
        
        InOrder inOrder = inOrder(mock, mock2);
        inOrder.verify(mock2).simpleMethod();
        try {
            inOrder.verifyNoMoreInteractions();
            fail();
        } catch(VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.FindingRedundantInvocationsInOrderTest::shouldValidateState
    public void shouldValidateState() throws Exception {
        
        InOrder inOrder = inOrder(mock);
        verify(mock); 
        
        
        try {
            inOrder.verifyNoMoreInteractions();
            fail();
        } catch(UnfinishedVerificationException e) {}
    }

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::shouldStubbingNotRegisterRedundantInteractions
    public void shouldStubbingNotRegisterRedundantInteractions() throws Exception {
        when(mock.add("one")).thenReturn(true);
        when(mock.add("two")).thenReturn(true);

        mock.add("one");
        
        verify(mock).add("one");
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::shouldVerifyWhenWantedNumberOfInvocationsUsed
    public void shouldVerifyWhenWantedNumberOfInvocationsUsed() throws Exception {
        mock.add("one");
        mock.add("one");
        mock.add("one");
        
        verify(mock, times(3)).add("one");
        
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::shouldVerifyNoInteractionsAsManyTimesAsYouWant
    public void shouldVerifyNoInteractionsAsManyTimesAsYouWant() throws Exception {
        verifyNoMoreInteractions(mock);
        verifyNoMoreInteractions(mock);
        
        verifyZeroInteractions(mock);
        verifyZeroInteractions(mock);
    }

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::shouldFailZeroInteractionsVerification
    public void shouldFailZeroInteractionsVerification() throws Exception {
        mock.clear();
        
        try {
            verifyZeroInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::shouldFailNoMoreInteractionsVerification
    public void shouldFailNoMoreInteractionsVerification() throws Exception {
        mock.clear();
        
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::shouldPrintAllInvocationsWhenVerifyingNoMoreInvocations
    public void shouldPrintAllInvocationsWhenVerifyingNoMoreInvocations() throws Exception {
        mock.add(1);
        mock.add(2);
        mock.clear();
        
        verify(mock).add(2);
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {
            assertContains("list of all invocations", e.getMessage());
        }
    }

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::shouldNotContainAllInvocationsWhenSingleUnwantedFound
    public void shouldNotContainAllInvocationsWhenSingleUnwantedFound() throws Exception {
        mock.add(1);
        
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {
            assertNotContains("list of all invocations", e.getMessage());
        }
    }

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::shouldVerifyOneMockButFailOnOther
    public void shouldVerifyOneMockButFailOnOther() throws Exception {
        List list = mock(List.class);
        Map map = mock(Map.class);

        list.add("one");
        list.add("one");
        
        map.put("one", 1);
        
        verify(list, times(2)).add("one");
        
        verifyNoMoreInteractions(list);
        try {
            verifyZeroInteractions(map);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.NoMoreInteractionsVerificationTest::verifyNoMoreInteractionsShouldScreamWhenNullPassed
    public void verifyNoMoreInteractionsShouldScreamWhenNullPassed() throws Exception {
        verifyNoMoreInteractions((Object[])null);
    }

// org.mockitousage.verification.OnlyVerificationTest::shouldVerifyMethodWasInvokedExclusively
	public void shouldVerifyMethodWasInvokedExclusively() {
		mock.clear();
		verify(mock, only()).clear();
	}

// org.mockitousage.verification.OnlyVerificationTest::shouldVerifyMethodWasInvokedExclusivelyWithMatchersUsage
	public void shouldVerifyMethodWasInvokedExclusivelyWithMatchersUsage() {
		mock.get(0);
		verify(mock, only()).get(anyInt());
	}

// org.mockitousage.verification.OnlyVerificationTest::shouldFailIfMethodWasNotInvoked
	public void shouldFailIfMethodWasNotInvoked() {
		mock.clear();
		try {
			verify(mock, only()).get(0);
			fail();
		} catch (WantedButNotInvoked e) {}
	}

// org.mockitousage.verification.OnlyVerificationTest::shouldFailIfMethodWasInvokedMoreThanOnce
	public void shouldFailIfMethodWasInvokedMoreThanOnce() {
		mock.clear();
		mock.clear();
		try {
			verify(mock, only()).clear();
			fail();
		} catch (NoInteractionsWanted e) {}
	}

// org.mockitousage.verification.OnlyVerificationTest::shouldFailIfMethodWasInvokedButWithDifferentArguments
	public void shouldFailIfMethodWasInvokedButWithDifferentArguments() {
		mock.get(0);
		mock.get(2);
		try {
			verify(mock, only()).get(999);
			fail();
		} catch (WantedButNotInvoked e) {}
	}

// org.mockitousage.verification.OnlyVerificationTest::shouldFailIfExtraMethodWithDifferentArgsFound
	public void shouldFailIfExtraMethodWithDifferentArgsFound() {
	    mock.get(0);
	    mock.get(2);
	    try {
	        verify(mock, only()).get(2);
	        fail();
	    } catch (NoInteractionsWanted e) {}
	}

// org.mockitousage.verification.OnlyVerificationTest::shouldVerifyMethodWasInvokedExclusivelyWhenTwoMocksInUse
	public void shouldVerifyMethodWasInvokedExclusivelyWhenTwoMocksInUse() {
		mock.clear();
		mock2.get(0);
		verify(mock, only()).clear();
		verify(mock2, only()).get(0);
	}

// org.mockitousage.verification.OrdinaryVerificationPrintsAllInteractionsTest::shouldShowAllInteractionsOnMockWhenOrdinaryVerificationFail
    public void shouldShowAllInteractionsOnMockWhenOrdinaryVerificationFail() throws Exception {
        firstInteraction();
        secondInteraction();
        
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (WantedButNotInvoked e) {
            assertContains("However, there were other interactions with this mock", e.getMessage());
            assertContains("firstInteraction(", e.getMessage());
            assertContains("secondInteraction(", e.getMessage());
        }
    }

// org.mockitousage.verification.OrdinaryVerificationPrintsAllInteractionsTest::shouldNotShowAllInteractionsOnDifferentMock
    public void shouldNotShowAllInteractionsOnDifferentMock() throws Exception {
        differentMockInteraction();
        firstInteraction();
        
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (WantedButNotInvoked e) {
            assertContains("firstInteraction(", e.getMessage());
            assertNotContains("differentMockInteraction(", e.getMessage());
        }
    }

// org.mockitousage.verification.OrdinaryVerificationPrintsAllInteractionsTest::shouldNotShowAllInteractionsHeaderWhenNoOtherInteractions
    public void shouldNotShowAllInteractionsHeaderWhenNoOtherInteractions() throws Exception {
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (WantedButNotInvoked e) {
            assertContains("there were zero interactions with this mock.", e.getMessage());
        }
    }

// org.mockitousage.verification.PrintingVerboseTypesWithArgumentsTest::shouldNotReportArgumentTypesWhenToStringIsTheSame
    public void shouldNotReportArgumentTypesWhenToStringIsTheSame() throws Exception {
        
        Boo boo = mock(Boo.class);
        boo.withLong(100);
        
        try {
            
            verify(boo).withLong(eq(100));
            fail();
        } catch (ArgumentsAreDifferent e) {
            
            assertContains("withLong((Integer) 100);", e.getMessage());
            assertContains("withLong((Long) 100);", e.getMessage());
        }
    }

// org.mockitousage.verification.PrintingVerboseTypesWithArgumentsTest::shouldShowTheTypeOfOnlyTheArgumentThatDoesntMatch
    public void shouldShowTheTypeOfOnlyTheArgumentThatDoesntMatch() throws Exception {
        
        Boo boo = mock(Boo.class);
        boo.withLongAndInt(100, 200);
        
        try {
            
            verify(boo).withLongAndInt(eq(100), eq(200));
            fail();
        } catch (ArgumentsAreDifferent e) {
            
            assertContains("withLongAndInt((Integer) 100, 200)", e.getMessage());
            assertContains("withLongAndInt((Long) 100, 200)", e.getMessage());
        }
    }

// org.mockitousage.verification.PrintingVerboseTypesWithArgumentsTest::shouldShowTheTypeOfTheMismatchingArgumentWhenOutputDescriptionsForInvocationsAreDifferent
    public void shouldShowTheTypeOfTheMismatchingArgumentWhenOutputDescriptionsForInvocationsAreDifferent() throws Exception {
        
        Boo boo = mock(Boo.class);
        boo.withLongAndInt(100, 200);
        
        try {
            
            verify(boo).withLongAndInt(eq(100), anyInt());
            fail();
        } catch (ArgumentsAreDifferent e) {
            
            assertContains("withLongAndInt((Long) 100, 200)", e.getMessage());
            assertContains("withLongAndInt((Integer) 100, <any>)", e.getMessage());
        }
    }

// org.mockitousage.verification.PrintingVerboseTypesWithArgumentsTest::shouldNotShowTypesWhenArgumentValueIsDifferent
    public void shouldNotShowTypesWhenArgumentValueIsDifferent() throws Exception {
        
        Boo boo = mock(Boo.class);
        boo.withLongAndInt(100, 200);
        
        try {
            
            verify(boo).withLongAndInt(eq(100L), eq(230));
            fail();
        } catch (ArgumentsAreDifferent e) {
            
            assertContains("withLongAndInt(100, 200)", e.getMessage());
            assertContains("withLongAndInt(100, 230)", e.getMessage());
        }
    }

// org.mockitousage.verification.PrintingVerboseTypesWithArgumentsTest::shouldNotShowTypesWhenTypesAreTheSameEvenIfToStringGivesTheSameResult
    public void shouldNotShowTypesWhenTypesAreTheSameEvenIfToStringGivesTheSameResult() throws Exception {
        
        IMethods mock = mock(IMethods.class);
        mock.simpleMethod(new Foo(10));
        
        try {
            
            verify(mock).simpleMethod(new Foo(20));
            fail();
        } catch (ArgumentsAreDifferent e) {
            
            assertContains("simpleMethod(foo)", e.getMessage());
        }
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyInOrderAllInvocations
    public void shouldVerifyInOrderAllInvocations() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyInOrderAndBeRelaxed
    public void shouldVerifyInOrderAndBeRelaxed() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        
        verifyNoMoreInteractions(mockThree);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldAllowFirstChunkBeforeLastInvocation
    public void shouldAllowFirstChunkBeforeLastInvocation() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        
        try {
            verifyNoMoreInteractions(mockTwo);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldAllowAllChunksBeforeLastInvocation
    public void shouldAllowAllChunksBeforeLastInvocation() {
        inOrder.verify(mockTwo, times(3)).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        
        verifyNoMoreInteractions(mockTwo);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyDetectFirstChunkOfInvocationThatExistInManyChunks
    public void shouldVerifyDetectFirstChunkOfInvocationThatExistInManyChunks() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        try {
            verifyNoMoreInteractions(mockTwo);
            fail();
        } catch(NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyDetectAllChunksOfInvocationThatExistInManyChunks
    public void shouldVerifyDetectAllChunksOfInvocationThatExistInManyChunks() {
        inOrder.verify(mockTwo, times(3)).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        verifyNoMoreInteractions(mockTwo);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyInteractionsFromAllChunksWhenAtLeastOnceMode
    public void shouldVerifyInteractionsFromAllChunksWhenAtLeastOnceMode() {
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        verifyNoMoreInteractions(mockTwo);
        try {
            inOrder.verify(mockThree).simpleMethod(3);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyInteractionsFromFirstChunk
    public void shouldVerifyInteractionsFromFirstChunk() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        try {
            verifyNoMoreInteractions(mockTwo);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldFailVerificationOfNonFirstChunk
    public void shouldFailVerificationOfNonFirstChunk() {
        inOrder.verify(mockTwo, times(1)).simpleMethod(2);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldPassOnCombinationOfTimesAndAtLeastOnce
    public void shouldPassOnCombinationOfTimesAndAtLeastOnce() {
        mockTwo.simpleMethod(2);
        
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        verifyNoMoreInteractions(mockTwo);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldPassOnEdgyCombinationOfTimesAndAtLeastOnce
    public void shouldPassOnEdgyCombinationOfTimesAndAtLeastOnce() {
        mockTwo.simpleMethod(2);
        mockThree.simpleMethod(3);
        
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        
        verifyNoMoreInteractions(mockThree);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyInOrderMockTwoAndThree
    public void shouldVerifyInOrderMockTwoAndThree() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        verifyNoMoreInteractions(mockTwo, mockThree);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyInOrderMockOneAndThree
    public void shouldVerifyInOrderMockOneAndThree() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockOne).simpleMethod(4);
        verifyNoMoreInteractions(mockOne, mockThree);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyInOrderOnlyTwoInvocations
    public void shouldVerifyInOrderOnlyTwoInvocations() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyInOrderOnlyMockTwo
    public void shouldVerifyInOrderOnlyMockTwo() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockTwo).simpleMethod(2);
        verifyNoMoreInteractions(mockTwo);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyMockTwoCalledTwice
    public void shouldVerifyMockTwoCalledTwice() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyMockTwoCalledAtLeastOnce
    public void shouldVerifyMockTwoCalledAtLeastOnce() {
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldFailOnWrongMethodCalledOnMockTwo
    public void shouldFailOnWrongMethodCalledOnMockTwo() {
        inOrder.verify(mockTwo, atLeastOnce()).differentMethod();
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldAllowTimesZeroButOnlyInOrder
    public void shouldAllowTimesZeroButOnlyInOrder() {
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        inOrder.verify(mockOne, times(0)).simpleMethod(1);
        
        try {
            verify(mockOne, times(0)).simpleMethod(1);
            fail();
        } catch (NeverWantedButInvoked e) {}
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldFailTimesZeroInOrder
    public void shouldFailTimesZeroInOrder() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        try {
            inOrder.verify(mockThree, times(0)).simpleMethod(3);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldFailWhenMockTwoWantedZeroTimes
    public void shouldFailWhenMockTwoWantedZeroTimes() {
        inOrder.verify(mockTwo, times(0)).simpleMethod(2);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifyLastInvocation
    public void shouldVerifyLastInvocation() {
        inOrder.verify(mockOne).simpleMethod(4);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifySecondAndLastInvocation
    public void shouldVerifySecondAndLastInvocation() {
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldVerifySecondAndLastInvocationWhenAtLeastOnceUsed
    public void shouldVerifySecondAndLastInvocationWhenAtLeastOnceUsed() {
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldFailOnLastTwoInvocationsInWrongOrder
    public void shouldFailOnLastTwoInvocationsInWrongOrder() {
        inOrder.verify(mockOne).simpleMethod(4);
        try {
            inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldFailOnLastAndFirstInWrongOrder
    public void shouldFailOnLastAndFirstInWrongOrder() {
        inOrder.verify(mockOne).simpleMethod(4);
        try {
            inOrder.verify(mockOne).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.RelaxedVerificationInOrderTest::shouldFailOnWrongMethodAfterLastInvocation
    public void shouldFailOnWrongMethodAfterLastInvocation() {
        inOrder.verify(mockOne).simpleMethod(4);
        try {
            inOrder.verify(mockOne).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldVerifyAllInvocationsInOrder
    public void shouldVerifyAllInvocationsInOrder() {
        InOrder inOrder = inOrder(mockOne, mockTwo, mockThree);
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldVerifyInOrderMockTwoAndThree
    public void shouldVerifyInOrderMockTwoAndThree() {
        InOrder inOrder = inOrder(mockTwo, mockThree);
        
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        verifyNoMoreInteractions(mockTwo, mockThree);
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldVerifyInOrderMockOneAndThree
    public void shouldVerifyInOrderMockOneAndThree() {
        InOrder inOrder = inOrder(mockOne, mockThree);
        
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockOne).simpleMethod(4);
        verifyNoMoreInteractions(mockOne, mockThree);
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldVerifyMockOneInOrder
    public void shouldVerifyMockOneInOrder() {
        InOrder inOrder = inOrder(mockOne);
        
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockOne).simpleMethod(4);
        
        verifyNoMoreInteractions(mockOne);
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldFailVerificationForMockOne
    public void shouldFailVerificationForMockOne() {
        InOrder inOrder = inOrder(mockOne);
        
        inOrder.verify(mockOne).simpleMethod(1);
        try {
            inOrder.verify(mockOne).differentMethod();
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldFailVerificationForMockOneBecauseOfWrongOrder
    public void shouldFailVerificationForMockOneBecauseOfWrongOrder() {
        InOrder inOrder = inOrder(mockOne);
        
        inOrder.verify(mockOne).simpleMethod(4);
        try {
            inOrder.verify(mockOne).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldVerifyMockTwoWhenThreeTimesUsed
    public void shouldVerifyMockTwoWhenThreeTimesUsed() {
        InOrder inOrder = inOrder(mockTwo);
        
        inOrder.verify(mockTwo, times(3)).simpleMethod(2);
        
        verifyNoMoreInteractions(mockTwo);
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldVerifyMockTwo
    public void shouldVerifyMockTwo() {
        InOrder inOrder = inOrder(mockTwo);
        
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        
        verifyNoMoreInteractions(mockTwo);
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldFailVerificationForMockTwo
    public void shouldFailVerificationForMockTwo() {
        InOrder inOrder = inOrder(mockTwo);

        try {
            inOrder.verify(mockTwo).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldThrowNoMoreInvocationsForMockTwo
    public void shouldThrowNoMoreInvocationsForMockTwo() {
        InOrder inOrder = inOrder(mockTwo);

        try {
            inOrder.verify(mockTwo, times(2)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldThrowTooLittleInvocationsForMockTwo
    public void shouldThrowTooLittleInvocationsForMockTwo() {
        InOrder inOrder = inOrder(mockTwo);

        try {
            inOrder.verify(mockTwo, times(4)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldThrowTooManyInvocationsForMockTwo
    public void shouldThrowTooManyInvocationsForMockTwo() {
        InOrder inOrder = inOrder(mockTwo);

        try {
            inOrder.verify(mockTwo, times(2)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldAllowThreeTimesOnMockTwo
    public void shouldAllowThreeTimesOnMockTwo() {
        InOrder inOrder = inOrder(mockTwo);

        inOrder.verify(mockTwo, times(3)).simpleMethod(2);
        verifyNoMoreInteractions(mockTwo);
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldVerifyMockTwoCompletely
    public void shouldVerifyMockTwoCompletely() {
        InOrder inOrder = inOrder(mockTwo, mockThree);

        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        verifyNoMoreInteractions(mockTwo, mockThree);
    }

// org.mockitousage.verification.SelectedMocksInOrderVerificationTest::shouldAllowTwoTimesOnMockTwo
    public void shouldAllowTwoTimesOnMockTwo() {
        InOrder inOrder = inOrder(mockTwo, mockThree);

        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        try {
            verifyNoMoreInteractions(mockTwo);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.VerificationAfterDelayTest::shouldVerifyNormallyWithSpecificTimes
    public void shouldVerifyNormallyWithSpecificTimes() {}

// org.mockitousage.verification.VerificationAfterDelayTest::shouldVerifyNormallyWithAtLeast
    public void shouldVerifyNormallyWithAtLeast() {}

// org.mockitousage.verification.VerificationAfterDelayTest::shouldFailVerificationWithWrongTimes
    public void shouldFailVerificationWithWrongTimes() throws Exception {
        
        Thread t = waitAndExerciseMock(20);

        
        t.start();

        
        verify(mock, times(0)).clear();
        
        expected.expect(MockitoAssertionError.class);
        verify(mock, after(50).times(2)).clear();
    }

// org.mockitousage.verification.VerificationAfterDelayTest::shouldWaitTheFullTimeIfTheTestCouldPass
    public void shouldWaitTheFullTimeIfTheTestCouldPass() throws Exception {
        
        Thread t = waitAndExerciseMock(50);

        
        t.start();

        
        long startTime = System.currentTimeMillis();
        
        try {
            verify(mock, after(100).atLeast(2)).clear();
            fail();
        } catch (MockitoAssertionError e) {}
        
        assertTrue(System.currentTimeMillis() - startTime >= 100);
    }

// org.mockitousage.verification.VerificationAfterDelayTest::shouldStopEarlyIfTestIsDefinitelyFailed
    public void shouldStopEarlyIfTestIsDefinitelyFailed() throws Exception {
        
        Thread t = waitAndExerciseMock(50);
        
        
        t.start();
        
        
        expected.expect(MockitoAssertionError.class);
        verify(mock, after(10000).never()).clear();
    }

// org.mockitousage.verification.VerificationExcludingStubsTest::shouldAllowToExcludeStubsForVerification
    public void shouldAllowToExcludeStubsForVerification() throws Exception {
        
        when(mock.simpleMethod()).thenReturn("foo");

        
        String stubbed = mock.simpleMethod(); 
        mock.objectArgMethod(stubbed);

        
        verify(mock).objectArgMethod("foo");

        
        try { verifyNoMoreInteractions(mock); fail(); } catch (NoInteractionsWanted e) {};
        
        
        ignoreStubs(mock);
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.verification.VerificationExcludingStubsTest::shouldExcludeFromVerificationInOrder
    public void shouldExcludeFromVerificationInOrder() throws Exception {
        
        when(mock.simpleMethod()).thenReturn("foo");

        
        mock.objectArgMethod("1");
        mock.objectArgMethod("2");
        mock.simpleMethod(); 

        
        InOrder inOrder = inOrder(ignoreStubs(mock));
        inOrder.verify(mock).objectArgMethod("1");
        inOrder.verify(mock).objectArgMethod("2");
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.verification.VerificationExcludingStubsTest::shouldIgnoringStubsDetectNulls
    public void shouldIgnoringStubsDetectNulls() throws Exception {
        ignoreStubs(mock, null);
    }

// org.mockitousage.verification.VerificationExcludingStubsTest::shouldIgnoringStubsDetectNonMocks
    public void shouldIgnoringStubsDetectNonMocks() throws Exception {
        ignoreStubs(mock, new Object());
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldMixVerificationInOrderAndOrdinaryVerification
    public void shouldMixVerificationInOrderAndOrdinaryVerification() {
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockThree).simpleMethod(4);
        verify(mockTwo).simpleMethod(2);
        
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldAllowOrdinarilyVerifyingMockPassedToInOrderObject
    public void shouldAllowOrdinarilyVerifyingMockPassedToInOrderObject() {
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);

        verify(mockThree).simpleMethod(3);
        verify(mockThree).simpleMethod(4);
        verify(mockTwo).simpleMethod(2);
        
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldAllowRedundantVerifications
    public void shouldAllowRedundantVerifications() {
        verify(mockOne, atLeastOnce()).simpleMethod(1);
        verify(mockTwo).simpleMethod(2);
        verify(mockThree).simpleMethod(3);
        verify(mockThree).simpleMethod(4);
        
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockThree).simpleMethod(4);
        
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldFailOnNoMoreInteractions
    public void shouldFailOnNoMoreInteractions() {
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockThree).simpleMethod(4);
        
        try {
            verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldFailOnNoMoreInteractionsOnMockVerifiedInOrder
    public void shouldFailOnNoMoreInteractionsOnMockVerifiedInOrder() {
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
        inOrder.verify(mockThree).simpleMethod(3);
        verify(mockTwo).simpleMethod(2);
        
        try {
            verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldAllowOneMethodVerifiedInOrder
    public void shouldAllowOneMethodVerifiedInOrder() {
        verify(mockTwo).simpleMethod(2);
        verify(mockOne, atLeastOnce()).simpleMethod(1);

        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldFailOnLastInvocationTooEarly
    public void shouldFailOnLastInvocationTooEarly() {
        inOrder.verify(mockThree).simpleMethod(4);
        
        verify(mockThree).simpleMethod(4);
        verify(mockTwo).simpleMethod(2);
        
        try {
            inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldScreamWhenUnfamiliarMockPassedToInOrderObject
    public void shouldScreamWhenUnfamiliarMockPassedToInOrderObject() {
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(1);
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldUseEqualsToVerifyMethodArguments
    public void shouldUseEqualsToVerifyMethodArguments() {
        mockOne = mock(IMethods.class);
        
        String textOne = "test";
        String textTwo = new String(textOne);
        
        assertEquals(textOne, textTwo);
        assertNotSame(textOne, textTwo);
        
        mockOne.simpleMethod(textOne);
        mockOne.simpleMethod(textTwo);
        
        verify(mockOne, times(2)).simpleMethod(textOne);
        
        inOrder = inOrder(mockOne);
        inOrder.verify(mockOne, times(2)).simpleMethod(textOne);
    }

// org.mockitousage.verification.VerificationInOrderMixedWithOrdiraryVerificationTest::shouldUseEqualsToVerifyMethodVarargs
    public void shouldUseEqualsToVerifyMethodVarargs() {
        mockOne = mock(IMethods.class);
        
        String textOne = "test";
        String textTwo = new String(textOne);
        
        assertEquals(textOne, textTwo);
        assertNotSame(textOne, textTwo);
        
        mockOne.varargsObject(1, textOne, textOne);
        mockOne.varargsObject(1, textTwo, textTwo);
        
        verify(mockOne, times(2)).varargsObject(1, textOne, textOne);
        
        inOrder = inOrder(mockOne);
        inOrder.verify(mockOne, times(2)).varargsObject(1, textOne, textOne);
    }

// org.mockitousage.verification.VerificationInOrderTest::shouldVerifySingleMockInOrderAndNotInOrder
    public void shouldVerifySingleMockInOrderAndNotInOrder() {
        mockOne = mock(IMethods.class);
        inOrder = inOrder(mockOne);
        
        mockOne.simpleMethod(1);
        mockOne.simpleMethod(2);
        
        verify(mockOne).simpleMethod(2);
        verify(mockOne).simpleMethod(1);
        
        inOrder.verify(mockOne).simpleMethod(2);
        try {
            inOrder.verify(mockOne).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.VerificationInOrderTest::shouldMessagesPointToProperMethod
    public void shouldMessagesPointToProperMethod() {
        mockTwo.differentMethod();
        mockOne.simpleMethod();
        
        try {
            inOrder.verify(mockOne, atLeastOnce()).differentMethod();
            fail();
        } catch (WantedButNotInvoked e) {
            assertContains("differentMethod()", e.getMessage());
        }
    }

// org.mockitousage.verification.VerificationInOrderTest::shouldVerifyInOrderWhenTwoChunksAreEqual
    public void shouldVerifyInOrderWhenTwoChunksAreEqual() {
        mockOne.simpleMethod();
        mockOne.simpleMethod();
        mockTwo.differentMethod();
        mockOne.simpleMethod();
        mockOne.simpleMethod();
        
        inOrder.verify(mockOne, times(2)).simpleMethod();
        inOrder.verify(mockTwo).differentMethod();
        inOrder.verify(mockOne, times(2)).simpleMethod();
        try {
            inOrder.verify(mockOne, atLeastOnce()).simpleMethod();
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.VerificationInOrderTest::shouldVerifyInOrderUsingMatcher
    public void shouldVerifyInOrderUsingMatcher() {
        mockOne.simpleMethod(1);
        mockOne.simpleMethod(2);
        mockTwo.differentMethod();
        mockOne.simpleMethod(3);
        mockOne.simpleMethod(4);
        
        verify(mockOne, times(4)).simpleMethod(anyInt());
        
        inOrder.verify(mockOne, times(2)).simpleMethod(anyInt());
        inOrder.verify(mockTwo).differentMethod();
        inOrder.verify(mockOne, times(2)).simpleMethod(anyInt());
        try {
            inOrder.verify(mockOne, times(3)).simpleMethod(anyInt());
            fail();
        } catch (VerificationInOrderFailure e) {}
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldFailWhenMethodNotCalled
    public void shouldFailWhenMethodNotCalled(){
        
        mockOne.oneArg( 1 );
        InOrder verifier = inOrder( mockOne );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );

        exceptionRule.expect( VerificationInOrderFailure.class );
        exceptionRule.expectMessage( "Verification in order failure" );
        exceptionRule.expectMessage( "Wanted but not invoked" );
        exceptionRule.expectMessage( "mockOne.oneArg(2)" );

        
        verifier.verify( mockOne, calls(1)).oneArg( 2 );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldFailWhenMethodCalledTooFewTimes
    public void shouldFailWhenMethodCalledTooFewTimes(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );

        InOrder verifier = inOrder( mockOne );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );

        exceptionRule.expect( VerificationInOrderFailure.class );
        exceptionRule.expectMessage( "Verification in order failure" );
        exceptionRule.expectMessage( "mockOne.oneArg(2)" );
        exceptionRule.expectMessage( "Wanted 2 times" );
        exceptionRule.expectMessage( "But was 1 time" );

        
        verifier.verify( mockOne, calls(2)).oneArg( 2 );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldFailWhenSingleMethodCallsAreOutOfSequence
    public void shouldFailWhenSingleMethodCallsAreOutOfSequence(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );

        InOrder verifier = inOrder( mockOne );
        verifier.verify( mockOne, calls(1)).oneArg( 2 );

        exceptionRule.expect( VerificationInOrderFailure.class );
        exceptionRule.expectMessage( "Verification in order failure" );
        exceptionRule.expectMessage( "Wanted but not invoked" );
        exceptionRule.expectMessage( "mockOne.oneArg(1)" );

        
        verifier.verify( mockOne, calls(1)).oneArg( 1 );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldFailWhenDifferentMethodCallsAreOutOfSequence
    public void shouldFailWhenDifferentMethodCallsAreOutOfSequence(){
        
        mockOne.oneArg( 1 );
        mockOne.voidMethod();

        InOrder verifier = inOrder( mockOne );
        verifier.verify( mockOne, calls(1)).voidMethod();

        exceptionRule.expect( VerificationInOrderFailure.class );
        exceptionRule.expectMessage( "Verification in order failure" );
        exceptionRule.expectMessage( "Wanted but not invoked" );
        exceptionRule.expectMessage( "mockOne.oneArg(1)" );

        
        verifier.verify( mockOne, calls(1)).oneArg( 1 );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldFailWhenMethodCallsOnDifferentMocksAreOutOfSequence
    public void shouldFailWhenMethodCallsOnDifferentMocksAreOutOfSequence(){
        
        mockOne.voidMethod();
        mockTwo.voidMethod();

        InOrder verifier = inOrder( mockOne, mockTwo );
        verifier.verify( mockTwo, calls(1)).voidMethod();

        exceptionRule.expect( VerificationInOrderFailure.class );
        exceptionRule.expectMessage( "Verification in order failure" );
        exceptionRule.expectMessage( "Wanted but not invoked" );
        exceptionRule.expectMessage( "mockOne.voidMethod()" );

        
        verifier.verify( mockOne, calls(1)).voidMethod();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldAllowSequentialCallsToCallsForSingleMethod
    public void shouldAllowSequentialCallsToCallsForSingleMethod(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 1 );

        InOrder verifier = inOrder( mockOne );

        
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(2)).oneArg( 2 );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifyNoMoreInteractions(mockOne);
        verifier.verifyNoMoreInteractions();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldAllowSequentialCallsToCallsForDifferentMethods
    public void shouldAllowSequentialCallsToCallsForDifferentMethods(){
        
        mockOne.oneArg( 1 );
        mockOne.voidMethod();
        mockOne.voidMethod();
        mockOne.oneArg( 1 );

        InOrder verifier = inOrder( mockOne );

        
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(2)).voidMethod();
        verifier.verify( mockOne, calls(1)).oneArg(1);
        verifyNoMoreInteractions(mockOne);
        verifier.verifyNoMoreInteractions();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldAllowSequentialCallsToCallsForMethodsOnDifferentMocks
    public void shouldAllowSequentialCallsToCallsForMethodsOnDifferentMocks(){
        
        mockOne.voidMethod();
        mockTwo.voidMethod();
        mockTwo.voidMethod();
        mockOne.voidMethod();

        InOrder verifier = inOrder( mockOne, mockTwo );

        
        verifier.verify( mockOne, calls(1)).voidMethod();
        verifier.verify( mockTwo, calls(2)).voidMethod();
        verifier.verify( mockOne, calls(1)).voidMethod();
        verifyNoMoreInteractions(mockOne);
        verifyNoMoreInteractions(mockTwo);
        verifier.verifyNoMoreInteractions();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldAllowFewerCallsForSingleMethod
    public void shouldAllowFewerCallsForSingleMethod(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );

        InOrder verifier = inOrder( mockOne );

        
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(1)).oneArg( 2 );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(1)).oneArg( 2 );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldNotVerifySkippedCallsWhenFewerCallsForSingleMethod
    public void shouldNotVerifySkippedCallsWhenFewerCallsForSingleMethod(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 1 );

        InOrder verifier = inOrder( mockOne );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(1)).oneArg( 2 );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        
        exceptionRule.expect( NoInteractionsWanted.class );

        
        verifyNoMoreInteractions( mockOne );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldNotVerifySkippedCallsInInOrderWhenFewerCallsForSingleMethod
    public void shouldNotVerifySkippedCallsInInOrderWhenFewerCallsForSingleMethod(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 2 );

        InOrder verifier = inOrder( mockOne );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(1)).oneArg( 2 );

        exceptionRule.expect( VerificationInOrderFailure.class );
        exceptionRule.expectMessage( "No interactions wanted here" );

        
        verifier.verifyNoMoreInteractions();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldAllowFewerCallsForDifferentMethods
    public void shouldAllowFewerCallsForDifferentMethods(){
        
        mockOne.oneArg( 1 );
        mockOne.voidMethod();
        mockOne.voidMethod();
        mockOne.oneArg( 1 );
        mockOne.voidMethod();

        InOrder verifier = inOrder( mockOne );

        
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(1)).voidMethod();
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(1)).voidMethod();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldNotVerifySkippedCallsWhenFewerCallsForDifferentMethods
    public void shouldNotVerifySkippedCallsWhenFewerCallsForDifferentMethods(){
        
        mockOne.oneArg( 1 );
        mockOne.voidMethod();
        mockOne.voidMethod();
        mockOne.oneArg( 1 );

        InOrder verifier = inOrder( mockOne );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(1)).voidMethod();
        verifier.verify( mockOne, calls(1)).oneArg( 1 );

        exceptionRule.expect( NoInteractionsWanted.class );

        
        verifyNoMoreInteractions( mockOne );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldNotVerifySkippedCallsInInOrderWhenFewerCallsForDifferentMethods
    public void shouldNotVerifySkippedCallsInInOrderWhenFewerCallsForDifferentMethods(){
        
        mockOne.oneArg( 1 );
        mockOne.voidMethod();
        mockOne.voidMethod();

        InOrder verifier = inOrder( mockOne );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(1)).voidMethod();

        exceptionRule.expect( VerificationInOrderFailure.class );
        exceptionRule.expectMessage( "No interactions wanted here" );

        
        verifier.verifyNoMoreInteractions();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldAllowFewerCallsForMethodsOnDifferentMocks
    public void shouldAllowFewerCallsForMethodsOnDifferentMocks(){
        
        mockOne.voidMethod();
        mockTwo.voidMethod();
        mockTwo.voidMethod();
        mockOne.voidMethod();
        mockTwo.voidMethod();

        InOrder verifier = inOrder( mockOne, mockTwo );

        
        verifier.verify( mockOne, calls(1)).voidMethod();
        verifier.verify( mockTwo, calls(1)).voidMethod();
        verifier.verify( mockOne, calls(1)).voidMethod();
        verifier.verify( mockTwo, calls(1)).voidMethod();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldNotVerifySkippedCallsWhenFewerCallsForMethodsOnDifferentMocks
    public void shouldNotVerifySkippedCallsWhenFewerCallsForMethodsOnDifferentMocks(){
        
        mockOne.voidMethod();
        mockTwo.voidMethod();
        mockTwo.voidMethod();
        mockOne.voidMethod();

        InOrder verifier = inOrder( mockOne, mockTwo );
        verifier.verify( mockOne, calls(1)).voidMethod();
        verifier.verify( mockTwo, calls(1)).voidMethod();
        verifier.verify( mockOne, calls(1)).voidMethod();

        exceptionRule.expect(NoInteractionsWanted.class);

        
        verifyNoMoreInteractions( mockTwo );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldNotVerifySkippedCallsInInOrderWhenFewerCallsForMethodsOnDifferentMocks
    public void shouldNotVerifySkippedCallsInInOrderWhenFewerCallsForMethodsOnDifferentMocks(){
        
        mockOne.voidMethod();
        mockTwo.voidMethod();
        mockTwo.voidMethod();

        InOrder verifier = inOrder( mockOne, mockTwo );
        verifier.verify( mockOne, calls(1)).voidMethod();
        verifier.verify( mockTwo, calls(1)).voidMethod();

        exceptionRule.expect( VerificationInOrderFailure.class );
        exceptionRule.expectMessage( "No interactions wanted here" );

        
        verifier.verifyNoMoreInteractions();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldVerifyWithCallsAfterUseOfTimes
    public void shouldVerifyWithCallsAfterUseOfTimes(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 1 );

        InOrder verifier = inOrder( mockOne );

        
        verifier.verify( mockOne, times(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(2)).oneArg( 2 );
        verifier.verify( mockOne, calls(1)).oneArg( 1 );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldVerifyWithCallsAfterUseOfAtLeast
    public void shouldVerifyWithCallsAfterUseOfAtLeast(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 2 );

        InOrder verifier = inOrder( mockOne );

        
        verifier.verify( mockOne, atLeast(1)).oneArg( 1 );
        verifier.verify( mockOne, calls(2)).oneArg( 2 );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldVerifyWithTimesAfterUseOfCalls
    public void shouldVerifyWithTimesAfterUseOfCalls(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 1 );

        InOrder verifier = inOrder( mockOne );

        
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, times(2)).oneArg( 2 );
        verifier.verify( mockOne, times(1)).oneArg( 1 );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldVerifyWithAtLeastAfterUseOfCalls
    public void shouldVerifyWithAtLeastAfterUseOfCalls(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 2 );
        mockOne.oneArg( 1 );

        InOrder verifier = inOrder( mockOne );

        
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, atLeast(1)).oneArg( 2 );
        verifier.verify( mockOne, atLeast(1)).oneArg( 1 );

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldVerifyWithTimesAfterCallsInSameChunk
    public void shouldVerifyWithTimesAfterCallsInSameChunk(){
        
        mockOne.oneArg( 1 );
        mockOne.oneArg( 1 );
        mockOne.oneArg( 1 );

        InOrder verifier = inOrder( mockOne );

        
        verifier.verify( mockOne, calls(1)).oneArg( 1 );
        verifier.verify( mockOne, times(2)).oneArg( 1 );
        verifier.verifyNoMoreInteractions();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldFailToCreateCallsWithZeroArgument
    public void shouldFailToCreateCallsWithZeroArgument(){
        
        InOrder verifier = inOrder( mockOne );
        exceptionRule.expect( MockitoException.class );
        exceptionRule.expectMessage( "Negative and zero values are not allowed here" );

        
        verifier.verify( mockOne, calls(0)).voidMethod();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldFailToCreateCallsWithNegativeArgument
    public void shouldFailToCreateCallsWithNegativeArgument(){
        
        InOrder verifier = inOrder( mockOne );
        exceptionRule.expect( MockitoException.class );
        exceptionRule.expectMessage( "Negative and zero values are not allowed here" );

        
        verifier.verify( mockOne, calls(-1)).voidMethod();

        
    }

// org.mockitousage.verification.VerificationInOrderWithCallsTest::shouldFailToCreateCallsForNonInOrderVerification
    public void shouldFailToCreateCallsForNonInOrderVerification(){
        
        mockOne.voidMethod();
        exceptionRule.expect( MockitoException.class );
        exceptionRule.expectMessage( "calls is only intended to work with InOrder" );

        
        verify( mockOne, calls(1)).voidMethod();

        
    }

// org.mockitousage.verification.VerificationOnMultipleMocksUsingMatchersTest::shouldVerifyUsingMatchers
    public void shouldVerifyUsingMatchers() throws Exception {
        List list = Mockito.mock(List.class);
        HashMap map = Mockito.mock(HashMap.class);
        
        list.add("test");
        list.add(1, "test two");
        
        map.put("test", 100);
        map.put("test two", 200);
        
        verify(list).add(anyObject());
        verify(list).add(anyInt(), eq("test two"));
        
        verify(map, times(2)).put(anyObject(), anyObject());
        verify(map).put(eq("test two"), eq(200));
        
        verifyNoMoreInteractions(list, map);
    }

// org.mockitousage.verification.VerificationOnMultipleMocksUsingMatchersTest::shouldVerifyMultipleMocks
    public void shouldVerifyMultipleMocks() throws Exception {
        List list = mock(List.class);
        Map map = mock(Map.class);
        Set set = mock(Set.class);

        list.add("one");
        list.add("one");
        list.add("two");
        
        map.put("one", 1);
        map.put("one", 1);
        
        verify(list, times(2)).add("one");
        verify(list, times(1)).add("two");
        verify(list, times(0)).add("three");
        
        verify(map, times(2)).put(anyObject(), anyInt());
        
        verifyNoMoreInteractions(list, map);
        verifyZeroInteractions(set);
    }

// org.mockitousage.verification.VerificationUsingMatchersTest::shouldVerifyExactNumberOfInvocationsUsingMatcher
    public void shouldVerifyExactNumberOfInvocationsUsingMatcher() {
        mock.simpleMethod(1);
        mock.simpleMethod(2);
        mock.simpleMethod(3);
        
        verify(mock, times(3)).simpleMethod(anyInt());
    }

// org.mockitousage.verification.VerificationUsingMatchersTest::shouldVerifyUsingSameMatcher
    public void shouldVerifyUsingSameMatcher() {
        Object one = new String("1243");
        Object two = new String("1243");
        Object three = new String("1243");

        assertNotSame(one, two);
        assertEquals(one, two);
        assertEquals(two, three);

        mock.oneArg(one);
        mock.oneArg(two);
        
        verify(mock).oneArg(same(one));
        verify(mock, times(2)).oneArg(two);
        
        try {
            verify(mock).oneArg(same(three));
            fail();
        } catch (WantedButNotInvoked e) {}
    }
