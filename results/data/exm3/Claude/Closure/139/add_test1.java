// com/google/javascript/jscomp/NormalizeTest.java
public void testMoveFunctionsWithLabelsAndBlocks() throws Exception {
  test("function f() { while(true) { a:function bar() {} } }",
       "function f() { for(;;) { a:{ var bar = function () {} } } }");
  test("function f() { { a:function bar() {} } }",
       "function f() { { a:{ var bar = function () {} } } }");
}