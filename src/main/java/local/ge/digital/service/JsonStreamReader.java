package local.ge.digital.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import local.ge.digital.exception.NoFirstPageInFileException;
import local.ge.digital.exception.PageNotFoundException;
import local.ge.digital.model.Page;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static local.ge.digital.service.JsonStreamReader.BooleanSupplier.alwaysFalse;

class JsonStreamReader {
    private static final int NB_TOKEN_TO_FIRST_PAGE = 4;
    private static final int NB_TOKEN_TO_LINKS = 2;
    private static final String PAGE_ADDRESS = "address";
    private final static ConcurrentMap<String, Page> PARSED_PAGES = new ConcurrentHashMap<>();

    private final MappingJsonFactory mappingJsonFactory;
    private final String filepath;

    JsonStreamReader(String filepath) {
        this.mappingJsonFactory = new MappingJsonFactory();
        this.filepath = filepath;
    }

    JsonStreamReader clearAll() {
        PARSED_PAGES.clear();
        return this;
    }

    String getFirstPageAddress() {
        try(JsonParser parser = mappingJsonFactory.createParser(new FileReader(filepath))) {

            shiftParserBy(NB_TOKEN_TO_FIRST_PAGE, parser);
            if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                Page firstPage = parser.readValueAs(Page.class);
                PARSED_PAGES.put(firstPage.getAddress(), firstPage);
                return firstPage.getAddress();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new NoFirstPageInFileException();
    }

    Page getOrCreatePage(String link) {
        return getPageFromMap(link).orElseGet(() -> searchForNewPage(link));
    }

    private Optional<Page> getPageFromMap(String link) {
        synchronized (PARSED_PAGES) {
            return PARSED_PAGES.containsKey(link) ? Optional.of(PARSED_PAGES.get(link)) : Optional.empty();
        }
    }

    private Page searchForNewPage(String link) {
        Page newPage = search(link, links -> new Page(link, links), () -> null);

        if(pageWasFound(newPage)) {
            PARSED_PAGES.putIfAbsent(newPage.getAddress(), newPage);
            return newPage;
        } else {
            throw new PageNotFoundException();
        }
    }

    private boolean pageWasFound(Page newPage) {
        return newPage != null;
    }

    boolean isLinkValid(String link) {
        return PARSED_PAGES.containsKey(link) || search(link, links -> cacheAndAlwaysTrue(link, links), alwaysFalse());
    }

    private Boolean cacheAndAlwaysTrue(String link, List<String> links) {
        PARSED_PAGES.putIfAbsent(link, new Page(link, links));
        return true;
    }

    private <R> R search(String link, Function<List<String>, R> onFound, Supplier<R> onMissing) {
        try (JsonParser parser = mappingJsonFactory.createParser(new FileReader(filepath))) {

            for (JsonToken token = parser.nextToken(); token != null; token = parser.nextToken()) {
                if (token == JsonToken.VALUE_STRING && nameIsAddress(parser.currentName()) && addressMatch(link, parser.getValueAsString())) {
                    shiftParserBy(NB_TOKEN_TO_LINKS, parser);
                    return onFound.apply(extractLinks(parser));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return onMissing.get();
    }

    private boolean nameIsAddress(String currentName) {
        return currentName != null && currentName.equals(PAGE_ADDRESS);
    }

    private boolean addressMatch(String link, String address) {
        return link.equals(address);
    }

    private void shiftParserBy(int nbToken, JsonParser parser) throws IOException {
        while (nbToken-- > 0) {
            parser.nextToken();
        }
    }

    private List<String> extractLinks(JsonParser parser) throws IOException {
        return Arrays.asList(parser.readValueAs(String[].class));
    }

    static class BooleanSupplier implements Supplier<Boolean> {
        static BooleanSupplier alwaysFalse() {
            return new BooleanSupplier();
        }

        @Override
        public Boolean get() {
            return false;
        }
    }
}