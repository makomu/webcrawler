package local.ge.digital.service;

import local.ge.digital.model.CrawlResult;
import local.ge.digital.model.Page;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebCrawler {
    private ExecutorService executorService;

    public CrawlResult crawl(String filepath, int timeout) {
        JsonStreamReader reader = init(filepath);

        executorService.execute(new PageTask(filepath, reader.getFirstPageAddress()));
        awaitTermination(timeout);

        return CrawlResult.getInstance();
    }

    private JsonStreamReader init(String filepath) {
        this.executorService = Executors.newCachedThreadPool();
        initResult();
        return initJsonStreamReader(filepath);
    }

    private void initResult() {
        CrawlResult.getInstance().clearAll();
    }

    private JsonStreamReader initJsonStreamReader(String filepath) {
        return new JsonStreamReader(filepath).clearAll();
    }

    class PageTask implements Runnable {
        private final String filepath;
        private final String pageAddress;

        PageTask(String filepath, String pageAddress) {
            this.filepath = filepath;
            this.pageAddress = pageAddress;
        }

         /*
             The call to reader.isLinkValid() in the run method caches the Page if it is found in the file, the
             expected behaviour for reader.getOrCreatePage() is to return the Page object from the parsedPages.
         */

        @Override
        public void run() {
            JsonStreamReader reader = new JsonStreamReader(filepath);
            Page page = reader.getOrCreatePage(pageAddress);
            addToSuccessPages(pageAddress);

            page.getLinks().forEach(link -> {
                if (successPagesContains(link)) {
                    addToSkippedPages(link);
                } else if (reader.isLinkValid(link)) {
                    enqueuePageTask(link);
                } else {
                    addToErrorPages(link);
                }
            });
        }

        private void addToSuccessPages(String pageAddress) {
            CrawlResult.getInstance().getSuccess().add(pageAddress);
        }

        private boolean successPagesContains(String link) {
            return CrawlResult.getInstance().getSuccess().contains(link);
        }

        private void addToSkippedPages(String pageAddress) {
            CrawlResult.getInstance().getSkipped().add(pageAddress);
        }

        private void enqueuePageTask(String link) {
            executorService.execute(new PageTask(filepath, link));
        }

        private void addToErrorPages(String pageAddress) {
            CrawlResult.getInstance().getError().add(pageAddress);
        }
    }

    private void awaitTermination(int timeout) {
        try {
            while (!executorService.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}