import boardifier.control.StageFactory;
import boardifier.model.Model;
import boardifier.view.RootPane;
import boardifier.view.View;
import control.MerelleController;
import control.MerelleDecider;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main entry point for the Merelle console game.
 *
 * Usage:
 *   java Main <mode> [strategy]
 *
 *   mode:
 *     0 -> Human vs Human
 *     1 -> Human vs Computer
 *     2 -> Computer vs Computer
 *
 *   strategy (optional):
 *     0 -> RANDOM
 *     1 -> SMART (default)
 *
 */
public class Main extends Application {

    private static int gameMode = 0;
    private static int strategy = MerelleDecider.STRATEGY_SMART;

    public static void main(String[] args) {
        if (args.length >= 1) {
            try {
                gameMode = Integer.parseInt(args[0]);
                if (gameMode < 0 || gameMode > 2) {
                    System.err.println("Mode must be 0, 1, or 2. Defaulting to 0.");
                    gameMode = 0;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid mode. Defaulting to 0.");
            }
        }
        if (args.length >= 2) {
            try {
                strategy = Integer.parseInt(args[1]);
                if (strategy < 0 || strategy > 1) {
                    System.err.println("Strategy must be 0 or 1. Defaulting to SMART.");
                    strategy = MerelleDecider.STRATEGY_SMART;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid strategy. Defaulting to SMART.");
            }
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        Model model = new Model();

        switch (gameMode) {
            case 0:
                model.addHumanPlayer("player1");
                model.addHumanPlayer("player2");
                System.out.println("Mode: Human (player1) vs Human (player2)");
                break;
            case 1:
                model.addHumanPlayer("player1");
                model.addComputerPlayer("Robot");
                System.out.println("Mode: Human (player1) vs Computer (Robot) — strategy: "
                        + (strategy == MerelleDecider.STRATEGY_SMART ? "SMART" : "RANDOM"));
                break;
            case 2:
                model.addComputerPlayer("Robot-W");
                model.addComputerPlayer("Robot-B");
                System.out.println("Mode: Computer vs Computer — strategy: "
                        + (strategy == MerelleDecider.STRATEGY_SMART ? "SMART" : "RANDOM"));
                break;
        }

        StageFactory.registerModelAndView(
                "merelle",
                "model.MerelleStageModel",
                "view.MerelleStageView");

        RootPane rootPane = new RootPane();
        View view = new View(model, primaryStage, rootPane);

        MerelleController controller = new MerelleController(model, view, strategy);
        controller.setFirstStageName("merelle");

        try {
            controller.startGame();
        } catch (Exception e) {
            System.err.println("Failed to start game: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        Thread gameThread = new Thread(() -> {
            try {
                controller.stageLoop();
            } catch (Exception e) {
                System.err.println("Game error: " + e.getMessage());
                e.printStackTrace();
            }
            System.exit(0);
        });
        gameThread.setDaemon(false);
        gameThread.start();
    }
}