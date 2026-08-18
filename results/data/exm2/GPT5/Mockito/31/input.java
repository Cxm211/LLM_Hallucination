		private String formatMethodCall() {
			return invocation.getMethod().getName() + "()";
		}

// trigger testcase
@Test
    public void shouldPrintTheParametersWhenCallingAMethodWithArgs() throws Throwable {
    	Answer<Object> answer = new ReturnsSmartNulls();

    	Foo smartNull = (Foo) answer.answer(invocationOf(Foo.class, "withArgs", "oompa", "lumpa"));

    	assertEquals("SmartNull returned by unstubbed withArgs(oompa, lumpa) method on mock", smartNull + "");
    }
