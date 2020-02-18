package scheduler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import localsearch.constraints.alldifferent.AllDifferent;
import localsearch.functions.basic.FuncMinus;
import localsearch.functions.max_min.Max;
import localsearch.functions.max_min.Min;
import localsearch.functions.occurrence.Occurrence;
import localsearch.model.ConstraintSystem;
import localsearch.model.IFunction;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;
import localsearch.search.TabuSearch;

public class AvoidPeriods {
	
	/*
	 * Find the best result for avoided periods for all accounts in cluster 
	 */
	public int[][] findAvoidedPeriods(int numberOfAccounts, int period, int numberOfAvoidedPeriods) {
		LocalSearchManager ls = new LocalSearchManager();
		ConstraintSystem S = new ConstraintSystem(ls);

		int numberOfPeriods = 24 / period;
		System.out.println(numberOfAccounts + " " + numberOfPeriods + " " + numberOfAvoidedPeriods);
		VarIntLS[][] x = new VarIntLS[numberOfAccounts][numberOfAvoidedPeriods];
		for (int i = 0; i < numberOfAccounts; i++) {
			for (int j = 0; j < numberOfAvoidedPeriods; j++) {
				x[i][j] = new VarIntLS(ls, 0, numberOfPeriods - 1);
			}
		}

		/*
		 * Make sure all periods of each account are different
		 */
		for (int i = 0; i < numberOfAccounts; i++) {
			S.post(new AllDifferent(x[i]));
		}

		/*
		 * Balanced number of periods on all numberOfAccounts
		 */
		VarIntLS[] all = new VarIntLS[numberOfAccounts * numberOfAvoidedPeriods];
		for (int i = 0; i < numberOfAccounts; i++) {
			for (int j = 0; j < numberOfAvoidedPeriods; j++) {
				all[i * numberOfAvoidedPeriods + j] = x[i][j];
			}
		}

		IFunction[] f = new IFunction[numberOfPeriods];
		for (int i = 0; i < numberOfPeriods; i++) {
			f[i] = new Occurrence(all, i);
		}

		IFunction max = new Max(f);
		IFunction min = new Min(f);
		FuncMinus mm = new FuncMinus(max, min);

		/*
		 * Search
		 */
		ls.close();
		System.out.println("Init S = " + S.violations());
		TabuSearch ts = new TabuSearch();
		ts.searchMaintainConstraintsMinimize2(mm, S, numberOfAccounts, 10000, 5000, 200);
		System.out.println("\n");
		System.out.println("S  =   " + S.violations());
		for (int i = 0; i < numberOfPeriods; i++)
			System.out.print(f[i].getValue() + "     -->   ");
		System.out.println();
		System.out.println("max = " + max.getValue());
		System.out.println("min = " + min.getValue());
		System.out.println("mm = " + mm.getValue());

		int[][] avoidPeriods = new int[numberOfAccounts][numberOfAvoidedPeriods];
		for (int i = 0; i < numberOfAccounts; i++) {
			System.out.println();
			for (int j = 0; j < numberOfAvoidedPeriods; j++) {
				avoidPeriods[i][j] = x[i][j].getValue();
				System.out.printf("%2d ", x[i][j].getValue());
			}
		}

		return avoidPeriods;

	}

	
	public String convertToString(int[] arr, int len) {
		String result = "";
		for (int i=0; i<len; i++) {
			result += arr[i] + "|";
		}
		result = result.substring(0, result.length()-1);
		return result;
	}
	
	public static String convertToString(int[][] arr, int row, int column) {
		String result = "";
		for (int i = 0; i < row; i++) {
			String line = "";
			for (int j = 0; j < column; j++) {
				line += arr[i][j] + "|";
			}
			result += line.substring(0, line.length() - 1) + "\n";
		}
		// Remove /n character in the end
		result = result.substring(0, result.length() - 1);
		System.out.println(result);
		return result;
	}
	
