// com/google/javascript/jscomp/CodePrinterTest.java
public void testMixedAssociativeOps() {
    Node innerAdd = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    Node middleAdd = new Node(Token.ADD, innerAdd, Node.newNumber(3));
    Node outerMul = new Node(Token.MUL, middleAdd, Node.newNumber(4));
    Node finalAdd = new Node(Token.ADD, outerMul, Node.newNumber(5));
    
    String expected = "1+2+3*4+5";
    String actual = printNode(finalAdd).replace("\n", "");
    assertEquals(expected, actual);
  }