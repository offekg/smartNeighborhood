package src;

import java.util.Random;;

public class Pedestrian {
	int id;
	int position;
	boolean isInTheNorth;
	
	Random randomMoving = new Random();
	
	public Pedestrian(int id, boolean isInTheNorth) {
		this.id = id;
		this.position = -1;
		this.isInTheNorth = isInTheNorth;
	}

	public boolean move(boolean freezeCrosswalk) {
		int nextPosition = randomMoving.nextInt(2) - position - 1;
		if (nextPosition == -1 || nextPosition == 4)
			return false; // means that this pedestrian goes inactive.
		if (!freezeCrosswalk && (nextPosition == 1 || nextPosition == 2)) {
			int shouldCross = randomMoving.nextInt(1);
			if (shouldCross == 1)
				cross();
		}
		position = nextPosition;
		return true;
	}
	
	public void cross() {
		isInTheNorth = !isInTheNorth;
	}
}
