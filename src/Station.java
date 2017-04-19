
public class Station implements Comparable<Station>{
	public int index;
	public double distance;
	public double cost;
	public double distanceFromZero;
	
	public Station(int ind, double dist, double c, double dfz) {
		//each station object has a distance (from previous), a cost of gas, and distance from node 0
		index = ind;
		distance = dist;
		cost = c;
		distanceFromZero = dfz;
	}
	
	
	public String toString(){
		return "ind: "+index+"; dist from prev: "+distance+ "; cost: "+cost+"; total dist: "+distanceFromZero+'\n';
	}


	@Override
	public int compareTo(Station o) {
		// default compare function
		return Double.compare(cost,o.cost);
	}

}
