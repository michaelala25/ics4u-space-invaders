public enum Direction {
	LEFT, RIGHT;
	
	public static Direction getOpposite(Direction direction){
		if (direction == LEFT)
			return RIGHT;
		return LEFT;
	}
}
