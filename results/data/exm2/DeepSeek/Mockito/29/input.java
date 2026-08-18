    public void describeTo(Description description) {
        description.appendText("same(");
        appendQuoting(description);
        description.appendText(wanted.toString());
        appendQuoting(description);
        description.appendText(")");
    }

// trigger testcase
public void shouldNotThrowNPEWhenNullPassedToSame() {
        mock.objectArgMethod("not null");

        verify(mock).objectArgMethod(same(null));
    }
