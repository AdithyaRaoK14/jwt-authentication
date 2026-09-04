package com.example.jwt_authentication.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.cert.Certificate;

@Configuration
public class JwtConfig {

    @Value("${jwt.keystore.path}")
    private Resource keyStoreResource;

    @Value("${jwt.keystore.password}")
    private String keyStorePassword;

    @Value("${jwt.keystore.alias}")
    private String keyStoreAlias;

    @Bean
    public KeyStore keyStore() throws Exception {

        KeyStore keyStore =
                KeyStore.getInstance("PKCS12");

        try (InputStream inputStream =
                     keyStoreResource.getInputStream()) {

            keyStore.load(
                    inputStream,
                    keyStorePassword.toCharArray()
            );
        }

        return keyStore;
    }

    @Bean
    public RSAPrivateKey privateKey(
            KeyStore keyStore) throws Exception {

        Key key =
                keyStore.getKey(
                        keyStoreAlias,
                        keyStorePassword.toCharArray()
                );

        return (RSAPrivateKey) key;
    }

    @Bean
    public RSAPublicKey publicKey(
            KeyStore keyStore) throws Exception {

        Certificate certificate =
                keyStore.getCertificate(keyStoreAlias);

        return (RSAPublicKey)
                certificate.getPublicKey();
    }

    @Bean
    public JwtEncoder jwtEncoder(
            RSAPublicKey publicKey,
            RSAPrivateKey privateKey) {

        RSAKey rsaKey =
                new RSAKey.Builder(publicKey)
                        .privateKey(privateKey)
                        .build();

        JWKSource<com.nimbusds.jose.proc.SecurityContext>
                jwkSource =
                new ImmutableJWKSet<>(
                        new JWKSet(rsaKey)
                );

        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(
            RSAPublicKey publicKey) {

        return NimbusJwtDecoder
                .withPublicKey(publicKey)
                .build();
    }
}