package com.fis.epm.utils;

import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSASigner;
import io.fusionauth.jwt.rsa.RSAVerifier;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultClock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.fis.epm.prop.EPMApiConstant;
import com.fis.pg.common.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Component
public class JwtProvider {
	@Value(EPMApiConstant.JWT_PUBLIC_KEY_PROP)
    protected String PUBLIC_KEY_FILE;
	@Value(EPMApiConstant.JWT_PRIVATE_KEY_PROP)
    protected String PRIVATE_KEY_FILE;

    @Autowired
    Environment env;

    private Clock clock = DefaultClock.INSTANCE;

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .setSigningKey(env.getProperty("app.jwt-secret", ""))
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    public Boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(env.getProperty("app.jwt-secret", "")).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature -> Message: {} ", e);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token -> Message: {}", e);
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token -> Message: {}", e);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token -> Message: {}", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty -> Message: {}", e);
        }

        return false;
    }

    public String getStringKey(String keyName) {
        String data = "";
        ClassPathResource cpr = new ClassPathResource(keyName);
        try {
            byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
            data = new String(bdata, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("IOException", e);
        }
        return data;
    }


    public String generateAccessTokenRSA(String username, Long userId, String orgId, Long deptId, Set<String> groups,String timeOut, String subId) {
        Signer signer = null;
        try {
            signer = RSASigner.newSHA256Signer(getStringKey(PRIVATE_KEY_FILE));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        JWT jwt = new JWT()
                .setIssuedAt(now)
                .setSubject(username)
                .setExpiration(now.plusSeconds(Integer.valueOf(StringUtils.nvl(timeOut,"1800")).intValue()))
                .addClaim("userId", userId)
                .addClaim("userName", username)
                .addClaim("orgId", orgId)
                .addClaim("groups", groups)
                .addClaim("deptId", deptId)
                .addClaim("subId", subId);
        String encodedJWT = JWT.getEncoder().encode(jwt, signer);
        return encodedJWT;
    }

    public String generateRefreshToken(String token) {
        return Jwts.builder()
                .setSubject(getUserNameFromTokenRSA(token))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + Integer.valueOf(env.getProperty("app.jwt-refresh-expiration", "1200")).intValue() * 1000))
                .signWith(SignatureAlgorithm.HS512, env.getProperty("app.jwt-secret", ""))
                .compact();
    }

    public String generateRefreshToken(String token,String timeRefresh) {
        return Jwts.builder()
                .setSubject(getUserNameFromTokenRSA(token))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + Integer.valueOf(StringUtils.nvl(timeRefresh,"1200")).intValue() * 1000))
                .signWith(SignatureAlgorithm.HS512, env.getProperty("app.jwt-secret", ""))
                .compact();
    }

    private Object getValueInClaims(String token, String key){
        Map<String, Object> claims = getAllClaims(token);
        if (claims.containsKey(key)){
            return claims.get(key);
        }
        return null;
    }


    public String getUserNameFromTokenRSA(String token) {
        return StringUtils.nvl(getValueInClaims(getJwtRaw(token), "userName"), "");
    }

    public String getSubIdFromTokenRSA(String token) {
        return StringUtils.nvl(getValueInClaims(getJwtRaw(token), "subId"), "");
    }

    public String getUserIdFromTokenRSA(String token) {
        return StringUtils.nvl(getValueInClaims(getJwtRaw(token), "userId"), "0");
    }

    // chuyen shop -> org
    public String getdeptIdFromTokenRSA(String token) {
        //return getAllClaims(token).get("shopCode").toString();
        //hanx sua cho phu hop db
        return StringUtils.nvl(getValueInClaims(getJwtRaw(token), "deptId"), "0");
    }

    public Long getDeptIdFromTokenRSA(String token) {
        //return getAllClaims(getJwtRaw(token)).get("shopCode").toString();
        //hanx sua cho phu hop DB moi
        return Long.parseLong(StringUtils.nvl(getValueInClaims(getJwtRaw(token), "deptId"), "0"));
    }

    public Long getOrgIdFromTokenRSA(String token) {
        //return getAllClaims(getJwtRaw(token)).get("shopCode").toString();
        //hanx sua cho phu hop DB moi
        return Long.parseLong(StringUtils.nvl(getValueInClaims(getJwtRaw(token), "orgId"), "0"));
    }

    public ArrayList<String> getLstGroupFromTokenRSA(String token) {
        return (ArrayList<String>) getAllClaims(getJwtRaw(token)).get("groups");
    }

    public Date getIssuedAtDateFromTokenRSA(String token) {
        return (Date) getAllClaims(token).get("iat");
    }

    public Date getExpirationDateFromTokenRSA(String token) {
        return (Date) getAllClaims(token).get("exp");
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromTokenRSA(token);
        return expiration.before(clock.now());
    }

    private Boolean ignoreTokenExpiration(String token) {
        // here you specify tokens, for that the expiration is ignored
        return false;
    }

    private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
        if (lastPasswordReset == null)
            return false;

        return (lastPasswordReset != null && created.before(lastPasswordReset));
    }

    public Boolean canTokenBeRefreshed(String token, Date lastPasswordReset) {
        final Date created = getIssuedAtDateFromTokenRSA(token);
        return !isCreatedBeforeLastPasswordReset(created, lastPasswordReset)
                && (!isTokenExpired(token) || ignoreTokenExpiration(token));
    }

    public int getJwtExpiration() {
        return Integer.valueOf(env.getProperty("app.jwt-expiration", "1800")).intValue();
    }

    public int getJwtRefreshExpiration() {
        return Integer.valueOf(env.getProperty("app.jwt-refresh-expiration", "1200")).intValue();
    }

    public String getJwtRaw(String authorizationHeader) {
        return StringUtils.isNullOrEmpty(authorizationHeader)?null:authorizationHeader.replaceAll("Bearer", "").trim();
    }

    public String generateAccessTokenRSAExpiration100Year(String username, Long userId, String orgId, Long deptId, Set<String> groups) {
        Signer signer = null;
        try {
            signer = RSASigner.newSHA256Signer(getStringKey(PRIVATE_KEY_FILE));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

//        JWT jwt = new JWT()
//                .setIssuedAt(now)
//                .setSubject(username)
//                .setExpiration(now.plusSeconds(jwtExpiration))
//                .addClaim("userId", userId)
//                .addClaim("shopCode", shopCode)
//                .addClaim("groups", groups)
//                .addClaim("centerCode", centerCode)
//                .addClaim("shopType", shopType)
//                .addClaim("shopId", shopId)
//                .addClaim("provinceCode", provinceCode)
//                .addClaim("staffName", staffName)
//                .addClaim("stockShopId", stockShopId);
        JWT jwt = new JWT()
                .setIssuedAt(now)
                .setSubject(username)
                .setExpiration(now.plusYears(100))
                .addClaim("userId", userId)
                .addClaim("userName", username)
                .addClaim("orgId", orgId)
                .addClaim("groups", groups)
                .addClaim("deptId", deptId);
        String encodedJWT = JWT.getEncoder().encode(jwt, signer);
        return encodedJWT;
    }

    public String getTokenAPI(String username) {
        Signer signer = null;
        try {
            signer = RSASigner.newSHA256Signer(getStringKey(PRIVATE_KEY_FILE));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        JWT jwt = new JWT()
                .setIssuedAt(now)
                .setSubject(username)
                .setExpiration(now.plusSeconds(Integer.valueOf(env.getProperty("app.jwt-expiration", "1800")).intValue()))
                .addClaim("userName", username);
        String encodedJWT = JWT.getEncoder().encode(jwt, signer);
        return encodedJWT;
    }

    public Boolean validateTokenRSA(String authToken) {
        try {
            Verifier verifier = RSAVerifier.newVerifier(getStringKey(PUBLIC_KEY_FILE));
            JWT.getDecoder().decode(authToken, verifier);
            return true;
        } catch (Exception e) {
            log.error("Invalid token: " + e.getMessage());
        }
        return false;
    }

    public Map<String, Object> getAllClaims(String token) {
        try {
            Verifier verifier = RSAVerifier.newVerifier(getStringKey(PUBLIC_KEY_FILE));
            JWT jwt = JWT.getDecoder().decode(token, verifier);
            return jwt.getAllClaims();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return new HashMap<>();
    }

    public String getJwt(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.replace("Bearer ", "");
        }

        return null;
    }

    protected Date getExpirationDateFromToken(String token, boolean useRSA) {
        if (useRSA) {
            return Date.from(((ZonedDateTime) getAllClaims(token).get(Claims.EXPIRATION)).toInstant());
        } else {
            return getClaimFromToken(token, Claims::getExpiration);
        }
    }

    protected boolean isTokenExpired(String token, boolean useRSA) {
        final Date expiration = getExpirationDateFromToken(token, useRSA);
        return expiration.before(new Date());
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    //for retrieveing any information from token we will need the secret key
    protected Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(env.getProperty("app.jwt-secret", "")).parseClaimsJws(token).getBody();
    }

    protected boolean isValidToken(String authToken, boolean withExpireTime, boolean useRSA) {
        if (useRSA) {
            return (withExpireTime && isValidToken(authToken) && !isTokenExpired(authToken, useRSA))
                    || (!withExpireTime && isValidToken(authToken));
        } else {
            return (withExpireTime && isValidToken(authToken) && !isTokenExpired(authToken, useRSA))
                    || (!withExpireTime && isValidToken(authToken));
        }
    }

    public boolean isValidToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(env.getProperty("app.jwt-secret", "")).parseClaimsJws(authToken);
            return true;
        } catch (Exception e) {
            log.error("Invalid token >>> Cannot parse secret-key token - " + e.getMessage());
        }
        return false;
    }

    public String getSubjectToken(String token) {
        try {
            Verifier verifier = RSAVerifier.newVerifier(getStringKey(PUBLIC_KEY_FILE));
            return JWT.getDecoder().decode(token, verifier).subject;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return "";
    }

    public Jwt<Header, Claims> decodeWithoutKey(String token) {
        int i = token.lastIndexOf('.');
        String withoutSignature = token.substring(0, i + 1);
        return Jwts.parser().parseClaimsJwt(withoutSignature);
    }
}
