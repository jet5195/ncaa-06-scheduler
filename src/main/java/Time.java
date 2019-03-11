import java.util.HashMap;
import java.util.Map;

public enum Time {
    ABOUT(750),
    CODING(930),
    DATABASES(1200),
    ANOTHER(1080);

    private int value;
    private static Map map = new HashMap<>();

    private Time(int value) {
        this.value = value;
    }

    static {
        for (Time time : Time.values()) {
            map.put(time.value, time);
        }
    }

    public static Time valueOf(int time) {
        return (Time) map.get(time);
    }

    public int getValue() {
        return value;
    }
}