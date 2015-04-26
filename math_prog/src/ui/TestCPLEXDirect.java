package ui;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

public class TestCPLEXDirect {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestCPLEXDirect app = new TestCPLEXDirect();
		try {
			app.testLP();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void testLP() throws IloException {
		IloCplex solver = new IloCplex();

		IloNumVar x = solver.numVar(0.0, Double.MAX_VALUE, IloNumVarType.Float,
				"x");
		IloNumVar y = solver.numVar(0.0, Double.MAX_VALUE, IloNumVarType.Float,
				"y");

		IloLinearNumExpr objective_expression = solver.linearNumExpr();
		objective_expression.addTerm(3.0, x);
		objective_expression.addTerm(4.0, y);
		solver.addMaximize(objective_expression);

		IloLinearNumExpr expr1 = solver.linearNumExpr();
		expr1.addTerm(1.0, x);
		expr1.addTerm(2.0, y);
		solver.addLe(expr1, 14.0, "c0");

		IloLinearNumExpr expr2 = solver.linearNumExpr();
		expr2.addTerm(3.0, x);
		expr2.addTerm(-1.0, y);
		solver.addGe(expr2, 0.0, "c1");

		IloLinearNumExpr expr3 = solver.linearNumExpr();
		expr3.addTerm(1.0, x);
		expr3.addTerm(-1.0, y);
		solver.addLe(expr3, 2.0, "c2");

		boolean flag = solver.solve();

		if (flag) {
			System.out.println("Solution status: " + solver.getStatus());
			System.out.println("x = " + solver.getValue(x));
			System.out.println("y = " + solver.getValue(y));
			System.out.println("Obj: " + solver.getObjValue());
		}
	}
}
