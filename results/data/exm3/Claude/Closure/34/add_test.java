// com/google/javascript/jscomp/CodePrinterTest.java
public void testManyMultiplies() {
    int numMuls = 5000;
    List<String> numbers = Lists.newArrayList("2", "3");
    Node current = new Node(Token.MUL, Node.newNumber(2), Node.newNumber(3));
    for (int i = 2; i < numMuls; i++) {
      current = new Node(Token.MUL, current);
      int num = (i % 100) + 2;
      numbers.add(String.valueOf(num));
      current.addChildToBack(Node.newNumber(num));
    }

    String expected = Joiner.on("*").join(numbers);
    String actual = printNode(current).replace("\n", "");
    assertEquals(expected, actual);
  }