# Authentication Flow - Spring Security + JWT

## Overview

This project uses **stateless JWT authentication**. No sessions are stored on the server. The client gets a token on login and sends it with every request.

---

## The Login Flow

### What happens when a user hits `POST /Users/login`

```
Client sends { email, password }
    |
    v
UserController.login()
    |
    |-- Creates UsernamePasswordAuthenticationToken(email, password)
    |   This is an UNAUTHENTICATED token (2-arg constructor).
    |   It's a claim: "I am this person, here's my proof."
    |
    |-- Passes it to authenticationManager.authenticate(authToken)
    |       |
    |       |-- Spring internally uses DaoAuthenticationProvider which:
    |       |
    |       |-- Calls UserDetailsImplService.loadUserByUsername(email)
    |       |       |
    |       |       |-- Queries DB: userRepository.findByEmail(email)
    |       |       |-- Returns UserDetailsImpl(email, hashedPassword, role)
    |       |
    |       |-- Calls passwordEncoder.matches(rawPassword, hashedPassword)
    |       |       |
    |       |       |-- If NO match  -> throws BadCredentialsException (401)
    |       |       |-- If match     -> continues
    |       |
    |       |-- Returns a NEW UsernamePasswordAuthenticationToken
    |           with 3 args (principal, credentials, authorities)
    |           This one IS authenticated.
    |
    |-- Extracts UserDetailsImpl from authentication.getPrincipal()
    |
    |-- Calls jwtUtils.generateToken(email, role)
    |       |
    |       |-- Jwts.builder()
    |       |       .setSubject(email)           // who this token is for
    |       |       .addClaims(Map.of("role"...)) // custom data
    |       |       .setIssuedAt(now)
    |       |       .setExpiration(now + 24hrs)
    |       |       .signWith(secretKey)          // signs the token
    |       |       .compact()                    // produces "eyJ..." string
    |
    |-- Returns { "token": "eyJ...", "role": "USER" } to client
```

### Key concept: UsernamePasswordAuthenticationToken has two constructors

| Constructor | Meaning | When used |
|---|---|---|
| `(principal, credentials)` — 2 args | Unauthenticated. "Here's who I claim to be." | During login, before verification |
| `(principal, credentials, authorities)` — 3 args | Authenticated. "This person is verified." | After AuthenticationManager verifies, or in the JWT filter |

---

## How AuthenticationManager Knows Your Implementation

You don't wire it manually. Spring auto-configuration does this:

```
AuthenticationManager
    └── DaoAuthenticationProvider (auto-configured by Spring)
            ├── UserDetailsService  -> finds your UserDetailsImplService
            |                          (the only bean implementing this interface)
            └── PasswordEncoder     -> finds your BCryptPasswordEncoder @Bean
```

Spring sees your `@Service` class that implements `UserDetailsService` and automatically plugs it into the authentication chain. If you had two `UserDetailsService` beans, Spring would throw an error asking you to specify which one.

---

## The JWT Filter Flow (Every Future Request)

### What happens when a user hits any protected endpoint

```
Client sends request with header:
    Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
    |
    v
JwtAuthenticationFilter.doFilterInternal()
    |
    |-- Reads "Authorization" header
    |
    |-- No header or doesn't start with "Bearer "?
    |       -> filterChain.doFilter() (let Spring reject as unauthenticated)
    |
    |-- Extracts token: authHeader.substring(7)
    |
    |-- Calls jwtUtils.getEmail(token)
    |       |
    |       |-- Internally calls getClaims(token) which calls:
    |       |
    |       |   Jwts.parserBuilder()
    |       |       .setSigningKey(secretKey)   // use same key that signed it
    |       |       .build()
    |       |       .parseClaimsJws(token)      // THIS IS THE VERIFICATION
    |       |
    |       |   parseClaimsJws() does THREE checks in one call:
    |       |
    |       |   1. SIGNATURE CHECK
    |       |      Takes header+payload from token, re-signs with secretKey,
    |       |      compares with the signature in the token.
    |       |      If tampered -> SignatureException
    |       |
    |       |   2. EXPIRATION CHECK
    |       |      Reads "exp" claim, compares to current time.
    |       |      If expired -> ExpiredJwtException
    |       |
    |       |   3. FORMAT CHECK
    |       |      If malformed/garbage -> MalformedJwtException
    |       |
    |       |-- If all pass -> returns email from subject claim
    |
    |-- Calls userDetailsImplService.loadUserByUsername(email)
    |       -> loads user from DB to get current authorities
    |
    |-- Creates UsernamePasswordAuthenticationToken(userDetails, null, authorities)
    |   This is the 3-arg constructor -> AUTHENTICATED.
    |   credentials is null because we don't need the password anymore,
    |   the JWT already proved identity.
    |
    |-- SecurityContextHolder.getContext().setAuthentication(authToken)
    |   Stores the authenticated user for the duration of this request.
    |   Any code can now call SecurityContextHolder.getContext().getAuthentication()
    |   to get the current user.
    |
    |-- filterChain.doFilter() -> request proceeds to the controller
```

### JWT Structure

A JWT token has three parts separated by dots:

```
eyJhbGciOi...  .  eyJzdWIiOi...  .  abc123signature
    HEADER            PAYLOAD          SIGNATURE

Header:  { "alg": "HS256" }
Payload: { "sub": "prashant@test.com", "role": "USER", "iat": ..., "exp": ... }
Signature: HMAC-SHA256(header + "." + payload, secretKey)
```

The payload is Base64 encoded (NOT encrypted) — anyone can decode and read it. The signature is what prevents tampering. If someone changes the payload, the signature won't match when the server re-computes it.

---

## What's NOT Implemented (Future Improvements)

### Refresh Token + Access Token Pattern

Currently we have a single token that lasts 24 hours. The problem: if the token is stolen, the attacker has access for 24 hours. If we shorten the expiry to 15 minutes, the user has to log in constantly.

The solution is two tokens:

```
ACCESS TOKEN:   Short-lived (15 min). Sent with every request.
REFRESH TOKEN:  Long-lived (7 days). Stored securely. Used only to get a new access token.
```

Flow:
```
1. Login -> server returns { accessToken (15min), refreshToken (7 days) }
2. Client uses accessToken for all API calls
3. accessToken expires after 15 min
4. Client sends refreshToken to POST /Users/refresh
5. Server validates refreshToken, issues NEW accessToken
6. If refreshToken is also expired -> user must log in again
```

Why this is better:
- If accessToken is stolen, attacker only has 15 minutes
- refreshToken is sent rarely (only to one endpoint), reducing exposure
- Server can revoke refreshTokens (store them in DB/Redis and delete to force logout)
- accessToken can be stateless (no DB check), refreshToken can be stateful (stored in DB)

### Role-Based Authorization

Currently `UserDetailsImplService` hardcodes `"USER"` role. To support ADMIN:
- Add a `role` field to the `User` entity
- Read it in `loadUserByUsername()` instead of hardcoding
- In `SecurityConfig`, restrict endpoints by role:
  ```java
  .requestMatchers("/movies/add").hasRole("ADMIN")
  .requestMatchers("/booking/**").hasRole("USER")
  ```

### Password Hashing

Passwords must be hashed before storing in DB:
- Add `BCryptPasswordEncoder` as a `@Bean` in SecurityConfig
- In `UserService.saveUser()`: `user.setPassword(passwordEncoder.encode(rawPassword))`
- `AuthenticationManager` uses the same encoder to compare during login
