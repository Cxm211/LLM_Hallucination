// com/google/javascript/jscomp/PureFunctionIdentifierTest.java
public void testCallFunctionFAndG() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){ (f && g)() }\n" +
        "h()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f && g)", "h"));
  }
