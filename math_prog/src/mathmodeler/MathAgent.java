package mathmodeler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPSolver.OptimizationProblemType;
import com.google.ortools.linearsolver.MPSolver.ResultStatus;
import com.google.ortools.linearsolver.MPSolverParameters;
import com.google.ortools.linearsolver.MPVariable;

public class MathAgent {
	// max 1000 characters per line
	private static final int MAX_CHARS_PER_LINE = 1000;

	private static String putNewLines(String s) {
		String eol = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		int c = 0;
		for (int i = 0; i < s.length(); i++) {
			c++;
			sb.append(s.charAt(i));
			if (s.charAt(i) == '\n')
				c = 0;
			if ((c >= MAX_CHARS_PER_LINE) && (s.charAt(i) == ' ')) {
				sb.append(eol);
				c = 0;
			}
		}
		return sb.toString();
	}

	protected static Logger log = LoggerFactory.getLogger(MathAgent.class);

	// ORTOOLS
	MPSolver ortools_solver;
	Map<DecisionVariable, MPVariable> dictORT = new HashMap<>();
	Map<LPConstraint, MPConstraint> dictORTConstraints = new HashMap<>();

	// common
	protected String modelName = "dummy";
	protected int timeLimit = Integer.MAX_VALUE;
	protected boolean enableOutput = false;
	protected String solverStatus = null;
	protected double objectiveValue;
	protected Map<String, List<DecisionVariable>> indexedDecisionVariablesMap = new HashMap<>();
	protected Map<String, DecisionVariable> singleDecisionVariablesMap = new HashMap<>();
	protected List<LPConstraint> constraints = new ArrayList<>();
	protected Objective objectiveExpression;
	protected double[] duals;

	public List<LPConstraint> getConstraints() {
		return constraints;
	}

	public double[] getDuals() {
		return duals;
	}

	public void addIndexedDecisionVariables(String dv_key,
			List<DecisionVariable> listOfDecisionVariables) {
		indexedDecisionVariablesMap.put(dv_key, listOfDecisionVariables);
	}

	public DecisionVariable getIndexedDecisionVariable(String dv_key,
			String indices) {
		for (DecisionVariable dv : indexedDecisionVariablesMap.get(dv_key)) {
			if (dv.indices.equalsIgnoreCase(indices))
				return dv;
		}
		throw new IllegalStateException();
	}

	public DecisionVariable getIndexedDecisionVariable(String dv_key, int index1) {
		String indices = String.format("%d", index1);
		for (DecisionVariable dv : indexedDecisionVariablesMap.get(dv_key)) {
			if (dv.indices.equalsIgnoreCase(indices))
				return dv;
		}
		throw new IllegalStateException();
	}

	public DecisionVariable getIndexedDecisionVariable(String dv_key,
			int index1, int index2) {
		String indices = String.format("%d_%d", index1, index2);
		for (DecisionVariable dv : indexedDecisionVariablesMap.get(dv_key)) {
			if (dv.indices.equalsIgnoreCase(indices))
				return dv;
		}
		throw new IllegalStateException();
	}

	public DecisionVariable getIndexedDecisionVariable(String dv_key,
			int index1, int index2, int index3) {
		String indices = String.format("%d_%d_%d", index1, index2, index3);
		for (DecisionVariable dv : indexedDecisionVariablesMap.get(dv_key)) {
			if (dv.indices.equalsIgnoreCase(indices))
				return dv;
		}
		throw new IllegalStateException();
	}

	public void addConstraint(LPConstraint constr) {
		constraints.add(constr);
	}

	public void setObjective(Objective objective) {
		this.objectiveExpression = objective;
	}

	private List<String> getBinaryVariables() {
		List<String> bList = new ArrayList<>();
		for (String key : indexedDecisionVariablesMap.keySet()) {
			List<DecisionVariable> aList = indexedDecisionVariablesMap.get(key);
			for (DecisionVariable dv : aList) {
				if (dv.type.equalsIgnoreCase("BINARY")) {
					bList.add(dv.name);
				}
			}
		}
		for (DecisionVariable dv : singleDecisionVariablesMap.values()) {
			if (dv.type.equalsIgnoreCase("BINARY")) {
				bList.add(dv.name);
			}
		}
		return bList;
	}

