package de.unimannheim.swt.pse.ctf;

import de.unimannheim.swt.pse.ctf.game.engine.Game;
import de.unimannheim.swt.pse.ctf.game.engine.GameEngine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is the main class (entry point) of your webservice.
 */
@SpringBootApplication
public class CtfApplication {
	private static ConfigurableApplicationContext context;
	public static void main(String[] args) {
		context = SpringApplication.run(CtfApplication.class, args);
	}

	/**
	 * FIXME You need to return your game engine implementation here. Return an instance here.
	 *
	 * @return your {@link Game} engine.
	 */
	public static Game createGameEngine() {
		return new GameEngine();
	}

	 /**
	 * Stops the SpringBootApplication.
	 */
	public static void stopApplication() {
		if (context != null) {
			SpringApplication.exit(context, () -> 0);
		}
	}

}
