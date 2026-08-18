// org/apache/commons/math3/ode/nonstiff/DormandPrince853IntegratorTest.java
@Test
public void testSimultaneousEvents() {
    FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
        public int getDimension() {
            return 1;
        }
        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = 1.0;
        }
    };

    final int[] count1 = new int[1];
    final int[] count2 = new int[1];

    EventHandler event1 = new EventHandler() {
        public void init(double t0, double[] y0, double t) {
            // do nothing
        }
        public Action eventOccurred(double t, double[] y, boolean increasing) {
            count1[0]++;
            return Action.CONTINUE;
        }
        public void resetState(double t, double[] y) {
            // do nothing
        }
        public double g(double t, double[] y) {
            return t - 1.0;
        }
    };

    EventHandler event2 = new EventHandler() {
        public void init(double t0, double[] y0, double t) {
            // do nothing
        }
        public Action eventOccurred(double t, double[] y, boolean increasing) {
            count2[0]++;
            return Action.CONTINUE;
        }
        public void resetState(double t, double[] y) {
            // do nothing
        }
        public double g(double t, double[] y) {
            return t - 1.0;
        }
    };

    FirstOrderIntegrator integ = new DormandPrince853Integrator(0.001, 1.0, 1.0e-12, 0.0);
    integ.addEventHandler(event1, 0.01, 1.0e-7, 100);
    integ.addEventHandler(event2, 0.01, 1.0e-7, 100);
    double t0 = 0.0;
    double[] y0 = new double[] { 0.0 };
    double t = 2.0;
    double[] y = new double[1];
    integ.integrate(ode, t0, y0, t, y);

    Assert.assertEquals("Event1 should be triggered once", 1, count1[0]);
    Assert.assertEquals("Event2 should be triggered once", 1, count2[0]);
}