	private List<String> getIntegerVariables() {
		List<String> iList = new ArrayList<>();
		for (String key : indexedDecisionVariablesMap.keySet()) {
			List<DecisionVariable> aList = indexedDecisionVariablesMap.get(key);
			for (DecisionVariable dv : aList) {
				if (dv.type.equalsIgnoreCase("INTEGER")) {
					iList.add(dv.name);
				}
			}
		}
		for (DecisionVariable dv : singleDecisionVariablesMap.values()) {
			if (dv.type.equalsIgnoreCase("INTEGER")) {
				iList.add(dv.name);
			}
		}
		return iList;
	}

	public void setModelName(String mn) {
		this.modelName = mn;

	}

	public void setEnableOutput(boolean b) {
		this.enableOutput = b;

	}

	public void setTimeLimit(int tl) {
		this.timeLimit = tl;

	}

	@Deprecated
	public void prepareORTools() {
		prepareORTools(OptimizationProblemType.GLPK_MIXED_INTEGER_PROGRAMMING);
	}

	public void prepareORTools(OptimizationProblemType solverType) {
		ortools_solver = new MPSolver(modelName, solverType);
		log.info(ortools_solver.solverVersion());
		if (enableOutput)
			ortools_solver.enableOutput();
		if (timeLimit != Integer.MAX_VALUE)
			ortools_solver.setTimeLimit(1000 * timeLimit);

		// variables
		for (String key : indexedDecisionVariablesMap.keySet()) {
			List<DecisionVariable> aList = indexedDecisionVariablesMap.get(key);
			for (DecisionVariable dv : aList) {
				addDecisionVariableToORToolsDictionary(dv);
			}
		}
		for (DecisionVariable dv : singleDecisionVariablesMap.values()) {
			addDecisionVariableToORToolsDictionary(dv);
		}

		// objective
		if (objectiveExpression.sense.equalsIgnoreCase("Minimize"))
			ortools_solver.objective().setMinimization();
		else
			ortools_solver.objective().setMaximization();
		for (int i = 0; i < objectiveExpression.expr.variables.size(); i++) {
			DecisionVariable dv = objectiveExpression.expr.variables.get(i);
			double coef = objectiveExpression.expr.coefs.get(i);
			ortools_solver.objective().setCoefficient(dictORT.get(dv), coef);
		}

		// constraints
		for (LPConstraint c : constraints) {
			double lhs = c.lhs;
			double rhs = c.rhs;
			if (c.lhs == -Double.MAX_VALUE)
				lhs = -MPSolver.infinity();
			if (c.rhs == Double.MAX_VALUE)
				rhs = MPSolver.infinity();
			MPConstraint constr = ortools_solver.makeConstraint(lhs, rhs,
					c.constraintName);
			for (int i = 0; i < c.expr.variables.size(); i++) {
				DecisionVariable dv = c.expr.variables.get(i);
				double coef = c.expr.coefs.get(i);
				constr.setCoefficient(dictORT.get(dv), coef);
			}
			dictORTConstraints.put(c, constr);
		}

	}

	private void addDecisionVariableToORToolsDictionary(DecisionVariable dv) {
		MPVariable var;
		if (dv.type.equalsIgnoreCase("BINARY")) {
			var = ortools_solver.makeBoolVar(dv.name);
		} else if (dv.type.equalsIgnoreCase("INTEGER")) {
			var = ortools_solver.makeIntVar(dv.lb, dv.ub, dv.name);
		} else
			// numvar
			var = ortools_solver.makeNumVar(dv.lb, dv.ub, dv.name);
		dictORT.put(dv, var);
	}

