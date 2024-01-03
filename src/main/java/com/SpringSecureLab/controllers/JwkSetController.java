package com.SpringSecureLab.controllers;

import com.SpringSecureLab.utils.RSAKeyProperties;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JwkSetController {

    private final RSAKeyProperties keyProperties;

    @Autowired
    public JwkSetController(RSAKeyProperties keyProperties) {
        this.keyProperties = keyProperties;
    }

    //everytime the server is restarted, new keys are created
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> keys() {
        RSAKey.Builder builder = new RSAKey.Builder(keyProperties.getPublicKey());
        JWKSet jwkSet = new JWKSet(builder.build());
        return jwkSet.toJSONObject();
    }
}
