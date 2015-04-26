package ui;
import org.apache.commons.lang3.StringUtils;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPSolver.ResultStatus;
import com.google.ortools.linearsolver.MPVariable;

public class TestSolversThroughORTools {

	/**
	 * 
	 * http://www.math.clemson.edu/~mjs/courses/mthsc.440/integer
	 * 
	 * Suppose we wish to invest $14,000. We have identified four investment
	 * opportunities. Investment 1 requires an investment of $5,000 and has a
	 * present value (a time-discounted value) of $8,000; investment 2 requires
	 * $7,000 and has a value of $11,000; investment 3 requires $4,000 and has a
	 * value of $6,000; and investment 4 requires $3,000 and has a value of
	 * $4,000. Into which investments should we place our money so as to
	 * maximize our total present value?
	 * 
	 * Maximize 8x0 + 11x1 + 6x2 + 4x3
	 * 
	 * subject to 5x0 + 7x1 + 4x2 + 3x3 <= 14
	 * 
	 * x0, x1, x2, x3 binary
	 */

	public static void main(String[] args) {
		String os_name = System.getProperty("os.name").toLowerCase();
		System.out.println(os_name);
		System.loadLibrary("jniortools");
		TestSolversThroughORTools app = new TestSolversThroughORTools();
		System.out.println("Linear Programming Solvers");
		app.testLinearSolvers();
		System.out.println("Integer Programming Solvers");
		app.testIntegerProgrammingSolvers();
	}

	void testLinearSolvers() {
		System.out.println(StringUtils.repeat('*', 40));
		testGLOP();
		System.out.println(StringUtils.repeat('*', 40));
		testCLP();
		System.out.println(StringUtils.repeat('*', 40));
		testGLPK_LINEAR();
		System.out.println(StringUtils.repeat('*', 40));
	}

	void testIntegerProgrammingSolvers() {
		System.out.println(StringUtils.repeat('*', 40));
		testCBC();
		System.out.println(StringUtils.repeat('*', 40));
		testGLPK_INTEGER();
		System.out.println(StringUtils.repeat('*', 40));

	}

	void testCLP() {
		MPSolver ortools_solver = new MPSolver("CLP_TEST",
				MPSolver.OptimizationProblemType.CLP_LINEAR_PROGRAMMING);
		formulateLP(ortools_solver);

	}

	void testGLPK_LINEAR() {
		MPSolver ortools_solver = new MPSolver("GLPK_LINEAR_TEST",
				MPSolver.OptimizationProblemType.GLPK_LINEAR_PROGRAMMING);
		formulateLP(ortools_solver);

	}

	void testGLOP() {
		MPSolver ortools_solver = new MPSolver("GLOP_TEST",
				MPSolver.OptimizationProblemType.GLOP_LINEAR_PROGRAMMING);
		formulateLP(ortools_solver);
	}

	void testSCIP() {
		// MPSolver ortools_solver = new MPSolver("SCIP_IP_TEST",
		// MPSolver.OptimizationProblemType.SCIP_MIXED_INTEGER_PROGRAMMING);
		// formulateIP(ortools_solver);
	}

	void testCBC() {
		MPSolver ortools_solver = new MPSolver("CBC_TEST",
				MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);
		formulateIP(ortools_solver);

	}

	void testGLPK_INTEGER() {
		MPSolver ortools_solver = new MPSolver("GLPK_INTEGER_TEST",
				MPSolver.OptimizationProblemType.GLPK_MIXED_INTEGER_PROGRAMMING);
		formulateIP(ortools_solver);

	}

	void formulateLP(MPSolver solver) {
		solver.objective().setMaximization();
		MPVariable x = solver.makeNumVar(0.0, MPSolver.infinity(), "x");
		MPVariable y = solver.makeNumVar(0.0, MPSolver.infinity(), "y");
		solver.objective().setCoefficient(x, 3);
		solver.objective().setCoefficient(y, 4);
		MPConstraint c0 = solver.makeConstraint(-MPSolver.infinity(), 14.0,
				"c0");
		c0.setCoefficient(x, 1.0);
		c0.setCoefficient(y, 2.0);
		MPConstraint c1 = solver.makeConstraint(0.0, MPSolver.infinity(), "c1");
		c1.setCoefficient(x, 3.0);
		c1.setCoefficient(y, -1.0);
		MPConstraint c2 = solver
				.makeConstraint(-MPSolver.infinity(), 2.0, "c2");
		c2.setCoefficient(x, 1.0);
		c2.setCoefficient(y, -1.0);
		ResultStatus resultStatus = solver.solve();
		if (resultStatus == ResultStatus.OPTIMAL) {
			// System.out.println(solver.exportModelAsLpFormat(false));
			System.out.println("Solver version: " + solver.solverVersion());
			System.out.println("Problem solved in " + solver.wallTime()
					+ " milliseconds");
			System.out.println("Objective value = "
					+ solver.objective().value());
			System.out.println("Objective offset = "
					+ solver.objective().offset());
			System.out.println("x = " + x.solutionValue());
			System.out.println("y = " + y.solutionValue());
			System.out.println("Problem solved in " + solver.iterations()
					+ " iterations");
			System.out.println("x: reduced cost = " + x.reducedCost());
			System.out.println("x: reduced cost = " + y.reducedCost());
			System.out.printf("c0: dual value = %.6f activity = %.6f\n",
					c0.dualValue(), c0.activity());
			System.out.printf("c1: dual value = %.6f activity = %.6f\n",
					c1.dualValue(), c1.activity());
			System.out.printf("c2: dual value = %.6f activity = %.6f\n",
					c2.dualValue(), c2.activity());
		}
	}

	void formulateIP(MPSolver solver) {
		solver.objective().setMaximization();
		MPVariable x0 = solver.makeIntVar(0.0, MPSolver.infinity(), "x0");
		MPVariable x1 = solver.makeIntVar(0.0, MPSolver.infinity(), "x1");
		MPVariable x2 = solver.makeIntVar(0.0, MPSolver.infinity(), "x2");
		MPVariable x3 = solver.makeIntVar(0.0, MPSolver.infinity(), "x3");
		solver.objective().setCoefficient(x0, 8);
		solver.objective().setCoefficient(x1, 11);
		solver.objective().setCoefficient(x2, 6);
		solver.objective().setCoefficient(x3, 3);
		MPConstraint c0 = solver.makeConstraint(-MPSolver.infinity(), 14.0,
				"c0");
		c0.setCoefficient(x0, 5.0);
		c0.setCoefficient(x1, 7.0);
		c0.setCoefficient(x2, 4.0);
		c0.setCoefficient(x3, 3.0);
		ResultStatus resultStatus = solver.solve();
		if (resultStatus == ResultStatus.OPTIMAL) {
			// System.out.println(solver.exportModelAsLpFormat(false));
			System.out.println("Solver version: " + solver.solverVersion());
			System.out.println("Problem solved in " + solver.wallTime()
					+ " milliseconds");
			System.out.println("Objective value = "
					+ solver.objective().value());
			System.out.println("Objective offset = "
					+ solver.objective().offset());
			System.out.println("Objective best bound = "
					+ solver.objective().bestBound());
			System.out.println("x0 = " + x1.solutionValue());
			System.out.println("x1 = " + x1.solutionValue());
			System.out.println("x2 = " + x2.solutionValue());
			System.out.println("x3 = " + x3.solutionValue());
		}
	}

}