	/**
	 * examples of passing parameters to ORTools // MPSolverParameters
	 * solver_param = new MPSolverParameters(); //
	 * solver_param.setIntegerParam(MPSolverParameters.PRESOLVE, //
	 * MPSolverParameters.PRESOLVE_ON); //
	 * solver_param.setIntegerParam(MPSolverParameters.INCREMENTALITY, //
	 * MPSolverParameters.INCREMENTALITY_OFF); //
	 * solver_param.setIntegerParam(MPSolverParameters.SCALING, //
	 * MPSolverParameters.SCALING_ON); //
	 * solver_param.setDoubleParam(MPSolverParameters.RELATIVE_MIP_GAP, //
	 * 0.01);
	 **/

	public boolean solveUsingORTools(MPSolverParameters solver_parameters) {
		ResultStatus rv = ortools_solver.solve(solver_parameters);
		return solveUsingORToolsBase(rv);
	}

	public boolean solveUsingORTools() {
		ResultStatus rv = ortools_solver.solve();
		return solveUsingORToolsBase(rv);
	}

	private boolean solveUsingORToolsBase(ResultStatus rv) {
		if ((rv == MPSolver.ResultStatus.INFEASIBLE) || (rv == MPSolver.ResultStatus.UNBOUNDED)
				|| (rv == MPSolver.ResultStatus.NOT_SOLVED) || (rv == MPSolver.ResultStatus.ABNORMAL)) {
			solverStatus = "ERROR";
			log.error("Solver terminated abnormally ERROR CODE: " + rv);
		} else if (rv == MPSolver.ResultStatus.FEASIBLE)
			solverStatus = "SUBOPTIMAL";
		else if (rv == MPSolver.ResultStatus.OPTIMAL)
			solverStatus = "OPTIMAL";

		if (solverStatus.equalsIgnoreCase("ERROR")) {
			String rv_s = "";
			if (rv == MPSolver.ResultStatus.INFEASIBLE)
				rv_s = "MPSolver.INFEASIBLE";
			else if (rv == MPSolver.ResultStatus.UNBOUNDED)
				rv_s = "MPSolver.UNBOUNDED";
			else if (rv == MPSolver.ResultStatus.NOT_SOLVED)
				rv_s = "MPSolver.NOT_SOLVED";
			else if (rv == MPSolver.ResultStatus.ABNORMAL)
				rv_s = "MPSolver.ABNORMAL";
			else
				rv_s = "UNKNOWN";
			log.error("Problem was not solved. Return status: " + rv_s);
			// return false;
			throw new IllegalStateException(
					"Problem was not solved. Return status: " + rv_s);
		} else {
			objectiveValue = ortools_solver.objective().value();
			log.info(String.format("ORT_Solver value = %.4f", objectiveValue));
			for (String key : indexedDecisionVariablesMap.keySet()) {
				List<DecisionVariable> aList = indexedDecisionVariablesMap
						.get(key);
				for (DecisionVariable dv : aList) {
					MPVariable var = dictORT.get(dv);
					dv.setValue(var.solutionValue());
				}
			}
			for (DecisionVariable dv : singleDecisionVariablesMap.values()) {
				MPVariable var = dictORT.get(dv);
				dv.setValue(var.solutionValue());
			}
			return true;
		}
	}

	public void computeDualsUsingORTools() {
		duals = new double[constraints.size()];
		for (int i = 0; i < duals.length; i++) {
			LPConstraint lpc = constraints.get(i);
			duals[i] = dictORTConstraints.get(lpc).dualValue();
		}
	}

	public void disposeUsingORTools() {
		ortools_solver.clear();
		ortools_solver.delete();
	}

	public MPSolver getORToolsHook() {
		return ortools_solver;
	}

