public LevenbergMarquardtOptimizer() {

        setMaxIterations(1000);
        setMaxEvaluations(1000);

        setInitialStepBoundFactor(100.0);
        setCostRelativeTolerance(1.0e-10);
        setParRelativeTolerance(1.0e-10);
        setOrthoTolerance(1.0e-10);

    }