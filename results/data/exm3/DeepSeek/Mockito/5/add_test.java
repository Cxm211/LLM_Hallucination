// org/mockitointegration/NoJUnitDependenciesTest.java
@Test
    public void verificationOverTimeImpl_should_not_depend_on_JUnit() throws Exception {
        ClassLoader cl = ClassLoaders.excludingClassLoader()
                .withCodeSourceUrlOf(
                        Mockito.class,
                        Matcher.class,
                        Enhancer.class,
                        Objenesis.class
                )
                .without("junit", "org.junit")
                .build();
        checkDependency(cl, "org.mockito.verification.VerificationOverTimeImpl");
    }
