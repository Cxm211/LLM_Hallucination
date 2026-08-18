// org/apache/commons/math/ode/events/EventStateTest.java
@Test
    public void eventNearStepStart()
        throws EventException, ConvergenceException, DerivativeException {

        final double eventTime = 10.0;
        EventHandler handler = new EventHandler() {
            public void resetState(double t, double[] y) {
            }
            public double g(double t, double[] y) {
                return t - eventTime;
            }
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return CONTINUE;
            }
        };

        final double tolerance = 0.01;
        EventState es = new EventState(handler, 5.0, tolerance, 10);

        double t0 = 9.95;
        es.reinitializeBegin(t0, new double[0]);
        AbstractStepInterpolator interpolator =
            new DummyStepInterpolator(new double[0], true);
        interpolator.storeTime(t0);

        interpolator.shift();
        interpolator.storeTime(15.0);
        Assert.assertTrue(es.evaluateStep(interpolator));
        Assert.assertEquals(eventTime, es.getEventTime(), tolerance);

    }