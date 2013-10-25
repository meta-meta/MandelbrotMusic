package com.generalprocessingunit.processing;
import processing.core.PApplet;


public class Main extends PApplet{
	public static void main(String[] args){
		PApplet.main(new String[] { /*"--present",*/ Main.class.getCanonicalName() });
	}
		
	@Override
	public void setup() {		
		size(1280, 720, PApplet.OPENGL);
	}
	
	@Override
	public void draw(){

	}
}
