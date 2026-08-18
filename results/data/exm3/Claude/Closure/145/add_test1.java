// com/google/javascript/jscomp/CodePrinterTest.java
public void testNestedBlocksWithFunction() {
    // Test nested blocks with FUNCTION
    assertPrint("if(x){{function foo(){}}}",
        "if(x){function foo(){}}");
}