package me.snowdrop.istio.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.fabric8.kubernetes.api.model.HasMetadata;
import me.snowdrop.istio.api.IstioResource;
import me.snowdrop.istio.api.authentication.v1alpha1.Policy;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRule;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import me.snowdrop.istio.api.policy.v1beta1.Rule;
import me.snowdrop.istio.mixer.template.metric.Metric;
import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IstioClientTest {
    
    private final static YAMLMapper objectMapper = new YAMLMapper();
    
    @Test
    public void shouldApplyMetricIstioResource() {
        checkInput("metric.yaml", Metric.class);
    }
    
    @Test
    public void shouldApplyVirtualServiceIstioResource() {
        checkInput("virtual-service.yaml", VirtualService.class);
    }

    @Test
    public void shouldApplyRuleIstioResource() {
        checkInput("rule.yaml", Rule.class);
    }

    @Test
    public void shouldApplyDestinationRuleIstioResource() {
        checkInput("destination-rule.yaml", DestinationRule.class);
    }

    @Test
    public void shouldApplyPolicyIstioResource() {
        checkInput("policy.yaml", Policy.class);
    }

    private void checkInput(String inputFileName, Class<? extends IstioResource> expectedSpecClass) {
        final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(inputFileName);
        IstioClient client = new DefaultIstioClient();
        List<HasMetadata> result = client.load(inputStream).get();
    
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);
        final HasMetadata hasMetadata = result.get(0);
        try {
            assertThat(hasMetadata.getKind()).isEqualTo(expectedSpecClass.newInstance().getKind());
        } catch (Exception e) {
            Assert.fail("Failed to read resource kind.");
        }
    
        // check roundtrip
        try {
            final ByteArrayInputStream output = new ByteArrayInputStream(objectMapper.writeValueAsBytes(hasMetadata));
            List<HasMetadata> fromOutput = client.load(output).get();
            assertThat(fromOutput).isEqualTo(result);
        } catch (JsonProcessingException e) {
            Assert.fail("Couldn't output resource back to string");
        }
    }

    @Test
    public void shouldApplyAllResourcesInAggregateDescriptor() {
        final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("aggregate.yaml");
        IstioClient client = new DefaultIstioClient();
        List<HasMetadata> result = client.load(inputStream).get();

        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getKind()).isEqualTo(new Metric().getKind());
        assertThat(result.get(1).getKind()).isEqualTo(new VirtualService().getKind());
    }
}
