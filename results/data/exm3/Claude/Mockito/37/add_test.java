// org/mockito/internal/stubbing/answers/AnswersValidatorTest.java
@Test
public void shouldValidateReturnsAnswer() throws Throwable {
    //given
    Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
    Returns answer = new Returns("some value");
    //when
    validator.validate(answer, invocation);
    //then - no exception should be thrown
}