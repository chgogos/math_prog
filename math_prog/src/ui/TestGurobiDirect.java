package ui;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class TestGurobiDirect {

	public static void main(String[] args) {
		TestGurobiDirect app = new TestGurobiDirect();
		try {
			app.testLP();
		} catch (GRBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void testLP() throws GRBException {
		GRBEnv env = new GRBEnv();
		GRBModel model = new GRBModel(env);

		GRBVar x = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "x");
		GRBVar y = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "y");

		GRBLinExpr objexpr = new GRBLinExpr();
		
		objexpr.addTerm(3.0, x);
		objexpr.addTerm(4.0, y);
		model.update();
		model.setObjective(objexpr, GRB.MAXIMIZE);
		
		GRBLinExpr expr1 = new GRBLinExpr();
		expr1.addTerm(1.0, x);
		expr1.addTerm(2.0, y);
		model.addConstr(expr1, GRB.LESS_EQUAL, 14.0, "c0");

		GRBLinExpr expr2 = new GRBLinExpr();
		expr2.addTerm(3.0, x);
		expr2.addTerm(-1.0, y);
		model.addConstr(expr2, GRB.GREATER_EQUAL, 0.0, "c1");

		GRBLinExpr expr3 = new GRBLinExpr();
		expr3.addTerm(1.0, x);
		expr3.addTerm(-1.0, y);
		model.addConstr(expr3, GRB.LESS_EQUAL, 2.0, "c2");

		model.optimize();
		
		System.out.println(x.get(GRB.StringAttr.VarName) + " "
				+ x.get(GRB.DoubleAttr.X));
		System.out.println(y.get(GRB.StringAttr.VarName) + " "
				+ y.get(GRB.DoubleAttr.X));
		System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
	}
}