	public void printResult(int[][] matrix, int rows, int cols) {
		for (int i=0; i<rows; i++) {
			System.out.println();
			for (int j=0; j<cols; j++) {
				System.out.print(matrix[i][j] + " ");
			}
		}
		System.out.println();
	}

	
	public void setAvoidedPeridosForAccounts() {
		String url = "jdbc:mysql://hitvn-backend-crawler-master-db-test.ciwx42paf5ev.ap-southeast-1.rds.amazonaws.com:3306/social_listening";
		String username = "root";
		String password = "Hitvn2020";
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection(url, username, password);
			
			/*
			 * Get list clusters
			 */
			List<Cluster> listClusters = new ArrayList<Cluster>();
			
			PreparedStatement getAllActiveClustersStatement = con.prepareStatement("SELECT Id, Name, Period, NumberOfAvoidedPeriods, Space, NumberOfAccounts FROM clusters WHERE Status = ?");
			getAllActiveClustersStatement.setInt(1, 1);
			ResultSet rs = getAllActiveClustersStatement.executeQuery();
			
			while (rs.next()) {
				
				Cluster cluster = new Cluster();
				cluster.Id = rs.getInt("Id");
				cluster.Name = rs.getString("Name");
				cluster.Period = rs.getInt("Period");
				cluster.NumberOfAvoidedPeriods = rs.getInt("NumberOfAvoidedPeriods");
				cluster.Space = rs.getInt("Space");
				cluster.NumberOfAccounts = rs.getInt("NumberOfAccounts");
				
				listClusters.add(cluster);
				
				PreparedStatement getAccountStatement = con.prepareStatement("SELECT distinct(OrderInCluster) FROM accounts WHERE Cluster = ? AND Status = ? Order By OrderInCluster ASC");
				getAccountStatement.setInt(1, 1);
				getAccountStatement.setInt(2, cluster.Id);
				ResultSet accountResults = getAccountStatement.executeQuery();
				
				int flag = 0;
				int rowCount = 0;
				while (accountResults.next()) {
					int orderInCluster = accountResults.getInt("OrderInCluster");
					if (orderInCluster == rowCount) {
						rowCount++;
					} else {
						flag = 1;
						break;
					}
				}
				
				if (flag == 0 && cluster.NumberOfAccounts == rowCount) {
					int[][] avoidPeriods = findAvoidedPeriods(cluster.NumberOfAccounts, cluster.Period, cluster.NumberOfAvoidedPeriods);
					
					/*
					 * Update avoided periods for cluster
					 */
					String insertValue = convertToString(avoidPeriods, cluster.NumberOfAccounts, cluster.NumberOfAvoidedPeriods);
					PreparedStatement insertAvoidedPeriodsStatement = con.prepareStatement("UPDATE clusters SET AvoidedPeriods = ? WHERE Id = ?");
					insertAvoidedPeriodsStatement.setString(1, insertValue);
					insertAvoidedPeriodsStatement.setInt(2, cluster.Id);
					int insertResults = insertAvoidedPeriodsStatement.executeUpdate();
					System.out.println(insertResults);
				} else {
					System.out.println("Make sure all accounts in culster " + cluster.Id + " are consistent before generating avoided periods!!!");
				}
				
					
//					for (int i=0; i<numberOfAccounts; i++) {
//						String insertValue = convertToString(avoidPeriods[i], cluster.NumberOfAvoiedPeriods);
//						PreparedStatement insertAvoidedPeriodsStatement = con.prepareStatement("UPDATE accounts SET AvoidedPeriods = ? WHERE OrderInCluster = ?");
//						insertAvoidedPeriodsStatement.setString(1, insertValue);
//						insertAvoidedPeriodsStatement.setInt(2, i);
//						int insertResults = insertAvoidedPeriodsStatement.executeUpdate();
//						System.out.println(insertResults);
//					}
				
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	public static void main(String[] args) {
		AvoidPeriods sch = new AvoidPeriods();
		sch.setAvoidedPeridosForAccounts();
		
	}
}


class Account {
	int Id;
	String Username;
	int Cluster;
	int OrderInCluster;
	int Status;
}
