// org/apache/commons/codec/language/SoundexTest.java
@Test
public void testHWRuleEx2() {
    Assert.assertEquals("B620", this.getStringEncoder().encode("Bihara"));
    Assert.assertEquals("B620", this.getStringEncoder().encode("Bwhara"));
    Assert.assertEquals("K620", this.getStringEncoder().encode("Kihara"));
}