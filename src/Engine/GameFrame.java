package Engine;

import EnemyTanks.DynamicTankEasy;
import EnemyTanks.EnemyTank;
import EnemyTanks.StaticTankEasy;
import EnemyTanks.StaticTankHard;
import Equipment.Bullet;
import Equipment.Rocket;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 * The window on which the rendering is performed.
 * This example uses the modern BufferStrategy approach for double-buffering,
 * actually it performs triple-buffering!
 * For more information on BufferStrategy check out:
 * http://docs.oracle.com/javase/tutorial/extra/fullscreen/bufferstrategy.html
 * http://docs.oracle.com/javase/8/docs/api/java/awt/image/BufferStrategy.html
 *
 * @author Seyed Mohammad Ghaffarian
 */
public class GameFrame extends JFrame {

    public static final int GAME_HEIGHT = 720;                  // 720p game resolution
    public static final int GAME_WIDTH = 16 * GAME_HEIGHT / 9;  // wide aspect ratio

    private BufferedImage mainTankImage;
    private BufferedImage mainTankGun;
    private BufferedImage bulletImage;
    private BufferedImage rocketImage;
    private BufferedImage staticTankEasyBodyImage;
    private BufferedImage staticTankHardBodyImage;
    private BufferedImage enemyTankGunImage;
    private BufferedImage dynamicTankEasyBodyImage;

    private long lastRender;
    private ArrayList<Float> fpsHistory;

    private BufferStrategy bufferStrategy;

    public GameFrame(String title) {
        super(title);
        setResizable(false);
        setSize(GAME_WIDTH, GAME_HEIGHT);
        lastRender = -1;
        fpsHistory = new ArrayList<>(100);

        try {
            mainTankImage = ImageIO.read(new File("./pictures/tank-body.png"));
            mainTankGun = ImageIO.read(new File("./pictures/tank-gun.jpg"));
            bulletImage = ImageIO.read(new File("./pictures/bullet1.png"));
            rocketImage = ImageIO.read(new File("./pictures/bullet3.png"));
            staticTankEasyBodyImage = ImageIO.read(new File("./pictures/enemy-tank-body1.png"));
            staticTankHardBodyImage = ImageIO.read(new File("./pictures/enemy-tank-body4.png"));
            dynamicTankEasyBodyImage = ImageIO.read(new File("./pictures/enemy-tank-body2.png"));
            enemyTankGunImage = ImageIO.read(new File("./pictures/enemy-gun.jpg"));


        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * This must be called once after the JFrame is shown:
     * frame.setVisible(true);
     * and before any rendering is started.
     */
    public void initBufferStrategy() {
        // Triple-buffering
        createBufferStrategy(3);
        bufferStrategy = getBufferStrategy();
    }


    /**
     * Game rendering with triple-buffering using BufferStrategy.
     */
    public void render(GameState state) {
        // Render single frame
        do {
            // The following loop ensures that the contents of the drawing buffer
            // are consistent in case the underlying surface was recreated
            do {
                // Get a new graphics context every time through the loop
                // to make sure the strategy is validated
                Graphics2D graphics = (Graphics2D) bufferStrategy.getDrawGraphics();
                try {
                    doRendering(graphics, state);
                } finally {
                    // Dispose the graphics
                    graphics.dispose();
                }
                // Repeat the rendering if the drawing buffer contents were restored
            } while (bufferStrategy.contentsRestored());

            // Display the buffer
            bufferStrategy.show();
            // Tell the system to do the drawing NOW;
            // otherwise it can take a few extra ms and will feel jerky!
            Toolkit.getDefaultToolkit().sync();

            // Repeat the rendering if the drawing buffer was lost
        } while (bufferStrategy.contentsLost());
    }

    /**
     * Rendering all game elements based on the game state.
     */
    private void doRendering(Graphics2D g2d, GameState state) {
        // Draw background
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        g2d.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);


        g2d.drawImage(mainTankImage, state.getMainTank().getLocX(), state.getMainTank().getLocY(), null);

        try {
            for (Bullet bullet : state.getBullets()) {
                try {
                    AffineTransform backup = g2d.getTransform();
                    AffineTransform trans = new AffineTransform();
                    trans.rotate(bullet.getRadian(), bullet.getLocX(), bullet.getLocY()); // the points to rotate around (the center in my example, your left side for your problem)

                    g2d.transform(trans);
                    g2d.drawImage(bulletImage, bullet.getLocX(), bullet.getLocY(), null);  // the actual location of the sprite
                    g2d.setTransform(backup); // restore previous transform
                } catch (NullPointerException | ConcurrentModificationException e) {

                }
            }
        } catch (ConcurrentModificationException e) {

        }

        try {
            for (Rocket rocket : state.getRockets()) {
                try {


                    AffineTransform trans = new AffineTransform();
                    AffineTransform backup = g2d.getTransform();
                    trans.rotate(rocket.getRadian(), rocket.getLocX(), rocket.getLocY()); // the points to rotate around (the center in my example, your left side for your problem)

                    g2d.transform(trans);
                    g2d.drawImage(rocketImage, rocket.getLocX(), rocket.getLocY(), null);  // the actual location of the sprite
                    g2d.setTransform(backup); // restore previous transform
                } catch (NullPointerException | ConcurrentModificationException e) {

                }
            }
        } catch (ConcurrentModificationException e) {

        }


        for (EnemyTank enemyTank : state.getEnemyTanks()) {
            if (enemyTank instanceof StaticTankEasy) {
                g2d.drawImage(staticTankEasyBodyImage, enemyTank.getLocX(), enemyTank.getLocY(), null);
            }
            if (enemyTank instanceof StaticTankHard) {
                g2d.drawImage(staticTankHardBodyImage, enemyTank.getLocX(), enemyTank.getLocY(), null);
            }
            if (enemyTank instanceof DynamicTankEasy) {
                g2d.drawImage(dynamicTankEasyBodyImage, enemyTank.getLocX(), enemyTank.getLocY(), null);

            }

            AffineTransform backup = g2d.getTransform();
            AffineTransform trans = new AffineTransform();
            trans.rotate(enemyTank.getGunAndBodyRadian(), enemyTank.getTankCenterX(), enemyTank.getTankCenterY()); // the points to rotate around (the center in my example, your left side for your problem)

            g2d.transform(trans);
            g2d.drawImage(enemyTankGunImage, enemyTank.getGunLocX(), enemyTank.getGunLocY(), null);  // the actual location of the sprite
            g2d.setTransform(backup); // restore previous transform


        }

        AffineTransform backup = g2d.getTransform();
        AffineTransform trans = new AffineTransform();
        trans.rotate(state.getMainTank().getGunAndBodyRadian(), state.getMainTank().getTankCenterX(), state.getMainTank().getTankCenterY()); // the points to rotate around (the center in my example, your left side for your problem)

        g2d.transform(trans);
        g2d.drawImage(mainTankGun, state.getMainTank().getGunLocX(), state.getMainTank().getGunLocY(), null);  // the actual location of the sprite
        g2d.setTransform(backup); // restore previous transform


        // Draw GAME OVER
        if (state.gameOver) {
            String str = "GAME OVER";
            g2d.setColor(Color.WHITE);
            g2d.setFont(g2d.getFont().deriveFont(Font.BOLD).deriveFont(64.0f));
            int strWidth = g2d.getFontMetrics().stringWidth(str);
            g2d.drawString(str, (GAME_WIDTH - strWidth) / 2, GAME_HEIGHT / 2);
        }
    }


}