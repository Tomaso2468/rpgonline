package io.github.tomaso2468.rpgonline.transition;

import io.github.tomaso2468.rpgonline.Game;
import io.github.tomaso2468.rpgonline.GameState;
import io.github.tomaso2468.rpgonline.render.Renderer;

public class BlankTransition implements Transition {
	@Override
	public void update(Game game, GameState currentState, GameState nextState, float delta) {
		
	}

	@Override
	public void render(Game game, GameState currentState, GameState nextState, Renderer renderer) {
		
	}

	@Override
	public boolean isDone() {
		return true;
	}

}
