package mathmodeler;

import gurobi.GRB;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.BasisStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mathmodeler.DecisionVariable;
import mathmodeler.LPConstraint;
import mathmodeler.MathAgent;

public class MathAgentGurobiCplex extends MathAgent {

	// ##################################################
	// GUROBI
	// ##################################################

	GRBEnv menv = null;
	GRBModel gurobi_solver = null;
	Map<DecisionVariable, GRBVar> dictGurobiVariables = new HashMap<>();
	Map<LPConstraint, GRBConstr> dictGurobiConstraints = new HashMap<>();

	public void prepareGurobi() throws GRBException {
		menv = new GRBEnv(null);

		if (timeLimit != Integer.MAX_VALUE)
			menv.set(GRB.DoubleParam.TimeLimit, timeLimit);

		if (enableOutput)
			menv.set(GRB.IntParam.OutputFlag, 1);
		else
			menv.set(GRB.IntParam.OutputFlag, 0);

		gurobi_solver = new GRBModel(menv);

		// variables
		for (String key : indexedDecisionVariablesMap.keySet()) {
			List<DecisionVariable> aList = indexedDecisionVariablesMap.get(key);
			for (DecisionVariable dv : aList) {
				addDecisionVariableToGurobiDictionary(dv);
			}
		}
		for (DecisionVariable dv : singleDecisionVariablesMap.values()) {
			addDecisionVariableToGurobiDictionary(dv);
		}
		gurobi_solver.update();

		// objective
		if (objectiveExpression.getSense().equalsIgnoreCase("Minimize"))
			gurobi_solver.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE);
		else
			gurobi_solver.set(GRB.IntAttr.ModelSense, GRB.MAXIMIZE);

		// constraints
		for (LPConstraint c : constraints) {
			double lhs = c.getLhs();
			double rhs = c.getRhs();
			GRBLinExpr gexpr = new GRBLinExpr();
			for (int i = 0; i < c.getExpr().getVariables().size(); i++) {
				DecisionVariable dv = c.getExpr().getVariables().get(i);
				double coef = c.getExpr().getCoefs().get(i);
				gexpr.addTerm(coef, dictGurobiVariables.get(dv));
			}
			GRBConstr constr;
			if (lhs == rhs)
				constr = gurobi_solver.addConstr(gexpr, GRB.EQUAL, rhs,
						c.getConstraintName());
			else if (rhs == Double.MAX_VALUE)
				constr = gurobi_solver.addConstr(gexpr, GRB.GREATER_EQUAL, lhs,
						c.getConstraintName());
			else
				constr = gurobi_solver.addConstr(gexpr, GRB.LESS_EQUAL, rhs,
						c.getConstraintName());
			dictGurobiConstraints.put(c, constr);
		}

