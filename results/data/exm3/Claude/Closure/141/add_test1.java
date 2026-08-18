// com/google/javascript/jscomp/PureFunctionIdentifierTest.java
public void testCallFunctionWithNestedAnonymous() throws Exception {
  String source = "function f(){ return function(){} }\n" +
      "function g(){ f()() }\n" +
      "g()";

  checkMarkedCalls(source, ImmutableList.<String>of("f()", "f()()", "g"));
}