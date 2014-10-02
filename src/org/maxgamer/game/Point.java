package org.maxgamer.game;

/**
 * @author Dirk
 * Represents a point in a 2D plane.
 * This uses doubles for precision
 * unlike java.awt.Point.
 */
public class Point{
	public double x,y;
	
	/**
	 * Creates a new point with the same properties as p.
	 * @param p The point to copy
	 */
	public Point(Point p){
		this.x = p.x;
		this.y = p.y;
	}
	/**
	 * Creates a new point with (x,y) coordinates
	 * @param x The x coordinate
	 * @param y The y coordinate
	 */
	public Point(double x, double y){
		this.x = x;
		this.y = y;
	}
	/**
	 * Returns a duplicate of this point.
	 */
	public Point clone(){
		return new Point(this);
	}
	/**
	 * Returns the distance squared to a point.
	 * This is much faster than using a distance(Point p) because
	 * square roots are very expensive to calculate.
	 * @param p The point to find the distance to
	 * @return the distance squared between the two points.
	 */
	public double distanceSq(Point p){
		return Math.pow(p.x - x, 2) + Math.pow(p.y - y,2);
	}
	/**
	 * Returns the distance squared to a point.
	 * This is a very expensive function as it uses
	 * the Math.sqrt() method.
	 * @param p The point to find the distance to.
	 * @return The distance to the given point.
	 */
	public double distance(Point p){
		return Math.sqrt(distanceSq(p));
	}
	
	/**
	 * Adds another point to this one. E.g x += p.x; y+= p.y.
	 * @param p The point to add
	 * @return This point
	 */
	public Point add(Point p){
		this.x += p.x;
		this.y += p.y;
		return this;
	}
	
	public Point add(double d, double e){
		this.x += d;
		this.y += e;
		return this;
	}
	
	@Override
	public String toString(){
		return "[" + this.x + ", " + this.y + "]";
	}
}