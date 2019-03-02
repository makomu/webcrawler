package local.ge.digital;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import local.ge.digital.service.WebCrawler;

public class Demo {

    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        WebCrawler wc = new WebCrawler();

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(wc.crawl("src/main/resources/internet_1.json", 75)));
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(wc.crawl("src/main/resources/internet_2.json", 30)));
    }
}