	public String getBasisStatusForVariableUsingORTools(DecisionVariable dv) {
		MPVariable var = dictORT.get(dv);
		// The status of a given variable in the current basis.
		// FREE = 0
		// AT_LOWER_BOUND
		// AT_UPPER_BOUND
		// FIXED_VALUE
		// BASIC

		String s = "";
		if (var.basisStatus() == MPSolver.BasisStatus.FREE)
			s = "FREE";
		else if (var.basisStatus() == MPSolver.BasisStatus.AT_LOWER_BOUND)
			s = "AT_LOWER_BOUND";
		else if (var.basisStatus() == MPSolver.BasisStatus.AT_UPPER_BOUND) {
			s = "AT_UPPER_BOUND";
		} else if (var.basisStatus() == MPSolver.BasisStatus.FIXED_VALUE)
			s = "FIXED_VALUE";
		else if (var.basisStatus() == MPSolver.BasisStatus.BASIC)
			s = "BASIC";
		else
			throw new IllegalStateException(dv.toString() + " "
					+ var.basisStatus());
		dv.setReducedCost(var.reducedCost());
		// log.debug(String.format("%s %s x:%.8f rc:%.8f", var.name(), s,
		// var.solutionValue(), var.reducedCost()));
		return s;

		// if ((var.basisStatus() == MPSolver.AT_LOWER_BOUND)
		// && (var.solutionValue() < 0.0001)) {
		// // log.debug(String.format("%s NON BASIC [%s-%d] %.8f", var.name(),
		// // s, var.basisStatus(), var.solutionValue()));
		// return "NON_BASIC";
		// } else {
		// // log.debug(String.format("%s BASIC [%s-%d] %.8f", var.name(),
		// // s, var.basisStatus(), var.solutionValue()));
		// return "BASIC";
		// }

	}

	public void addSingleDecisionVariable(String dv_id, DecisionVariable dv) {
		singleDecisionVariablesMap.put(dv_id, dv);
	}

	public DecisionVariable getSingleDecisionVariable(String dv_id) {
		return singleDecisionVariablesMap.get(dv_id);
	}

	private String generateLP() {
		StringBuilder sb = new StringBuilder();
		sb.append(objectiveExpression);
		sb.append(System.getProperty("line.separator"));
		sb.append("Subject To");
		sb.append(System.getProperty("line.separator"));
		for (LPConstraint constraint : constraints) {
			sb.append(constraint);
			sb.append(System.getProperty("line.separator"));
		}
		List<String> binaries = getBinaryVariables();
		if (!binaries.isEmpty()) {
			sb.append("Binaries");
			sb.append(System.getProperty("line.separator"));
			for (String var : binaries) {
				sb.append(var + " ");
			}
		}
		sb.append(System.getProperty("line.separator"));
		sb.append("End");
		return sb.toString();
	}

	public void printProblemLPFormat() {
		String sb = generateLP();
		System.out.println(putNewLines(sb.toString()));
	}

	public void saveProblemLPFormat(String filename) {
		String s = generateLP();
		FileWriter fw = null;
		try {
			fw = new FileWriter(filename);
			fw.write(s);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fw != null)
					fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public double getObjectiveValue() {
		return objectiveValue;
	}

	public List<DecisionVariable> getDecisionVariables(String name) {
		return indexedDecisionVariablesMap.get(name);
	}

	public int getNumberOfDecisionVariables() {
		int vars = 0;
		for (String key : indexedDecisionVariablesMap.keySet()) {
			vars += indexedDecisionVariablesMap.get(key).size();
		}
		return vars + singleDecisionVariablesMap.size();
	}

	public void printProblemStatistics() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("variables = %d constraints= %d",
				getNumberOfDecisionVariables(), constraints.size()));
		System.out.println(sb.toString());
	}

	protected void clearModel() {
		solverStatus = null;
		objectiveValue = Double.MAX_VALUE;
		indexedDecisionVariablesMap = new HashMap<>();
		singleDecisionVariablesMap = new HashMap<>();
		constraints = new ArrayList<>();
		duals = null;
		objectiveExpression = null;
		// ORTOOLS
		ortools_solver = null;
		dictORT = new HashMap<>();
		dictORTConstraints = new HashMap<>();
	}

}
