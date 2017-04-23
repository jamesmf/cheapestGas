import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Collections;
import java.util.List;

public class CheapestGas {

	public static ArrayList<Station> getData(int n){
		//returns a List of <distances, costs> each length n (for n stations)
		ArrayList<Station> data = new ArrayList<Station>();
		
		double distanceFromZero = 0;
		Random r = new Random();
		for (int i=0; i<n; i++){
			double distance = r.nextGaussian()*3+30;
			double cost = r.nextGaussian()*0.5+3;
			int index = i;
			Station station = new Station(index,distance,cost,distanceFromZero);
			data.add(station);
			distanceFromZero = distanceFromZero + distance;
		}
		return data;		
	}
	
	
	public static void main(String args[]) throws IOException{
		
		int[] Ns = {12000000,13000000,14000000,15000000,16000000,17000000,18000000};//,
				//12500000,15000000,20000000}; //number of stations
		int numberOfIterations = 5; //in order to lower variance, we will run each n multiple times and take the mean
		int U = 5000; //size of the tank -- can vary
		
		for (int n : Ns){
			double totalTime = 0;
			double[] times = new double[numberOfIterations];
			for (int numIt = 0; numIt < numberOfIterations; numIt++){
				ArrayList<Station> data = getData(n);
		//			System.out.println(data);
				
				
				
				//initialize our objects to slide a window of 'distance' U (tank size) over the nodes
				PriorityQueue<Station> window = new PriorityQueue<Station>(); //priority queue lets us check min(cost) in log(n) time
				ArrayList<Station> underConsideration = new ArrayList<Station>(); //useful for removing from queue
				
				//initialize ArrayLists of 'next' and 'previous' cheapest stations
				ArrayList<Station> prev = new ArrayList<Station>(); //list of cheapest previous station within range U for station i
				ArrayList<Station> next = new ArrayList<Station>(); //list of cheapest future station within range U for station i
				for (int i=0; i< n; i++){
					//initialize with dummy values
					Station blankStation = new Station(-1,10000000,10000000,10000000);
					prev.add(blankStation); 
					next.add(blankStation);
				}
				
				//initialize the time
				double t1 = System.currentTimeMillis();
				
				//iterate over the stations to calculate 'next' and 'prev' cheapest stations ONCE (dynamic programming)
				for (Station s : data){
					//check if we need to remove from past station from the queue 
					//(if a previous station is more than U away from current)
					while (true && (underConsideration.size() > 0)){
						Station prevS = underConsideration.get(0);
						if (prevS.distanceFromZero < (s.distanceFromZero - U)){
							//the minimum cost right after we remove station i is next(i) 
							//because when it is the earliest station left in the queue, every other station in the queue is 
							//1) within driving distance U and 2) after it
		//						System.out.println("before adding station "+s.index+" we remove " +prevS.index);
							underConsideration.remove(0);
							window.remove(prevS);
							Station nextStation = Collections.min(underConsideration,new StationComparator());
							//store the minimum 'next' station in next[station_to_remove.index]
							next.set(prevS.index, nextStation);
		
						}
						else{
							break;
						}
					}
					
					//insert into the priority queue
					window.add(s);
					underConsideration.add(s);
					//immediately when the station falls into the window, we can calculate prev
					Station prevStation = Collections.min(underConsideration,new StationComparator());
					//assign minimum cost station under consideration as the 'prev[s]' of station s
					prev.set(s.index, prevStation);
					
				}
				//loop over stations is finished, but we still have stations with no 'next'
				//so here we have to process remaining elements of the queue
				while(true && underConsideration.size() > 0){
					Station s2 = underConsideration.get(0);
		//				System.out.println("before we finish, remove "+s2.index);
					Station nextStation = Collections.min(underConsideration,new StationComparator());
					next.set(s2.index, nextStation);
					underConsideration.remove(0);
					window.remove(s2);
				}
		//			System.out.println("Array of Cheapest Gas Station Previous to Station i Within Distance U");
		//			System.out.println(prev);
		//			System.out.println("Array of Cheapest Gas Station After Station i Within Distance U");
		//			System.out.println(next);
				
				
				//now we need to check which stations are "breakpoints" - where prev[i] == i
				List<Integer> breakPoints = new ArrayList<Integer>();
				int i = 0;
				for (Station potentialBreakPoint : prev){
					if (potentialBreakPoint.index == i){
						breakPoints.add(i);
					}
					i++;
				}
		//			System.out.println(breakPoints);
				
				//now we can 'paste together' solutions from breakpoint to breakpoint to attain the global optimum
				breakPoints.add(n-1);
				breakPoints.remove(0);
				int[] jk = {0,0};
				int counter = 0;
				double totalCost = 0;
				int[] moves = {0};
				while (jk[1] < n-1){
					jk[1] = breakPoints.get(counter);
		//				System.out.println(jk[0]+" "+jk[1]);
					totalCost = driveToNext(jk,data,next,totalCost,U,moves);
					counter++;
				}
		//			System.out.println(totalCost);
				double time = System.currentTimeMillis() - t1;
				//System.out.println(String.valueOf(n)+','+n*Math.log(n)+','+time);
				//System.out.println(time);
				totalTime+=time;
				times[numIt] = time;
			}
		Arrays.sort(times);
		double med = times[(int)(numberOfIterations/2)];
		double min = times[0];
		double max = times[numberOfIterations-1];
		System.out.println(String.valueOf(n)+','+n*Math.log(n)+','+med+","+min+","+max);
		
		}
	}			



	private static double driveToNext(int[] jk, ArrayList<Station> data, ArrayList<Station> next, double totalCost, int U,int[] moves) {
		//this implements the locally optimal solution that, when used only from point i to a breakpoint k, achieves global optimization
		int j = jk[0];
		int k = jk[1];
		Station stationJ = data.get(j);
		Station stationK = data.get(k);
		while (j < k ){
			if (stationK.distanceFromZero - stationJ.distanceFromZero > U){
				//this means we can't drive straight to k, we need to stop at an intermediate station
				Station upNext = next.get(j);
				j = upNext.index;
				double diff = (upNext.distanceFromZero - stationJ.distanceFromZero);
				double c = stationJ.cost * diff;
//				System.out.println("drove from "+stationJ.index+", a distance of "+df.format(diff)+", to "+upNext.index+"; cost "+df.format(stationJ.cost)+"*"+df.format(diff)+'='+df.format(stationJ.cost*diff));
				totalCost +=  c;
				stationJ = data.get(j);
				moves[0]++;
			}
			else{
				//this means we can drive straight to breakpoint k
				Station upNext = stationK;
				j = upNext.index;
				double diff = (upNext.distanceFromZero - stationJ.distanceFromZero);
				double c = stationJ.cost * diff;
//				System.out.println("drove from "+stationJ.index+", a distance of "+df.format(diff)+", to "+upNext.index+"; cost "+df.format(stationJ.cost)+"*"+df.format(diff)+'='+df.format(stationJ.cost*diff));
				totalCost += c;
				moves[0]++;
			}
		}
		jk[0] = j;
		jk[1] = k;
		return totalCost;
		
	}
	
}
