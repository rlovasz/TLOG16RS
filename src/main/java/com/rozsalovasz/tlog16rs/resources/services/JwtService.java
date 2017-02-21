/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rozsalovasz.tlog16rs.resources.services;

import com.rozsalovasz.tlog16rs.entities.User;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

/**
 *
 * @author precognox
 */
public class JwtService {

    private final String secret = "dZaiwdfFsD1daUYnTXnG";
    private final int expirationTimeInMinutes = 5;

    /**
     * Generates JWT for the user
     * @param user
     * @return with the generated token
     * @throws UnsupportedEncodingException
     * @throws JoseException 
     */
    public String generateJwtToken(User user) throws UnsupportedEncodingException, JoseException {
        JwtClaims claims = new JwtClaims();
        JsonWebSignature jws = new JsonWebSignature();
        Key key = new HmacKey(secret.getBytes("UTF-8"));
        String jwt;
        claims.setExpirationTimeMinutesInTheFuture(expirationTimeInMinutes);
        claims.setSubject(user.getName());
        jws.setPayload(claims.toJson());
        jws.setKey(key);
        jws.setKeyIdHeaderValue("kid");
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
        jws.setDoKeyValidation(false);
        jwt = jws.getCompactSerialization();
        return jwt;
    }

    /**
     * Validates the token and gives the name of the user
     * @param token
     * @return with the name of the user
     * @throws InvalidJwtException 
     */
    public String getNameFromJwtToken(String token) throws InvalidJwtException {
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setVerificationKey(new HmacKey(secret.getBytes()))
                .setRelaxVerificationKeyValidation()
                .setSkipSignatureVerification()
                .build();
        jwtConsumer.processContext(jwtConsumer.process(token));
        JwtClaims jwtClaims = jwtConsumer.processToClaims(token);
        String name = jwtClaims.getClaimValue("sub").toString();
        return name;
    }

}
