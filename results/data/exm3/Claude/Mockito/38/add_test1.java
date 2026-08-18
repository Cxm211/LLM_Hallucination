// org/mockito/internal/verification/argumentmatching/ArgumentMatchingToolTest.java
@Test
public void shouldHandleMultipleArgumentsWithNullInMiddle() {
    //when
    Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(
        (List) Arrays.asList(new Equals(10), new Equals(20), new Equals(30)),
        new Object[] {10, null, 30}
    );
    
    //then
    assertEquals(1, suspicious.length);
    assertEquals(1, suspicious[0].intValue());
}