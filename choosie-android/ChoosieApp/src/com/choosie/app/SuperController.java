package com.choosie.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.util.Pair;

public class SuperController {

	Map<Screen, ScreenController> screenToController;

	public SuperController(Activity choosieActivity) {

		List<Pair<Screen, ScreenController>> screenControllerPairs = new ArrayList<Pair<Screen, ScreenController>>();		
		
		screenControllerPairs.add(new Pair<Screen, ScreenController>(Screen.FEED,
				new FeedScreenController(choosieActivity.findViewById(R.id.layout_feed), choosieActivity, this)));
	    screenControllerPairs.add(new Pair<Screen, ScreenController>(Screen.POST, new PostScreenController(
	    		choosieActivity.findViewById(R.id.layout_post), choosieActivity, this)));
	    screenControllerPairs.add(new Pair<Screen, ScreenController>(Screen.ME, new MeScreenController(
	    		choosieActivity.findViewById(R.id.layout_me), choosieActivity, this)));
	    
		screenToController = new HashMap<Screen, ScreenController>();
		
		for (Pair<Screen, ScreenController> pair: screenControllerPairs){
			screenToController.put(pair.first, pair.second);
		}
		
		for (ScreenController screen : screenToController.values()) {
			screen.onCreate();
		}		
	}

	public void switchToScreen(Screen screenToShow) {
		// Hide all screens except 'screen'
		for (Screen screen : screenToController.keySet()) {
			if (screen == screenToShow) {
				screenToController.get(screen).showScreen();
			} else {
				screenToController.get(screen).hideScreen();
			}
		}
	}

}
