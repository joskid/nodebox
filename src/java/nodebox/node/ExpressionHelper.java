package nodebox.node;

import nodebox.graphics.Color;
import nodebox.util.waves.*;

import java.util.List;
import java.util.Random;

/**
 * Class containing static method used in Expression.
 *
 * @see nodebox.node.Expression
 */
public class ExpressionHelper {

    // TODO: Expression system is not thread-safe.
    public static NodeContext currentContext;
    public static Port currentPort;

    public static Random randomGenerator = new Random();

    public static double random(Object seed, double... minmax) {
        switch (minmax.length) {
            case 0:
                return random(seed);
            case 1:
                return random(seed, minmax[0]);
            default: // Anything larger than 2
                return random(seed, minmax[0], minmax[1]);
        }
    }

    public static double random(Object seed) {
        if (seed instanceof Number) {
            Number number = (Number) seed;
            randomGenerator.setSeed(number.longValue() * 100000000);
        } else {
            randomGenerator.setSeed(seed.hashCode());
        }
        return randomGenerator.nextDouble();
    }

    public static double random(Object seed, double max) {
        return random(seed) * max;
    }

    public static double random(Object seed, double min, double max) {
        return min + random(seed) * (max - min);
    }

    public static int randint(Object seed, int min, int max) {
        if (seed instanceof Number) {
            Number number = (Number) seed;
            randomGenerator.setSeed(number.longValue() * 100000000);
        } else {
            randomGenerator.setSeed(seed.hashCode());
        }
        // nextInt's specified value is exclusive, whereas we want to include it, so add 1.
        return min + randomGenerator.nextInt(max - min + 1);
    }

    public static int toInt(double v) {
        return (int) v;
    }

    public static double toFloat(int v) {
        return (double) v;
    }

    public static double clamp(double v, double min, double max) {
        return min > v ? min : max < v ? max : v;
    }

    public static Color color(double... values) {
        switch (values.length) {
            case 0:
                return new Color();
            case 1:
                return new Color(values[0], values[0], values[0]);
            case 2:
                return new Color(values[0], values[0], values[0], values[1]);
            case 3:
                return new Color(values[0], values[1], values[2]);
            case 4:
                return new Color(values[0], values[1], values[2], values[3]);
            default:
                return new Color();
        }
    }

    public static Color color(double gray) {
        return new Color(gray, gray, gray);
    }

    public static Color color(double gray, double alpha) {
        return new Color(gray, gray, gray, alpha);
    }

    public static Color color(double red, double green, double blue) {
        return new Color(red, green, blue);
    }

    public static Color color(double red, double green, double blue, double alpha) {
        return new Color(red, green, blue, alpha);
    }

    public static Color hsb(double... values) {
        switch (values.length) {
            case 0:
                return new Color();
            case 1:
                return new Color(values[0], values[0], values[0]);
            case 2:
                return new Color(values[0], values[1]);
            case 3:
                return Color.fromHSB(values[0], values[1], values[2]);
            case 4:
                return Color.fromHSB(values[0], values[1], values[2], values[3]);
            default:
                return new Color();
        }
    }

    public static Color hsb(double gray) {
        return new Color(gray, gray, gray);
    }

    public static Color hsb(double gray, double alpha) {
        return new Color(gray, gray, gray, alpha);
    }

    public static Color hsb(double hue, double saturation, double brightness) {
        return Color.fromHSB(hue, saturation, brightness);
    }

    public static Color hsb(double hue, double saturation, double brightness, double alpha) {
        return Color.fromHSB(hue, saturation, brightness, alpha);
    }

    public static double wave(AbstractWave.Type type, double... values) {
        double frame = currentContext.getFrame();

        switch (values.length) {
            case 0:
                return wave(type, 0, 1, 60, frame);
            case 1:
                return wave(type, values[0], 1, 60, frame);
            case 2:
                return wave(type, values[0], values[1], 60, frame);
            case 3:
                return wave(type, values[0], values[2], values[3], frame);
            case 4:
                return wave(type, values[0], values[2], values[3], values[4]);
            default:
                return wave(type, 0, 1, 60, frame);
        }
    }

    public static double wave() {
        return wave(AbstractWave.Type.SINE, 0, 1, 60, currentContext.getFrame());
    }

    public static double wave(AbstractWave.Type type) {
        return wave(type, 0, 1, 60, currentContext.getFrame());
    }

    public static double wave(AbstractWave.Type type, double max) {
        return wave(type, 0, max, 60, currentContext.getFrame());
    }

    public static double wave(AbstractWave.Type type, double min, double max) {
        return wave(type, min, max, 60, currentContext.getFrame());
    }

    public static double wave(AbstractWave.Type type, double min, double max, double speed) {
        return wave(type, min, max, speed, currentContext.getFrame());
    }

    public static double wave(AbstractWave.Type type, double min, double max, double speed, double frame) {
        float fmin = (float) min;
        float fmax = (float) max;
        float fspeed = (float) speed;

        AbstractWave wave;
        switch (type) {
            case TRIANGLE:
                wave = TriangleWave.from(fmin, fmax, fspeed);
                break;
            case SQUARE:
                wave = SquareWave.from(fmin, fmax, fspeed);
                break;
            case SAWTOOTH:
                wave = SawtoothWave.from(fmin, fmax, fspeed);
                break;
            case SINE:
            default:
                wave = SineWave.from(fmin, fmax, fspeed);
                break;
        }
        return wave.getValueAt((float) frame);
    }

    public static double hold(double minFrame, double functionValue, double... values) {
        double frame = currentContext.getFrame();

        switch (values.length) {
            case 1:
                return hold(minFrame, functionValue, values[0], frame);
            case 2:
                return hold(minFrame, functionValue, values[0], values[1]);
            case 0:
            default:
                return hold(minFrame, functionValue, 0, frame);
        }
    }

    public static double hold(double minFrame, double functionValue) {
        return hold(minFrame, functionValue, 0, currentContext.getFrame());
    }

    public static double hold(double minFrame, double functionValue, double defaultValue) {
        return hold(minFrame, functionValue, defaultValue, currentContext.getFrame());
    }

    public static double hold(double minFrame, double functionValue, double defaultValue, double frame) {
        return frame < minFrame ? defaultValue : functionValue;
    }

    public static double schedule(double start, double end, double functionValue, double... values) {
        double frame = currentContext.getFrame();

        switch (values.length) {
            case 1:
                return schedule(start, end, functionValue, values[0], frame);
            case 2:
                return schedule(start, end, functionValue, values[0], values[1]);
            case 0:
            default:
                return schedule(start, end, functionValue, 0, frame);
        }
    }

    public static double schedule(double start, double end, double functionValue) {
        return schedule(start, end, functionValue, 0, currentContext.getFrame());
    }

    public static double schedule(double start, double end, double functionValue, double defaultValue) {
        return schedule(start, end, functionValue, defaultValue, currentContext.getFrame());
    }

    public static double schedule(double start, double end, double functionValue, double defaultValue, double frame) {
        return start <= frame && frame < end ? functionValue : defaultValue;
    }

    public static double timeloop(double speed, List<Number> values) {
        return timeloop(speed, values, currentContext.getFrame());
    }

    public static double timeloop(double speed, List<Number> values, double frame) {
        if (values.size() == 0) return 0;
        int index = (int) (frame / speed);
        try {
            return values.get(index % values.size()).doubleValue();
        } catch (ClassCastException e) {
            return 0;
        }
    }

}
