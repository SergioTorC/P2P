package main;

import controller.P2P;
import view.View;

import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {

		P2P p2P = new P2P();
		View view = new View();
		view.setController(p2P);
		p2P.setView(view);
	}
}
