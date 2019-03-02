package local.ge.digital.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class WebCrawlerTest {

    private WebCrawler sut;
    private ObjectMapper mapper;

    @Before
    public void before() {
        sut = new WebCrawler();
        mapper = new ObjectMapper();
    }

    @Test
    public void testCrawlHappyFlow() throws JsonProcessingException {
        assertThat(crawlInternet("src/main/resources/internet_1.json", 100), equalTo(getExpectedResultInternet1()));
        assertThat(crawlInternet("src/main/resources/internet_2.json", 50), equalTo(getExpectedResultInternet2()));
    }

    private String crawlInternet(String filepath, int timeout) throws JsonProcessingException {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(sut.crawl(filepath, timeout));
    }

    private String getExpectedResultInternet1() {
        StringBuilder sb = new StringBuilder();
        sb
        .append("{\r\n")
        .append("  \"success\" : [ \"http://foo.bar.com/p4\", \"http://foo.bar.com/p2\", \"http://foo.bar.com/p1\", \"http://foo.bar.com/p6\", \"http://foo.bar.com/p5\" ],\r\n")
        .append("  \"skipped\" : [ \"http://foo.bar.com/p4\", \"http://foo.bar.com/p2\", \"http://foo.bar.com/p1\", \"http://foo.bar.com/p5\" ],\r\n")
        .append("  \"error\" : [ \"http://foo.bar.com/p3\", \"http://foo.bar.com/p7\" ]\r\n")
        .append("}");

        return sb.toString();
    }

    private String getExpectedResultInternet2() {
        StringBuilder sb = new StringBuilder();
        sb
        .append("{\r\n")
        .append("  \"success\" : [ \"http://foo.bar.com/p4\", \"http://foo.bar.com/p3\", \"http://foo.bar.com/p2\", \"http://foo.bar.com/p1\", \"http://foo.bar.com/p5\" ],\r\n")
        .append("  \"skipped\" : [ \"http://foo.bar.com/p1\" ],\r\n")
        .append( "  \"error\" : [ ]\r\n")
        .append("}");

        return sb.toString();
    }
}