package core;

import java.util.Map;

@FunctionalInterface
public interface MessageListener {
    void onMessage(Map<String, Object> message);
}
