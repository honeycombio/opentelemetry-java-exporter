package io.honeycomb.opentelemetry.exporters;

import io.honeycomb.libhoney.builders.HoneyClientBuilder;
import io.honeycomb.libhoney.EventPostProcessor;
import io.honeycomb.libhoney.ResponseObserver;
import io.honeycomb.libhoney.ValueSupplier;
import io.honeycomb.libhoney.transport.Transport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.URISyntaxException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HoneycombSpanExporterBuilderTest {

    final String serviceName = "my-service";
    HoneycombSpanExporterBuilder builder;
    HoneyClientBuilder mockBuilder;

    @BeforeEach
    public void setUp() {
        builder = new HoneycombSpanExporterBuilder(serviceName);
        builder.clientBuilder = mockBuilder = spy(builder.clientBuilder);
        when(mockBuilder.build()).thenCallRealMethod();
    }

    @Test
    public void addGlobalField() {
        builder.addGlobalField("name", "value").build();
        verify(mockBuilder, times(1)).addGlobalField("name", "value");
        completeNegativeVerification();
    }

    @Test
    public void addGlobalDynamicFields() {
        builder.addGlobalDynamicFields("name", mock(ValueSupplier.class)).build();
        verify(mockBuilder, times(1)).addGlobalDynamicFields(eq("name"), any(ValueSupplier.class));
        completeNegativeVerification();
    }

    @Test
    public void addProxyCredential() {
        builder.addProxy("proxy.domain.com:8443", "user", "secret").build();
        verify(mockBuilder, times(1)).addProxy("proxy.domain.com:8443", "user", "secret");
        completeNegativeVerification();
    }

    @Test
    public void dataSet() {
        builder.dataSet("set").build();
        verify(mockBuilder, times(1)).dataSet("set");
        completeNegativeVerification();
    }

    @Test
    public void apiHost() throws URISyntaxException {
        builder.apiHost("host:80").build();
        verify(mockBuilder, times(1)).apiHost("host:80");
        verify(mockBuilder, times(1)).apiHost(any(URI.class));
        completeNegativeVerification();
    }

    @Test
    public void writeKey() {
        builder.writeKey("key").build();
        verify(mockBuilder, times(1)).writeKey("key");
        completeNegativeVerification();
    }

    @Test
    public void testDebugEnabled() {
        builder.debug(true).build();
        verify(mockBuilder, times(1)).debug(true);
        completeNegativeVerification();
    }

    @Test
    public void testDebugDisabled() {
        builder.debug(false).build();
        verify(mockBuilder, times(1)).debug(false);
        completeNegativeVerification();
    }

    @Test
    public void batchSize() {
        builder.batchSize(123).build();
        verify(mockBuilder, times(1)).batchSize(123);
        completeNegativeVerification();
    }

    @Test
    public void batchTimeoutMillis() {
        builder.batchTimeoutMillis(123).build();
        verify(mockBuilder, times(1)).batchTimeoutMillis(123);
        completeNegativeVerification();
    }

    @Test
    public void queueCapacity() {
        builder.queueCapacity(123).build();
        verify(mockBuilder, times(1)).queueCapacity(123);
        completeNegativeVerification();
    }

    @Test
    public void maxPendingBatchRequests() {
        builder.maxPendingBatchRequests(123).build();
        verify(mockBuilder, times(1)).maxPendingBatchRequests(123);
        completeNegativeVerification();
    }

    @Test
    public void maxConnections() {
        builder.maxConnections(123).build();
        verify(mockBuilder, times(1)).maxConnections(123);
        completeNegativeVerification();
    }

    @Test
    public void maxConnectionsPerApiHost() {
        builder.maxConnectionsPerApiHost(123).build();
        verify(mockBuilder, times(1)).maxConnectionsPerApiHost(123);
        completeNegativeVerification();
    }

    @Test
    public void connectionTimeout() {
        builder.connectionTimeout(123).build();
        verify(mockBuilder, times(1)).connectionTimeout(123);
        completeNegativeVerification();
    }

    @Test
    public void connectionRequestTimeout() {
        builder.connectionRequestTimeout(123).build();
        verify(mockBuilder, times(1)).connectionRequestTimeout(123);
        completeNegativeVerification();
    }

    @Test
    public void socketTimeout() {
        builder.socketTimeout(123).build();
        verify(mockBuilder, times(1)).socketTimeout(123);
        completeNegativeVerification();
    }

    @Test
    public void bufferSize() {
        builder.bufferSize(5_000).build();
        verify(mockBuilder, times(1)).bufferSize(5_000);
        completeNegativeVerification();
    }

    @Test
    public void ioThreadCount() {
        builder.ioThreadCount(1).build();
        verify(mockBuilder, times(1)).ioThreadCount(1);
        completeNegativeVerification();
    }

    @Test
    public void maximumHttpRequestShutdownWait() {
        builder.maximumHttpRequestShutdownWait(345L).build();
        verify(mockBuilder, times(1)).maximumHttpRequestShutdownWait(345L);
        completeNegativeVerification();
    }

    @Test
    public void additionalUserAgent() {
        builder.additionalUserAgent("agent").build();
        verify(mockBuilder, times(1)).additionalUserAgent("agent");
        completeNegativeVerification();
    }

    @Test
    public void addProxyNoCredentials() {
        builder.addProxy("proxyHost").build();
        verify(mockBuilder, times(1)).addProxy(anyString());
        completeNegativeVerification();
    }

    @Test
    public void sslContext() {
        final SSLContext mockContext = mock(SSLContext.class);
        builder.sslContext(mockContext).build();
        verify(mockBuilder, times(1)).sslContext(any(SSLContext.class));
        completeNegativeVerification();
    }

    @Test
    public void addResponseObserver() {
        builder.addResponseObserver(mock(ResponseObserver.class)).build();
        verify(mockBuilder, times(1)).addResponseObserver(any(ResponseObserver.class));
        completeNegativeVerification();
    }

    @Test
    public void eventPostProcessor() {
        builder.eventPostProcessor(mock(EventPostProcessor.class)).build();
        verify(mockBuilder, times(1)).eventPostProcessor(any(EventPostProcessor.class));
        completeNegativeVerification();
    }

    @Test
    public void transport() {
        final Transport mockTransport = mock(Transport.class);
        builder.transport(mockTransport).build();

        verify(mockBuilder, times(1)).transport(mockTransport);
        verifyNoMoreInteractions(mockTransport);
        completeNegativeVerification();
    }

    private void completeNegativeVerification(){
        verify(mockBuilder, times(1)).build();
        verifyNoMoreInteractions(mockBuilder);
    }
}
