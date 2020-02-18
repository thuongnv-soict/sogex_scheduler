package scheduler;

import java.util.ArrayList;
import java.util.List;

public class Test {
	
	
	public static void main(String[] args) {
		int[] arr = {0,1,3,2,1,0,3};
		List<Integer> distinct = new ArrayList<Integer>();
		int count = 0;
		int flag = 0;
		for (int i=0; i<arr.length; i++) {
			flag = 0;
			for (Integer d : distinct) {
				if (d.intValue() == arr[i]) {
					flag = 1;
					break;
				}
			}
			if (flag == 0) {
				distinct.add(arr[i]);
				count++;
			}
		}
		System.out.println(count);
	}
}
