// com/google/javascript/jscomp/CodePrinterTest.java
public void testManyAddsRightAssoc() {
    int numAdds = 10000;
    // Build a right‑associative chain: a+(b+(c+(...)))
    Node current = Node.newNumber(numAdds - 1);
    for (int i = numAdds - 2; i >= 0; i--) {
      current = new Node(Token.ADD, Node.newNumber(i), current);
    }
    List<String> numbers = Lists.newArrayList();
    for (int i = 0; i < numAdds; i++) {
      numbers.add(String.valueOf(i));
    }
    String expected = Joiner.on("+").join(numbers);
    String actual = printNode(current).replace("\n", "");
    assertEquals(expected, actual);
  }
