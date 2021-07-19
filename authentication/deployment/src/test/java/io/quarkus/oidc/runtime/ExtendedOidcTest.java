package io.quarkus.oidc.runtime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;

import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.runtime.ExecutorRecorder;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.mutiny.Uni;

public class ExtendedOidcTest {
    @RegisterExtension
    static QuarkusUnitTest runner = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource("application.properties"));
    protected DefaultTenantConfigResolver tenantConfigResolver;

    @BeforeEach
    public void setUp() {
        String publicKey = getPublicKey();
        OidcTenantConfig config = new OidcTenantConfig();
        OidcProvider provider = new OidcProvider(publicKey, config);
        //HERE IS ERROR
        TenantConfigContext context = new TenantConfigContext(provider, config);
        tenantConfigResolver = new DefaultTenantConfigResolver();
        TenantConfigBean tenantConfigBean = new TenantConfigBean(Collections.emptyMap(),
                Collections.emptyMap(),
                context,
                oidcTenantConfig ->
                        Uni.createFrom()
                                .emitter(uniEmitter ->
                                        uniEmitter.complete(context)),
                ExecutorRecorder.getCurrent());
        tenantConfigResolver = new DefaultTenantConfigResolver();
        //HERE IS ERROR ALSO
        tenantConfigResolver.tenantConfigBean = tenantConfigBean;
    }

    private String getPublicKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            keyPair = generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] encoded = keyPair.getPublic().getEncoded();
        Base64.Encoder encoder = Base64.getMimeEncoder();
        return new String(encoder.encode(encoded), StandardCharsets.UTF_8);
    }

    @Test
    public void testSuccessfullyGetChallengeData() {
        ExtendedOidcAuthenticationMechanism mechanism = new ExtendedOidcAuthenticationMechanism();
        assertNotNull(mechanism);
    }
}
