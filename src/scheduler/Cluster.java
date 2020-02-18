package scheduler;

class Cluster {
	public int Id;
	public String Name;
	public int Period;
	public String AvoidedPeriods;
	public int Space;
	public int NumberOfAccounts;
	public int NumberOfServers;
	public int NumberOfAvoidedPeriods;
	public int LimitedCrawlTimesPerAccount;
	public Cluster(int id, String name, int period, int numberOfAvoidedPeriods, String avoidedPeriods, int space, int numberOfAccounts, int numberOfServers) {
		super();
		Id = id;
		Name = name;
		Period = period;
		NumberOfAvoidedPeriods = numberOfAvoidedPeriods;
		AvoidedPeriods = avoidedPeriods;
		Space = space;
		NumberOfAccounts = numberOfAccounts;
		NumberOfServers = numberOfServers;
	}
	public Cluster() {
		super();
	}
}