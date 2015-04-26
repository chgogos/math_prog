package ui;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

public class TestCommonsMathDirect {

	public static void main(String[] args) {
		TestCommonsMathDirect app = new TestCommonsMathDirect();
		app.testLP();

	}

	void testLP() {
		SimplexSolver solver = new SimplexSolver();
		LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {
				3.0, 4.0 }, 0);
		Collection constraints = new ArrayList();
		constraints.add(new LinearConstraint(new double[] { 1, 2 },
				Relationship.LEQ, 14.0));
		constraints.add(new LinearConstraint(new double[] { 3, -1 },
				Relationship.GEQ, 0.0));
		constraints.add(new LinearConstraint(new double[] { 1, -1 },
				Relationship.LEQ, 2.0));

		PointValuePair solution = solver.optimize(f, new LinearConstraintSet(
				constraints), GoalType.MAXIMIZE);
		System.out.println("x   = " + solution.getPoint()[0]);
		System.out.println("y   = " + solution.getPoint()[1]);
		System.out.println("Obj.= " + solution.getValue());
	}
}
