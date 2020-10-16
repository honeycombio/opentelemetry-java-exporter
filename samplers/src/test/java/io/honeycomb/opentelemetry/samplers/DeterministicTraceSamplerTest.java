package io.honeycomb.opentelemetry.samplers;

import io.opentelemetry.common.AttributeKey;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.trace.Sampler;
import io.opentelemetry.sdk.trace.Sampler.Decision;
import io.opentelemetry.sdk.trace.Sampler.SamplingResult;
import io.opentelemetry.trace.Span;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class DeterministicTraceSamplerTest {

    private Sampler sampler;

    private static String SPAN_NAME = "span-name";
    private static Span.Kind SPAN_KIND = Span.Kind.CLIENT;

    @Test
    void alwaysOnSampler_GetDescription() {
        assertEquals("HoneycombDeterministicSampler", new DeterministicTraceSampler(1).getDescription());
    }

    @Test
    public void samplerShouldRejectNegativeSampleRate() {
        assertThrows(IllegalArgumentException.class, () -> new DeterministicTraceSampler(-1));
    }

    @Test
    public void checkSamplerWithSampleDataPoints() {
        sampler = new DeterministicTraceSampler(17);

        SamplingResult result = sampler.shouldSample(
                null,
                "hello",
                SPAN_NAME,
                SPAN_KIND,
                Attributes.empty(),
                Collections.emptyList());
        assertEquals(Decision.DROP, result.getDecision());
        assertEquals(Attributes.of(AttributeKey.longKey("sample.rate"), 0L), result.getAttributes());

        result = sampler.shouldSample(
                null,
                "world",
                SPAN_NAME,
                SPAN_KIND,
                Attributes.empty(),
                Collections.emptyList());
        assertEquals(Decision.DROP, result.getDecision());
        assertEquals(Attributes.of(AttributeKey.longKey("sample.rate"), 0L), result.getAttributes());

        result = sampler.shouldSample(
                null,
                "this5",
                SPAN_NAME,
                SPAN_KIND,
                Attributes.empty(),
                Collections.emptyList());
        assertEquals(Decision.RECORD_AND_SAMPLE, result.getDecision());
        assertEquals(Attributes.of(AttributeKey.longKey("sample.rate"), 17L), result.getAttributes());
    }

    @Test
    public void testThatVariousSampleRatesAreWithinExpectedBounds() {
        final int[] testSampleRates = {2, 10, 20};
        final int numberOfRequestIDsToTest = 50000;
        final double acceptableMarginOfError = 0.05;

        for (int sampleRate : testSampleRates) {
            sampler = new DeterministicTraceSampler(sampleRate);
            int nSampled = 0;

            for (int i = 0; i < numberOfRequestIDsToTest; i++) {
                SamplingResult result = sampler.shouldSample(null, randomRequestID(), SPAN_NAME, SPAN_KIND, Attributes.empty(), Collections.emptyList());
                if (result.getDecision() == Decision.RECORD_AND_SAMPLE) {
                    nSampled++;
                }
            }

            // Sampling should be balanced across all request IDs regardless of sample rate.
            // If we cross this threshold, flunk the test.
            double expectedNSampled = numberOfRequestIDsToTest * (1 / (double) sampleRate);
            int lower = (int) (expectedNSampled - (expectedNSampled * acceptableMarginOfError));
            int upper = (int) (expectedNSampled + (expectedNSampled * acceptableMarginOfError));
            assertTrue(nSampled >= lower);
            assertTrue(nSampled <= upper);
        }
    }

    @Test
    public void checkThatASampleRateOfOneSamplesAll() {
        final int nRequestIDs = 50_000;

        final int sampleRate = 1;
        sampler = new DeterministicTraceSampler(sampleRate);

        int nSampled = 0;
        for (int i = 0; i < nRequestIDs; i++) {
            SamplingResult result = sampler.shouldSample(null, randomRequestID(), SPAN_NAME, SPAN_KIND, Attributes.empty(), Collections.emptyList());
            if (result.getDecision() == Decision.RECORD_AND_SAMPLE) {
                nSampled++;
            }
        }

        // should sample all
        assertEquals(50_000, nSampled);
    }

    @Test
    public void checkThatASampleRateOfZeroSamplesNone() {
        final int nRequestIDs = 50_000;

        final int sampleRate = 0;
        sampler = new DeterministicTraceSampler(sampleRate);

        int nSampled = 0;
        for (int i = 0; i < nRequestIDs; i++) {
            SamplingResult result = sampler.shouldSample(null, randomRequestID(), SPAN_NAME, SPAN_KIND, Attributes.empty(), Collections.emptyList());
            if (result.getDecision() == Decision.RECORD_AND_SAMPLE) {
                nSampled++;
            }
        }

        // should sample none
        assertEquals(0, nSampled);
    }

    @Test
    public void checkThatSamplerGivesConsistentAnswers() {
        final DeterministicTraceSampler samplerA = new DeterministicTraceSampler(3);
        final DeterministicTraceSampler samplerB = new DeterministicTraceSampler(3);
        final String sampleString = UUID.randomUUID().toString();
        final int firstAnswer = samplerA.sample(sampleString);

        // sampler should not give different answers for subsequent runs
        for (int i = 0; i < 25; i++) {
            final int answerA = samplerA.sample(sampleString);
            final int answerB = samplerB.sample(sampleString);
            assertEquals(firstAnswer, answerA);
            assertEquals(firstAnswer, answerB);
        }
    }

    private static final String requestIDBytes = "abcdef0123456789";

    /**
     * Comment from node tests:
     * <pre>
     * // create request ID roughly resembling something you would get from
     * // AWS ALB, e.g.,
     * //
     * // 1-5ababc0a-4df707925c1681932ea22a20
     * //
     * // The AWS docs say the middle bit is "time in seconds since epoch",
     * // (implying base 10) but the above represents an actual Root= ID from
     * // an ALB access log, so... yeah.
     * </pre>
     */
    private String randomRequestID() {
        final StringBuilder reqID = new StringBuilder("1-");
        for (int i = 0; i < 8; i++) {
            final int charToPick = getRandomInt(requestIDBytes.length());
            reqID.append(requestIDBytes.charAt(charToPick));
        }
        reqID.append("-");
        for (int i = 0; i < 24; i++) {
            final int charToPick = getRandomInt(requestIDBytes.length());
            reqID.append(requestIDBytes.charAt(charToPick));
        }
        return reqID.toString();
    }

    private int getRandomInt(final int max) {
        return (int) (Math.random() * max);
    }
}
