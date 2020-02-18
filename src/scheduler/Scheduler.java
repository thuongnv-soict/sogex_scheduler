package scheduler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import localsearch.constraints.alldifferent.AllDifferent;
import localsearch.constraints.basic.IsEqual;
import localsearch.constraints.basic.LessOrEqual;
import localsearch.functions.occurrence.Occurrence;
import localsearch.model.ConstraintSystem;
import localsearch.model.IFunction;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;
import localsearch.search.TabuSearch;

public class Scheduler {
	
	/*
	 * Result Example:
	 * 
	 * Accounts: 5
	 * Space = 15 minutes
	 * Server = 2
	 * Period = 3 
	 * 
	 * Period:           1                                    2                                3                                        4 
	 * 
	 * | 4  1  3  1  1  1  4  4  3  3  1  1 | 4  3  4  3  4  2  3  3  2  3  2  2 | 2  2  0  2  0  0  2  0  2  0  0  2 | 2  0  0  0  2  0  3  2  3  0  0  2 | 0  0  4  0  2  4  2  2  0  4  0  0 | 0  0  0  1  0  1  1  1  0  1  1  1 | 1  4  4  1  1  1  1  1  1  1  4  4 | 1  3  1  1  3  3  1  1  1  3  3  1 
	 * | 3  3  4  4  4  4  1  1  1  4  3  4 | 2  4  2  4  3  3  2  4  4  4  3  3 | 0  0  2  0  2  2  0  2  0  2  2  0 | 0  3  3  3  0  3  0  0  0  2  3  0 | 4  4  0  4  0  0  0  0  4  0  4  2 | 1  1  1  0  1  0  0  0  1  0  0  0 | 4  1  1  4  4  4  4  4  4  4  1  1 | 3  1  3  3  1  1  3  3  3  1  1  3 
	 * 
	 * Number Of Periods = 24/3 = 8
	 * Space Per Periods = 3 * 60 / 15 = 12 
	 */

	public int[][] findConsistentScheduler(int[][] avoidedPeriods, int numberOfAccounts, int numberOfAvoidedPeriods, int numberOfPeriods, int limitedCrawlTimePerAccount,
			int numberOfServers, int numberOfSpaces) {

		int spacesPerPeiods = numberOfSpaces/ numberOfPeriods;
		LocalSearchManager ls = new LocalSearchManager();
		ConstraintSystem S = new ConstraintSystem(ls);

		VarIntLS[][] x = new VarIntLS[numberOfServers][numberOfSpaces];
		for (int i = 0; i < numberOfServers; i++) {
			for (int j = 0; j < numberOfSpaces; j++) {
				x[i][j] = new VarIntLS(ls, 0, numberOfAccounts - 1);
			}
		}

		/*
		 * Limit crawl times
		 */

		// Flat 2D array to calculate Occurrence
		VarIntLS[] allDay = new VarIntLS[numberOfServers * numberOfSpaces];
		for (int i = 0; i < numberOfServers; i++) {
			for (int j = 0; j < numberOfSpaces; j++) {
				allDay[i * numberOfSpaces + j] = x[i][j];
			}
		}

		// Add constraint
		IFunction[] f3 = new IFunction[numberOfAccounts];
		for (int i = 0; i < numberOfAccounts; i++) {
			f3[i] = new Occurrence(allDay, i);
			S.post(new LessOrEqual(f3[i], limitedCrawlTimePerAccount));
		}

		/*
		 * Make sure one account do one job in one time
		 */

		// Transpose matrix
		VarIntLS[][] col = new VarIntLS[numberOfSpaces][numberOfServers];
		for (int i = 0; i < numberOfSpaces; i++) {
			for (int j = 0; j < numberOfServers; j++) {
				col[i][j] = x[j][i];
			}
		}
		// Add constraint
		for (int i = 0; i < numberOfSpaces; i++) {
			S.post(new AllDifferent(col[i]));
		}

		/*
		 * Make sure no account work over 4 periods in a day
		 */

		// Split matrix by periods
		VarIntLS[][] period = new VarIntLS[numberOfPeriods][spacesPerPeiods * numberOfServers];
		for (int k = 0; k < numberOfPeriods; k++) {
			for (int i = 0; i < numberOfServers; i++) {
				for (int j = 0; j < spacesPerPeiods; j++) {
					period[k][i * spacesPerPeiods + j] = x[i][spacesPerPeiods * k + j];
				}
			}
		}

		// Add constraint
		for (int i = 0; i < numberOfAccounts; i++) {
			for (int j = 0; j < numberOfAvoidedPeriods; j++) {
				for (int k = 0; k < numberOfPeriods; k++) {
					IFunction fx = new Occurrence(period[avoidedPeriods[i][j]], i);
					S.post(new IsEqual(fx, 0));
				}
			}
		}

		/*
		 * Search
		 */
		ls.close();
		System.out.println("Init S = " + S.violations());
		TabuSearch ts = new TabuSearch();
		ts.search(S, numberOfSpaces * numberOfServers, 100000, 10000, 5000);
		System.out.println(S.violations() + "\n");

		// Logging
		int[][] scheduler = new int[numberOfServers][numberOfSpaces];
		for (int i = 0; i < numberOfServers; i++) {
			System.out.println();
			for (int j = 0; j < numberOfSpaces; j++) {
				if (j % spacesPerPeiods == 0) {
					System.out.print("|");
				}
				scheduler[i][j] = x[i][j].getValue();
				System.out.printf("%2d ", x[i][j].getValue());
			}
		}

		return scheduler;

	}

