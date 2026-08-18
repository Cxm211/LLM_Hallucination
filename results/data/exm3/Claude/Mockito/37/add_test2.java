// org/mockito/internal/stubbing/answers/AnswersValidatorTest.java
@Test
public void shouldValidateDoesNothingAnswer() throws Throwable {
    //given
    Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
    DoesNothing answer = new DoesNothing();
    //when
    validator.validate(answer, invocation);
    //then - no exception should be thrown
}