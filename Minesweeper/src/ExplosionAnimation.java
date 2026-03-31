import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ExplosionAnimation extends JWindow {

    private static final int TOTAL_FRAMES = 35;
    private static final int FPS          = 30;
    private static final int GIF_DURATION_MS = 1600;

    private int frame = 0;
    private Timer timer;
    private Timer autoCloseTimer;
    private Runnable onComplete;

    private ImageIcon gifIcon;
    private boolean useGif;

    private static class Particle {
        float x, y, vx, vy, life, maxLife, size;
        Color color;
    }
    private Particle[] particles;
    private final Random rng = new Random();

    private float shockRadius = 0;
    private float shockAlpha  = 1f;

    public ExplosionAnimation(Window parent, Runnable onComplete) {
        super(parent);
        this.onComplete = onComplete;

        setBackground(new Color(0, 0, 0, 0));
        if (parent != null) {
            setBounds(parent.getBounds());
        } else {
            setBounds(100, 100, 400, 300);
        }
        setAlwaysOnTop(true);

        gifIcon = loadGif("media/explosion.gif");
        useGif = gifIcon != null;

        if (useGif) {
            add(buildGifPanel(gifIcon));
        }
    }

    private ImageIcon loadGif(String source) {
        if (source == null || source.isBlank()) return null;
        try {
            if (source.startsWith("http://") || source.startsWith("https://")
                    || source.startsWith("file:/")) {
                return new ImageIcon(new URL(source));
            }

            ImageIcon fromResource = loadFromResource(source);
            if (fromResource != null) return fromResource;

            for (Path path : getCandidatePaths(source)) {
                if (Files.exists(path)) {
                    return new ImageIcon(path.toAbsolutePath().toString());
                }
            }
        } catch (Exception ex) {
            System.err.println("ExplosionAnimation: failed to load GIF â†’ " + ex.getMessage());
        }
        return null;
    }

    private ImageIcon loadFromResource(String source) {
        List<String> resourcePaths = new ArrayList<>();
        if (source.startsWith("/")) {
            resourcePaths.add(source);
        } else {
            resourcePaths.add("/" + source);
            if (!source.contains("/") && !source.contains("\\")) {
                resourcePaths.add("/media/" + source);
            }
        }

        for (String path : resourcePaths) {
            try (InputStream in = ExplosionAnimation.class.getResourceAsStream(path)) {
                if (in == null) continue;
                URL url = ExplosionAnimation.class.getResource(path);
                if (url != null) return new ImageIcon(url);
            } catch (Exception ignored) {
                // Best-effort only; fall through to filesystem candidates.
            }
        }
        return null;
    }

    private List<Path> getCandidatePaths(String source) {
        List<Path> paths = new ArrayList<>();
        Path cwd = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        Path srcPath = Paths.get(source);

        if (srcPath.isAbsolute()) {
            paths.add(srcPath);
            return paths;
        }

        paths.add(cwd.resolve(source));
        paths.add(cwd.resolve("src").resolve(source));
        paths.add(cwd.resolve("Minesweeper").resolve(source));
        paths.add(cwd.resolve("Minesweeper").resolve("src").resolve(source));

        boolean hasSeparator = source.contains("/") || source.contains("\\");
        if (!hasSeparator) {
            paths.add(cwd.resolve("media").resolve(source));
            paths.add(cwd.resolve("src").resolve("media").resolve(source));
            paths.add(cwd.resolve("Minesweeper").resolve("media").resolve(source));
            paths.add(cwd.resolve("Minesweeper").resolve("src").resolve("media").resolve(source));
        }

        try {
            Path codeSource = Paths.get(ExplosionAnimation.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            paths.add(codeSource.resolve("media").resolve(source));
            Path parent = codeSource.getParent();
            if (parent != null) {
                paths.add(parent.resolve("src").resolve("media").resolve(source));
            }
        } catch (Exception ignored) {
            // Best-effort only; fall through to filesystem candidates above.
        }
        return paths;
    }


    private JPanel buildGifPanel(ImageIcon gif) {
        GifPanel panel = new GifPanel(gif);
        panel.setOpaque(false);
        return panel;
    }

    private static class GifPanel extends JPanel {
        private final ImageIcon gifIcon;

        GifPanel(ImageIcon gifIcon) {
            this.gifIcon = gifIcon;
            if (this.gifIcon != null) {
                this.gifIcon.setImageObserver(this);
            }
            setLayout(new GridBagLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (gifIcon == null) {
                g2.dispose();
                return;
            }

            int srcW = Math.max(1, gifIcon.getIconWidth());
            int srcH = Math.max(1, gifIcon.getIconHeight());
            int maxW = (int) (getWidth() * 0.80);
            int maxH = (int) (getHeight() * 0.80);
            if (maxW <= 0 || maxH <= 0) {
                g2.dispose();
                return;
            }

            double scale = Math.min((double) maxW / srcW, (double) maxH / srcH);
            int dstW = (int) (srcW * scale);
            int dstH = (int) (srcH * scale);
            int x = (getWidth() - dstW) / 2;
            int y = (getHeight() - dstH) / 2;

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.translate(x, y);
            g2.scale(scale, scale);
            gifIcon.paintIcon(this, g2, 0, 0);
            g2.dispose();
        }
    }




    public void play() {
        setVisible(true);

        if (useGif) {
            autoCloseTimer = new Timer(GIF_DURATION_MS, e -> dismiss());
            autoCloseTimer.setRepeats(false);
            autoCloseTimer.start();
            return;
        }

        timer = new Timer(1000 / FPS, e -> {
            // Update particles first, then paint
            for (Particle p : particles) {
                if (p.life <= 0f) continue;
                p.x   += p.vx;
                p.y   += p.vy;
                p.vy  += 0.4f;  // gravity
                p.life -= 1f;
                if (p.life < 0f) p.life = 0f;  // never go below 0
            }
            shockRadius += 18f;

            frame++;
            repaint();
            if (frame >= TOTAL_FRAMES) {
                timer.stop();
                dismiss();
            }
        });
        timer.start();
    }

    private void dismiss() {
        if (timer != null) timer.stop();
        if (autoCloseTimer != null) autoCloseTimer.stop();
        setVisible(false);
        dispose();
        if (onComplete != null) onComplete.run();
    }

    public static void playExplosion(Window parent, Runnable after) {
        SwingUtilities.invokeLater(() -> {
            ExplosionAnimation anim = new ExplosionAnimation(parent, after);
            anim.play();
        });
    }
}