		gurobi_solver.update();
	}

	private void addDecisionVariableToGurobiDictionary(DecisionVariable dv)
			throws GRBException {
		GRBVar var;
		double obj_coeff = 0.0;
		if (objectiveExpression.getExpr().getVariables().contains(dv)) {
			int i = objectiveExpression.getExpr().getVariables().indexOf(dv);
			obj_coeff = objectiveExpression.getExpr().getCoefs().get(i);
		}
		if (dv.getType().equalsIgnoreCase("BINARY"))
			var = gurobi_solver.addVar(dv.getLb(), dv.getUb(), obj_coeff,
					GRB.BINARY, dv.getName());
		else if (dv.getType().equalsIgnoreCase("INTEGER"))
			var = gurobi_solver.addVar(dv.getLb(), dv.getUb(), obj_coeff,
					GRB.INTEGER, dv.getName());
		else
			var = gurobi_solver.addVar(dv.getLb(), dv.getUb(), obj_coeff,
					GRB.CONTINUOUS, dv.getName());
		dictGurobiVariables.put(dv, var);
	}

	public GRBEnv getGurobiEnvironmentHook() {
		return menv;
	}

	public GRBModel getGurobiModelHook() {
		return gurobi_solver;
	}

	public void solveUsingGurobi() throws GRBException {
		// solve
		gurobi_solver.write("gurobi_model.lp");
		gurobi_solver.optimize();
		int rv = gurobi_solver.get(GRB.IntAttr.Status);
		if ((rv == GRB.INFEASIBLE) || (rv == GRB.UNBOUNDED)) {
			solverStatus = "ERROR";
			log.error("Solver terminated abnormally ERROR CODE: " + rv);
		} else if ((rv == GRB.SUBOPTIMAL) || (rv == GRB.TIME_LIMIT)) // ??
			solverStatus = "SUBOPTIMAL";
		else if (rv == GRB.OPTIMAL)
			solverStatus = "OPTIMAL";

		if (!solverStatus.equalsIgnoreCase("ERROR")) {
			objectiveValue = gurobi_solver.get(GRB.DoubleAttr.ObjVal);
			log.debug(String.format("GRB_solver value = %.4f", objectiveValue));
			for (String key : indexedDecisionVariablesMap.keySet()) {
				List<DecisionVariable> aList = indexedDecisionVariablesMap
						.get(key);
				for (DecisionVariable dv : aList) {
					GRBVar var = dictGurobiVariables.get(dv);
					dv.setValue(var.get(GRB.DoubleAttr.X));
				}
			}
			for (DecisionVariable dv : singleDecisionVariablesMap.values()) {
				GRBVar var = dictGurobiVariables.get(dv);
				dv.setValue(var.get(GRB.DoubleAttr.X));
			}
		}

	}

	public void computeDualsUsingGurobi() throws GRBException {
		duals = new double[constraints.size()];
		for (int i = 0; i < duals.length; i++) {
			LPConstraint lpc = constraints.get(i);
			duals[i] = dictGurobiConstraints.get(lpc).get(GRB.DoubleAttr.Pi);
		}
	}

	public void print_X_RC_ForBasisVariablesUsingGurobi() throws GRBException {
		for (DecisionVariable dv : indexedDecisionVariablesMap.get("y")) {
			double v = dictGurobiVariables.get(dv).get(GRB.DoubleAttr.X);
			double rc = dictGurobiVariables.get(dv).get(GRB.DoubleAttr.RC);
			if (v > 0.00001)
				System.out.printf(
						"Basis Variable %s value %.2f reduced cost %.8f\n",
						dv.getName(), v, rc);
			else
				System.out.printf(
						"Non Basis Variable %s value %.2f reduced cost %.8f\n",
						dv.getName(), v, rc);
		}
	}

	public void disposeUsingGurobi() throws GRBException {
		gurobi_solver.dispose();
		menv.dispose();
	}

	public String getBasisStatusForVariableUsingGurobi(DecisionVariable dv)
			throws GRBException {
		GRBVar var = dictGurobiVariables.get(dv);
		int v = var.get(GRB.IntAttr.VBasis);

		// The status of a given variable in the current basis.
		// Possible values are 0 (basic),
		// -1 (non-basic at lower bound),
		// -2 (non-basic at upper bound),
		// and -3 (super-basic).

		String s = "";
		if (v == 0) {
			s = "BASIC";
		} else if (v == -1) {
			s = "AT_LOWER_BOUND";
		} else if (v == -2) {
			s = "AT_UPPER_BOUND";
		} else if (v == -3) {
			s = "SUPER_BASIC";
		} else {
			throw new IllegalStateException(dv.toString() + " " + v);
		}
		dv.setReducedCost(var.get(GRB.DoubleAttr.RC));

		// log.debug(String.format("%s %s x:%.8f rc:%.8f",
		// var.get(GRB.StringAttr.VarName), s, var.get(GRB.DoubleAttr.X),
		// var.get(GRB.DoubleAttr.RC)));

		return s;
	}

	// ##################################################
	// END GUROBI
	// ##################################################

	// ##################################################
	// CPLEX
	// ##################################################
	IloCplex cplex_solver = null;
	Map<DecisionVariable, IloNumVar> dictCPLEXVariables = new HashMap<>();
	Map<LPConstraint, IloConstraint> dictCPLEXConstraints = new HashMap<>();

	public void prepareCPLEX() throws IloException {
		cplex_solver = new IloCplex();
		if (!enableOutput) {
			cplex_solver.setOut(null);
		}
		if (timeLimit != Integer.MAX_VALUE)
			cplex_solver.setParam(IloCplex.DoubleParam.TiLim, timeLimit);

		// variables
		for (String key : indexedDecisionVariablesMap.keySet()) {
			List<DecisionVariable> aList = indexedDecisionVariablesMap.get(key);
			for (DecisionVariable dv : aList) {
				addDecisionVariableToCPLEXDictionary(dv);
			}
		}
		for (DecisionVariable dv : singleDecisionVariablesMap.values()) {
			addDecisionVariableToCPLEXDictionary(dv);
		}

		// objective
		IloLinearNumExpr objective_expression = cplex_solver.linearNumExpr();
		for (int i = 0; i < objectiveExpression.getExpr().getVariables().size(); i++) {
			DecisionVariable dv = objectiveExpression.getExpr().getVariables()
					.get(i);
			double coef = objectiveExpression.getExpr().getCoefs().get(i);
			objective_expression.addTerm(coef, dictCPLEXVariables.get(dv));
		}
		if (objectiveExpression.getSense().equalsIgnoreCase("Minimize"))
			cplex_solver.addMinimize(objective_expression);
		else
			cplex_solver.addMaximize(objective_expression);

		// constraints
		for (LPConstraint c : constraints) {
			double lhs = c.getLhs();
			double rhs = c.getRhs();
			IloLinearNumExpr cexpr = cplex_solver.linearNumExpr();
			for (int i = 0; i < c.getExpr().getVariables().size(); i++) {
				DecisionVariable dv = c.getExpr().getVariables().get(i);
				double coef = c.getExpr().getCoefs().get(i);
				cexpr.addTerm(coef, dictCPLEXVariables.get(dv));
			}
			IloConstraint constr;
			if (lhs == rhs)
				constr = cplex_solver.addEq(cexpr, 1.0, c.getConstraintName());
			else if (rhs == Double.MAX_VALUE)
				constr = cplex_solver.addGe(cexpr, lhs, c.getConstraintName());
			else
				constr = cplex_solver.addLe(cexpr, rhs, c.getConstraintName());
			dictCPLEXConstraints.put(c, constr);
		}
	}

	private void addDecisionVariableToCPLEXDictionary(DecisionVariable dv)
			throws IloException {
		IloNumVar var;
		if (dv.getType().equalsIgnoreCase("BINARY"))
			var = cplex_solver.numVar(dv.getLb(), dv.getUb(),
					IloNumVarType.Bool, dv.getName());
		else if (dv.getType().equalsIgnoreCase("INTEGER"))
			var = cplex_solver.numVar(dv.getLb(), dv.getUb(),
					IloNumVarType.Int, dv.getName());
		else
			var = cplex_solver.numVar(dv.getLb(), dv.getUb(),
					IloNumVarType.Float, dv.getName());
		dictCPLEXVariables.put(dv, var);
	}

	public IloCplex getCPLEXHook() {
		return cplex_solver;
	}

	public void solveUsingCPLEX() throws IloException {
		cplex_solver.exportModel("cplex_model.lp");
		boolean rv = cplex_solver.solve();
		if (!rv) {
			solverStatus = "ERROR";
			log.error("Solver terminated abnormally ERROR CODE: " + rv);
		} else
			solverStatus = "OPTIMAL"; // ??

		if (!solverStatus.equalsIgnoreCase("ERROR")) {
			objectiveValue = cplex_solver.getObjValue();
			log.info("CPLEX_IPsolver value = " + objectiveValue);
			for (String key : indexedDecisionVariablesMap.keySet()) {
				List<DecisionVariable> aList = indexedDecisionVariablesMap
						.get(key);
				for (DecisionVariable dv : aList) {
					IloNumVar var = dictCPLEXVariables.get(dv);
					dv.setValue(cplex_solver.getValue(var));
				}
			}
			for (DecisionVariable dv : singleDecisionVariablesMap.values()) {
				IloNumVar var = dictCPLEXVariables.get(dv);
				dv.setValue(cplex_solver.getValue(var));
			}
		}
	}

	public void computeDualsUsingCplex() throws IloException {
		duals = new double[constraints.size()];
		for (int i = 0; i < duals.length; i++) {
			LPConstraint lpc = constraints.get(i);
			duals[i] = cplex_solver.getDual((IloRange) dictCPLEXConstraints
					.get(lpc));
		}
	}

	public void disposeUsingCPLEX() throws IloException {
		cplex_solver.clearModel();
		cplex_solver.end();
	}

	public String getBasisStatusForVariableUsingCPLEX(DecisionVariable dv)
			throws IloException {
		IloNumVar var = dictCPLEXVariables.get(dv);
		BasisStatus bs = cplex_solver.getBasisStatus(var);

		// The status of a given variable in the current basis.
		// BasisStatus.Basic
		// BasisStatus.AtLower
		// BasisStatus.AtUpper
		// BasisStatus.FreeOrSuperbasic
		// BasisStatus.NotABasicStatus

		String s = "";
		if (bs == BasisStatus.Basic) {
			s = "BASIC";
		} else if (bs == BasisStatus.AtLower) {
			s = "AT_LOWER_BOUND";
		} else if (bs == BasisStatus.AtUpper) {
			s = "AT_UPPER_BOUND";
		} else if (bs == BasisStatus.FreeOrSuperbasic) {
			s = "SUPER_BASIC";
		} else {
			throw new IllegalStateException(dv.toString() + " " + bs);
		}
		dv.setReducedCost(cplex_solver.getReducedCost(var));

		// log.debug(String.format("%s %s x:%.8f rc:%.8f",
		// var.get(GRB.StringAttr.VarName), s, var.get(GRB.DoubleAttr.X),
		// var.get(GRB.DoubleAttr.RC)));

		return s;
	}

	public void clearModel() {
		super.clearModel();
		// GUROBI
		menv = null;
		gurobi_solver = null;
		dictGurobiVariables = new HashMap<>();
		dictGurobiConstraints = new HashMap<>();

		// CPLEX
		cplex_solver = null;
		dictCPLEXVariables = new HashMap<>();
		dictCPLEXConstraints = new HashMap<>();
	}
}
