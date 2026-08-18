    public void smartNullPointerException(Location location) {
        throw new SmartNullPointerException(join(
                "You have a NullPointerException here:",
                new Location(),
                "Because this method was *not* stubbed correctly:",
                location,
                ""
                ));
    }

        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            if (new ObjectMethodsGuru().isToString(method)) {
                return "SmartNull returned by unstubbed " + formatMethodCall()  + " method on mock";
            }

            new Reporter().smartNullPointerException(location);
            return null;
        }

// trigger testcase
@Test
	public void shouldPrintTheParametersOnSmartNullPointerExceptionMessage() throws Throwable {
    	Answer<Object> answer = new ReturnsSmartNulls();

        Foo smartNull = (Foo) answer.answer(invocationOf(Foo.class, "withArgs", "oompa", "lumpa"));

        try {
            smartNull.get();
            fail();
        } catch (SmartNullPointerException ex) {
        	String message = ex.getMessage();
        	assertTrue("Exception message should include oompa and lumpa, but was: " + message,
        			message.contains("oompa, lumpa"));
        }
	}
