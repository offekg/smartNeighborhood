package src;

import java.util.HashMap;
import java.util.Random;;

public class Pedestrian {
	int id;
	int position;
	boolean isInTheNorth;
	boolean allowedToCross = true;
	
	Random randomMoving = new Random();
	
	public Pedestrian(int id, int position, boolean isInTheNorth) {
		this.id = id;
		this.position = position;
		this.isInTheNorth = isInTheNorth;
	}

	public void move(int isFreezeCrosswalk) {
		int nextPosition = position - (randomMoving.nextInt(3) - 1);
		if (allowedToCross) {
			if (!(isFreezeCrosswalk == 0) && (nextPosition == 1 || nextPosition == 2)) {
				int shouldCross = randomMoving.nextInt(2);
				if (shouldCross == 1) {
					cross();
					allowedToCross = false;
				}
			}
		}
		position = nextPosition;
	}
	
	public void cross() {
		isInTheNorth = !isInTheNorth;
	}
	
	public boolean isPedestrianExists() {
		if (this.position < 0 || this.position > 3)
			return false;
		return true;
	}
	
	public HashMap<String, Object> getPedestrianState() {
		HashMap<String, Object> pedestrianData = new HashMap<String, Object>();
		pedestrianData.put("id", this.id);
		pedestrianData.put("position", this.position);
		pedestrianData.put("isInNorth", this.isInTheNorth);
		
		return pedestrianData;
	}
}
