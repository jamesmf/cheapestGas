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
	
	
	public static void main(String args[]){
		int[] Ns = {25}; //number of stations
		int U = 5; //size of the tank -- can vary
		
		for (int n : Ns){
			ArrayList<Station> data = getData(n);
			System.out.println(data);
			
			//initialize our objects to slide a window of 'distance' U (tank size) over the nodes
			PriorityQueue<Station> window = new PriorityQueue<Station>(); //priority queue lets us check min(cost) in log(n) time
			ArrayList<Station> underConsideration = new ArrayList<Station>(); //useful for removing from queue
			
			//initalize ArrayLists of 'next' and 'previous' cheapest stations
			ArrayList<Station> prev = new ArrayList<Station>(); //list of cheapest previous station within range U for station i
			ArrayList<Station> next = new ArrayList<Station>(); //list of cheapest future station within range U for station i
			for (int i=0; i< n; i++){
				prev.add(new Station(-1,10000000,10000000,10000000)); //initialize with unimportant values
				next.add(new Station(-1,10000000,10000000,10000000));
			}

			for (Station s : data){
				//check if we need to remove from past station from the queue 
				//(if a previous station is more than U away from current)

				while (true && (underConsideration.size() > 0)){
					Station prevS = underConsideration.get(0);
					if (prevS.distanceFromZero < (s.distanceFromZero - U)){
						//the minimum cost right before we remove station i is next(i)
						System.out.println("before adding station "+s.index+" we remove " +prevS.index);
						Station nextStation = Collections.min(underConsideration,new StationComparator());
						next.set(prevS.index, nextStation);
						underConsideration.remove(0);
						window.remove(nextStation);
					}
					else if (prevS.distanceFromZero + U > data.get(data.size()-1).distanceFromZero){
						break;
					}
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
			//now we have to process remaining elements of the queue to get their 'next' values
			while(true && underConsideration.size() > 0){
				Station s2 = underConsideration.get(0);
				System.out.println("before we finish, remove "+s2.index);
				Station nextStation = Collections.min(underConsideration,new StationComparator());
				next.set(s2.index, nextStation);
				underConsideration.remove(0);
				window.remove(nextStation);
			}
			System.out.println("Array of Cheapest Gas Station Previous to Station i Within Distance U");
			System.out.println(prev);
			System.out.println("Array of Cheapest Gas Station After Station i Within Distance U");
			System.out.println(next);
		}
	}
	
}
