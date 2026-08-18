    public Object answer(InvocationOnMock invocation) throws Throwable {
        return invocation.callRealMethod();
    }

// trigger testcase
@Test
    public void abstractMethodReturnsDefault() {
    	AbstractThing thing = spy(AbstractThing.class);
    	assertEquals("abstract null", thing.fullName());
    }

@Test
    public void abstractMethodStubbed() {
    	AbstractThing thing = spy(AbstractThing.class);
    	when(thing.name()).thenReturn("me");
    	assertEquals("abstract me", thing.fullName());
    }

@Test
    public void testCallsRealInterfaceMethod() {
    	List<String> list = mock(List.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
    	assertNull(list.get(1));
    }
