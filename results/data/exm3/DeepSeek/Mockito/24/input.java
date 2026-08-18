// buggy function
    public Object answer(InvocationOnMock invocation) {
        if (methodsGuru.isToString(invocation.getMethod())) {
            Object mock = invocation.getMock();
            MockName name = mockUtil.getMockName(mock);
            if (name.isDefault()) {
                return "Mock for " + mockUtil.getMockSettings(mock).getTypeToMock().getSimpleName() + ", hashCode: " + mock.hashCode();
            } else {
                return name.toString();
            }
        } else if (methodsGuru.isCompareToMethod(invocation.getMethod())) {
            //see issue 184.
            //mocks by default should return 0 if references are the same, otherwise some other value because they are not the same. Hence we return 1 (anything but 0 is good).
            //Only for compareTo() method by the Comparable interface
            return 1;
        }
        
        Class<?> returnType = invocation.getMethod().getReturnType();
        return returnValueFor(returnType);
    }

// trigger testcase
// org/mockito/internal/stubbing/defaultanswers/ReturnsEmptyValuesTest.java::should_return_zero_if_mock_is_compared_to_itself
@Test public void should_return_zero_if_mock_is_compared_to_itself() {
        //given
        Date d = mock(Date.class);
        d.compareTo(d);
        Invocation compareTo = this.getLastInvocation();

        //when
        Object result = values.answer(compareTo);

        //then
        assertEquals(0, result);
    }

// org/mockitousage/bugs/ShouldMocksCompareToBeConsistentWithEqualsTest.java::should_compare_to_be_consistent_with_equals_when_comparing_the_same_reference
@Test
    public void should_compare_to_be_consistent_with_equals_when_comparing_the_same_reference() {
        //given
        Date today    = mock(Date.class);

        //when
        Set<Date> set = new TreeSet<Date>();
        set.add(today);
        set.add(today);

        //then
        assertEquals(1, set.size());
    }
