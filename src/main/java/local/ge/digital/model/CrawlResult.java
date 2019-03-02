package local.ge.digital.model;

import lombok.Getter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class CrawlResult {
    private Set<String> success = ConcurrentHashMap.newKeySet();
    private Set<String> skipped = ConcurrentHashMap.newKeySet();
    private Set<String> error = ConcurrentHashMap.newKeySet();

    private CrawlResult() {}

    private static class Singleton {
        private static final CrawlResult INSTANCE = new CrawlResult();
    }

    public static CrawlResult getInstance() {
        return Singleton.INSTANCE;
    }

    public void clearAll() {
        Singleton.INSTANCE.getSuccess().clear();
        Singleton.INSTANCE.getSkipped().clear();
        Singleton.INSTANCE.getError().clear();
    }
}
