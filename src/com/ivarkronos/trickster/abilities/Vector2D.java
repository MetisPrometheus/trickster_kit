package com.ivarkronos.trickster.abilities;

public class Vector2D {

	private double x;
	private double y;
	
	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double magnitude(Vector2D vector) {
		return Math.sqrt(vector.x*vector.x + vector.y*vector.y);
	}
	
	public double vectorDotProduct(Vector2D vector1, Vector2D vector2) {
		return vector1.x*vector2.x + vector1.y*vector2.y;
	}
	
	public long vectorAngleWith(Vector2D vector) {
		double dotProduct = vectorDotProduct(this, vector);
		double magVector1 = magnitude(this);
		double magVector2 = magnitude(vector);
		
		long angle = Math.round(Math.toDegrees(Math.acos(dotProduct/(magVector1*magVector2))));
		return angle;
	}
	
}
