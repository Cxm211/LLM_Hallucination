// org/apache/commons/collections4/keyvalue/MultiKeyTest.java
@Test
public void testHashCodeAfterSerializationWithThreeKeys() throws IOException, ClassNotFoundException {
    final MultiKey<String> mk = new MultiKey<String>("A", "B", "C");
    final int originalHashCode = mk.hashCode();

    // serialize
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ObjectOutputStream out = new ObjectOutputStream(baos);
    out.writeObject(mk);
    out.close();

    // deserialize
    final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    final ObjectInputStream in = new ObjectInputStream(bais);
    final MultiKey<?> mk2 = (MultiKey<?>)in.readObject();
    in.close();

    assertEquals(originalHashCode, mk2.hashCode());
}