package com.test.lab01;

import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class RouteTest extends CamelBlueprintTestSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteTest.class);

    private static final String BLUEPRINT_DESCRIPTOR = "OSGI-INF/blueprint/blueprint-camel.xml";

    @Override
    protected String getBlueprintDescriptor() {
        return BLUEPRINT_DESCRIPTOR;
    }

    @Override
    public boolean isUseAdviceWith() {
        return true;
    }

    @Override
    protected Properties useOverridePropertiesWithPropertiesComponent() {
        final Properties properties = System.getProperties();

        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("META-INF/etc/lab01.cfg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    @Test
    public void testReadFile_whenFileExist_thenPrintContent() throws Exception {
        final RouteDefinition route = context.getRouteDefinition("lab01-route");

        route.adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() {
                replaceFromWith("{{TEST.FILE.ENDPOINT}}?antInclude=File1.xml&noop=true&idempotent=true");
                interceptSendToEndpoint("ftp://*").skipSendToOriginalEndpoint().to("log:uploadfile?showAll=true&multiline=true");
                weaveAddLast().to("mock:output");
            }
        });

        context.start();

        getMockEndpoint("mock:output").expectedMessageCount(1);
        assertMockEndpointsSatisfied();
    }
}
