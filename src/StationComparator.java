import java.util.Comparator;

public class StationComparator implements Comparator<Station> {

	@Override
	public int compare(Station o1, Station o2) {
		//compares two stations based on their cost
		//useful for the priority queue
		return Double.compare(o1.cost,o2.cost);
	}

}
