// buggy function
    public void verify(VerificationData data) {
        AssertionError error = null;

        timer.start();
        while (timer.isCounting()) {
            try {
                delegate.verify(data);

                if (returnOnSuccess) {
                    return;
                } else {
                    error = null;
                }
            } catch (MockitoAssertionError e) {
                error = handleVerifyException(e);
            }
            catch (org.mockito.exceptions.verification.junit.ArgumentsAreDifferent e) {
                error = handleVerifyException(e);
            }
        }

        if (error != null) {
            throw error;
        }
    }

// trigger testcase
// org/mockitointegration/NoJUnitDependenciesTest.java::pure_mockito_should_not_depend_JUnit
@Test
    public void pure_mockito_should_not_depend_JUnit() throws Exception {
        ClassLoader classLoader_without_JUnit = ClassLoaders.excludingClassLoader()
                .withCodeSourceUrlOf(
                        Mockito.class,
                        Matcher.class,
                        Enhancer.class,
                        Objenesis.class
                )
                .without("junit", "org.junit")
                .build();

        Set<String> pureMockitoAPIClasses = ClassLoaders.in(classLoader_without_JUnit).omit("runners", "junit", "JUnit").listOwnedClasses();

        for (String pureMockitoAPIClass : pureMockitoAPIClasses) {
            checkDependency(classLoader_without_JUnit, pureMockitoAPIClass);
        }
    }
