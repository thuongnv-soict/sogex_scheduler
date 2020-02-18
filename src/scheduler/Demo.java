package scheduler;

import localsearch.constraints.basic.IsEqual;
import localsearch.constraints.basic.LessOrEqual;
import localsearch.functions.occurrence.Occurrence;
import localsearch.model.ConstraintSystem;
import localsearch.model.IFunction;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;
import localsearch.search.TabuSearch;


public class Demo {
	
	public void test() {
		int accounts = 20;
		int spaces = 96;
		int concurence = 5;
		LocalSearchManager ls = new LocalSearchManager();
		ConstraintSystem S = new ConstraintSystem(ls);
		
		VarIntLS[][] x = new VarIntLS[spaces][accounts];
		int[] con  = new int[spaces];
		for (int i=0; i<spaces; i++) {
			con[i] = 0;
		}
		
		
		
		for (int i = 0; i < spaces; i++) {
			for (int j = 0; j < accounts; j++) {
				x[i][j] = new VarIntLS(ls, 0, 1);
			}
		}
//		for (int i = 0; i < spaces; i++) {
//			System.out.print(i + " :");
//			for (int j = 0; j<accounts; j++) {
//				System.out.print(x[i][j].getValue() + "  ");
//			}
//			System.out.println();
//		}
//		System.out.println();
//		S.post(new AllDifferent(x));
		
//		VarIntLS[] varNumberOfPeriods = new VarIntLS[n];
//		for (int i = 0; i < n; i++) {
//			varNumberOfPeriods[i] = new VarIntLS(ls, 0, 0);
//		}
		
		
//		IFunction[] f1 = new IFunction[n];
//		for (int i = 0; i < n; i++) {
//			List<Integer> distinctValues = new ArrayList<Integer>();
//			int count = 0;
//			int flag = 0;
//			for (int j = 0; j < spaces; j++) {
//				if (x[i][j].getValue() == 1) {
//					int period = j / 12;
//					flag = 0;
//					for (Integer value : distinctValues) {
//						if (value.intValue() == period) {
//							flag = 1;
//							break;
//						}
//					}
//					if (flag == 1) {
//						distinctValues.add(period);
//						count++;
//					}
//				}
//			}
//			f1[i] = new FuncPlus(varNumberOfPeriods[i], count);
//			S.post(new LessOrEqual(f1[i], 4));
//		}
//			
		
		/*
		 * Concurrence
		 */
		IFunction[] f2 = new IFunction[spaces];
		
		for (int i=0; i < spaces; i++) {
			f2[i] = new Occurrence(x[i], 1);
			S.post(new IsEqual(f2[i], concurence));
		}
		
		IFunction[] f3 = new IFunction[spaces];
//		for (int i = 0; i < accounts; i++) {
//			f3[i] = new FuncPlus(x[0][i], 0);
//			for (int j = 0; j < 8; j++) {
//				for (int k = 0; k < 12; k++) {
//					f3[i] = new FuncPlus(f3[i], x[j*8+k][i]);
//					
//					System.out.println(f3[i].getValue());
//				}
//				
//			}
//			System.out.println(f3[i].getValue());
//			S.post(new LessOrEqual(f3[i], 20));
//		}
		VarIntLS[] newArr = new VarIntLS[spaces];
		for (int i = 0; i < accounts; i++) {
			for (int j=0; j < spaces; j++) {
				newArr[j] = x[j][i];
			}
			f3[i] = new Occurrence(newArr, 1);
			S.post(new LessOrEqual(f3[i], 28));
		}
		
		
		// AllDifferentFunctions c1=new AllDifferentFunctions(f1);
		// S.post(new AllDifferentFunctions(f1));
//		S.post(new AllDifferent(f1));
		ls.close();
		System.out.println("Init S = " + S.violations());
		TabuSearch ts = new TabuSearch();
		ts.search(S, spaces * accounts, 100000, 10000, 5000);
		System.out.println(S.violations() + "\n");
		
		
		int d = 0; 
		for (int i = 0; i < spaces; i++) {
			System.out.print(i + " :");
			int count = 0;
			for (int j = 0; j<accounts; j++) {
				System.out.print(x[i][j].getValue() + "  ");
				if (x[i][j].getValue() == 1) {
					count++;
				}
			}
			if (count == concurence) {
				System.out.print("yeah");
				d++;
			}
			System.out.println();
		}
		System.out.println(d);
		System.out.println("hahahahah");
		for (int i = 0; i < accounts; i++) {
			int count = 0;
			for (int j = 0; j<spaces; j++) {
				if (x[j][i].getValue() == 1)
					count++;
			}
			System.out.println(count);
		}
	}
	public static void main(String[] args) {
		Demo sch = new Demo();
		sch.test();
	}
}
