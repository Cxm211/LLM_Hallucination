// com/google/javascript/jscomp/CodePrinterTest.java
public void testNestedBlocksWithDoLoop() {
    // Test nested blocks with DO loop
    assertPrint("if(x){{do{foo()}while(y);}}",
        "if(x){do foo();while(y)}");
}