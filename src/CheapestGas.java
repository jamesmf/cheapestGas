import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Collections;
import java.util.List;

public class CheapestGas {

	public static ArrayList<Station> getData(int n){
		//returns a List of <distances, costs> each length n (for n stations)
		ArrayList<Station> data = new ArrayList<Station>();
		
		double distanceFromZero = 0;
		for (int i=0; i<n; i++){
			double distance = Math.random();
			double cost = Math.random();
			int index = i;
			Station station = new Station(index,distance,cost,distanceFromZero);
			data.add(station);
			distanceFromZero = distanceFromZero + distance;
		}
		return data;		
	}
	
	
	public static void main(String args[]) throws IOException{
		int[] Ns = {25}; //number of stations
		int U = 5; //size of the tank -- can vary
		
		FileWriter writer = new FileWriter("../output.csv");
		
		for (int n : Ns){
			ArrayList<Station> data = getData(n);
			System.out.println(data);
			
			double t1 = System.currentTimeMillis();
			
			//initialize our objects to slide a window of 'distance' U (tank size) over the nodes
			PriorityQueue<Station> window = new PriorityQueue<Station>(); //priority queue lets us check min(cost) in log(n) time
			ArrayList<Station> underConsideration = new ArrayList<Station>(); //useful for removing from queue
			
			//initialize ArrayLists of 'next' and 'previous' cheapest stations
			ArrayList<Station> prev = new ArrayList<Station>(); //list of cheapest previous station within range U for station i
			ArrayList<Station> next = new ArrayList<Station>(); //list of cheapest future station within range U for station i
			for (int i=0; i< n; i++){
				prev.add(new Station(-1,10000000,10000000,10000000)); //initialize with unimportant values
				next.add(new Station(-1,10000000,10000000,10000000));
			}

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
						System.out.println("before adding station "+s.index+" we remove " +prevS.index);
						underConsideration.remove(0);
						window.remove(prevS);
						Station nextStation = Collections.min(underConsideration,new StationComparator());
						//store the minimum 'next' station in next[station_to_remove.index]
						next.set(prevS.index, nextStation);

					}
/*					else if (prevS.distanceFromZero + U > data.get(data.size()-1).distanceFromZero){
						break;
					}*/
					else{
						break;
					}
				}
				
				//insert into the priority queue
				window.add(s);
				underConsideration.add(s);
				//immediately when the station falls into the window, we can calculate prev(i)
				Station prevStation = Collections.min(underConsideration,new StationComparator());
				//assign minimum cost station under consideration as the 'prev()' of station s
				prev.set(s.index, prevStation);
				
			}
			//loop over stations is finished, but we still have stations with no 'next'
			//so here we have to process remaining elements of the queue
			while(true && underConsideration.size() > 0){
				Station s2 = underConsideration.get(0);
				System.out.println("before we finish, remove "+s2.index);
				Station nextStation = Collections.min(underConsideration,new StationComparator());
				next.set(s2.index, nextStation);
				underConsideration.remove(0);
				window.remove(s2);
			}
			System.out.println("Array of Cheapest Gas Station Previous to Station i Within Distance U");
			System.out.println(prev);
			System.out.println("Array of Cheapest Gas Station After Station i Within Distance U");
			System.out.println(next);
			
			
			//now we need to check which stations are "breakpoints" - where prev[i] == i
			List<Integer> breakPoints = new ArrayList<Integer>();
			int i = 0;
			for (Station potentialBreakPoint : prev){
				if (potentialBreakPoint.index == i){
					breakPoints.add(i);
				}
				i++;
			}
			System.out.println(breakPoints);
			
			//now we can 'paste together' solutions from breakpoint to breakpoint to attain the global optimum
			breakPoints.add(n);
			breakPoints.remove(0);
			int j = 0;
			int k = 0;
			int counter = 0;
			double totalCost = 0;
			
			while (k < n){
				k = breakPoints.get(counter);
				System.out.println("outside: "+j+" "+k);
				driveToNext(j,k,data,next,totalCost,U);
				j = k;
				counter++;
			}
			System.out.println(totalCost);
		}
	}


	private static void driveToNext(int j, int k, ArrayList<Station> data, ArrayList<Station> next, double totalCost, int U) {
		//this implements the locally optimal solution that, when used only from point i to a breakpoint k, achieves global optimization
		Station stationJ = data.get(j);
		Station stationK = data.get(k);
		int failsafe = 0;
		while (j < k &&  failsafe <10 ){
			if (stationK.distanceFromZero - stationJ.distanceFromZero > U){
				Station upNext = next.get(j);
				j = upNext.index;
				totalCost += stationJ.cost * (upNext.distanceFromZero - stationJ.distanceFromZero); 
				System.out.println(upNext);
				stationJ = data.get(j);
			}
			else{
				Station upNext = stationK;
				j = upNext.index;
				totalCost += stationJ.cost * (upNext.distanceFromZero - stationJ.distanceFromZero);
				System.out.println(upNext);
			}
			System.out.println("inside: "+j+" "+k+" "+totalCost);
			failsafe++;
		}
		
	}
	
}