	public static String convertToString(int[] arr, int len) {
		String result = "";
		for (int i = 0; i < len; i++) {
			result += arr[i] + "|";
		}
		// Remove /n character in the end
		result = result.substring(0, result.length() - 1);
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

	
	public static void printResult(int[][] matrix, int rows, int cols) {
		for (int i=0; i<rows; i++) {
			System.out.println();
			for (int j=0; j<cols; j++) {
				System.out.print(matrix[i][j] + "-");
			}
		}
		System.out.println();
	}
	
	public static int[][] convertToMatrix(String str, int rows, int cols) {
		String[] listStr = str.split("\\r?\\n");
		int[][] avoidedPeriods = new int[rows][cols];
		for (int i = 0; i < rows; i++) {
			String[] subStr = listStr[i].split("\\|");
			int[] listInt = new int[cols];
			for (int j = 0; j < cols; j++) {
				listInt[j] = Integer.parseInt(subStr[j]);
				System.out.println(listInt[j]);
			}
			avoidedPeriods[i] = listInt;
		}
		return avoidedPeriods;
	}

	public boolean schedule(String date) {
		String url = "jdbc:mysql://hitvn-backend-crawler-master-db-test.ciwx42paf5ev.ap-southeast-1.rds.amazonaws.com:3306/social_listening";
		String username = "root";
		String password = "Hitvn2020";
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection(url, username, password);

			/*
			 * Get list clusters
			 */
			PreparedStatement getActiveClusters = con
					.prepareStatement("SELECT Id, Name, Period, NumberOfAvoidedPeriods, AvoidedPeriods, Space, NumberOfAccounts, NumberOfServers, LimitedCrawlTimesPerAccount FROM clusters WHERE Status = ?");
			getActiveClusters.setInt(1, 1);

			ResultSet rs = getActiveClusters.executeQuery();
			while (rs.next()) {
				
				Cluster cluster = new Cluster();
				
				cluster.Id = rs.getInt("Id");
				cluster.Name = rs.getString("Name");
				cluster.Period = rs.getInt("Period");
				cluster.NumberOfAvoidedPeriods = rs.getInt("NumberOfAvoidedPeriods");
				cluster.AvoidedPeriods = rs.getString("AvoidedPeriods");
				cluster.Space = rs.getInt("Space");
				cluster.NumberOfAccounts = rs.getInt("NumberOfAccounts");
				cluster.NumberOfServers = rs.getInt("NumberOfServers");
				cluster.LimitedCrawlTimesPerAccount = rs.getInt("LimitedCrawlTimesPerAccount");
				
//				String[] listAvoidedPeriods = new String[cluster.NumberOfAccounts];
				
				// Get avoided periods for all accounts in each cluster
//				PreparedStatement getAccountStatement = con.prepareStatement(
//						"SELECT AvoidedPeriods FROM cluster WHERE Cluster = ? AND Status = ? ORDER BY OrderInCluster ASC");
//				getAccountStatement.setInt(1, 1);
//				getAccountStatement.setInt(2, cluster.Id);
//
//				ResultSet accountResults = getAccountStatement.executeQuery();
//				int numberOfAccounts = 0;
//				while (accountResults.next()) {
//					listAvoidedPeriods[numberOfAccounts] = accountResults.getString("AvoidedPeriods");
//					numberOfAccounts++;=
//				}
				
				/*
				 * Calculate number of spaces
				 * Ex: space = 15 minutes/times  -> number of spaces = 1440 / 15 = 96
				 */
				int numberOfSpaces = 1440 / cluster.Space;
				int numberOfPeriods = 24 / cluster.Period;
				int[][] avoidedPeriods = convertToMatrix(cluster.AvoidedPeriods, cluster.NumberOfAccounts, cluster.NumberOfAvoidedPeriods);
				System.out.println(cluster.NumberOfAccounts + " " + cluster.NumberOfAvoidedPeriods);
				printResult(avoidedPeriods, cluster.NumberOfAccounts, cluster.NumberOfAvoidedPeriods);
				int[][] scheduler = findConsistentScheduler(avoidedPeriods, cluster.NumberOfAccounts, cluster.NumberOfAvoidedPeriods, numberOfPeriods, cluster.LimitedCrawlTimesPerAccount,
						cluster.NumberOfServers, numberOfSpaces);

				/*
				 * Insert scheduler to database
				 */
				String schedulerString = convertToString(scheduler, cluster.NumberOfServers , numberOfSpaces);

				PreparedStatement insertScheduler = con.prepareStatement(
						"INSERT INTO schedulers(Date, Cluster, Content, Status) VALUES (STR_TO_DATE(?,'%d-%m-%Y'), ?, ?, ?)");
				insertScheduler.setString(1, date);
				insertScheduler.setInt(2, cluster.Id);
				insertScheduler.setString(3, schedulerString);
				insertScheduler.setInt(4, 1);
				int insertResults = insertScheduler.executeUpdate();

				System.out.println(insertResults);
			}

			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}

		return true;
	}

	public static void main(String[] args) {
		Scheduler sch = new Scheduler();
		sch.schedule("06-02-2020");
	}
}
