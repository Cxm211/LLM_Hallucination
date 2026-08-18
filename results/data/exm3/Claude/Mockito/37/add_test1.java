// org/mockito/internal/stubbing/answers/AnswersValidatorTest.java
@Test
public void shouldValidateThrowsExceptionAnswer() throws Throwable {
    //given
    Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
    ThrowsException answer = new ThrowsException(new RuntimeException());
    //when
    validator.validate(answer, invocation);
    //then - no exception should be thrown
}