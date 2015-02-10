public enum Direction {
	// The directions the aliens can move.
	
	LEFT, RIGHT;
	
	public static Direction getOpposite(Direction direction){
		if (direction == LEFT)
			return RIGHT;
		return LEFT;
	}
}
