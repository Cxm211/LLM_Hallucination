// com/google/javascript/jscomp/CodePrinterTest.java
public void testManyMultipliesLeftAssoc() {
    int numMults = 10000;
    List<String> numbers = Lists.newArrayList("0", "1");
    Node current = new Node(Token.MUL, Node.newNumber(0), Node.newNumber(1));
    for (int i = 2; i < numMults; i++) {
      current = new Node(Token.MUL, current);
      int num = i % 1000;
      numbers.add(String.valueOf(num));
      current.addChildToBack(Node.newNumber(num));
    }
    String expected = Joiner.on("*").join(numbers);
    String actual = printNode(current).replace("\n", "");
    assertEquals(expected, actual);
  }
