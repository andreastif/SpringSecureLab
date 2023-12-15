package com.auth.authserver2.controllers;

import com.auth.authserver2.domains.member.MemberCheckSessionDto;
import com.auth.authserver2.domains.member.MemberDto;
import com.auth.authserver2.domains.member.MemberLoginDto;
import com.auth.authserver2.domains.member.MemberUpdateDto;
import com.auth.authserver2.messages.ResponseMessage;
import com.auth.authserver2.services.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/")
@EnableMethodSecurity
public class MemberController {

    //todo: Create ACTUAL refresh token OR issue new JWT when one hits Refresh Token Endpoint with old token BEFORE expiry (if expired - have to log in again)


    private final JwtDecoder jwtDecoder;
    @Qualifier("memberService")
    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService, JwtDecoder jwtDecoder) {
        this.memberService = memberService;
        this.jwtDecoder = jwtDecoder;
    }

    @GetMapping("members")
    public ResponseEntity<?> getMemberByEmail(@RequestParam String email) {
        var member = memberService.getMemberByEmail(email);
        return new ResponseEntity<>(member, HttpStatus.OK);
    }

    @PostMapping("members")
    public ResponseEntity<ResponseMessage> registerNewMember(@RequestBody MemberDto newMember) {
        log.info("Accessing api/v1/members registerNewMember @PostMapping");
        var responseMessage = memberService.save(newMember);

        if (responseMessage.isSuccessful()) {
            return new ResponseEntity<>(responseMessage, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
        }
    }

    /*
    The HttpServletResponse object represents the response to a request and is used to interact with HTTP response data, including headers, status codes, and cookies.
    The HttpServletResponse is something Spring will automatically provide when the method is invoked.
    You use this response object to add the cookie by calling response.addCookie(jwtCookie);.
    The loginUser method in the MemberService is assumed to return a Cookie object that is already configured with the JWT.

    Spring MVC operates on top of the Servlet API. Every HTTP request coming to your server is initially a HttpServletRequest, and every HTTP response is a HttpServletResponse.
    When you define a controller method with HttpServletResponse as a parameter, Spring injects the actual response object that corresponds to the incoming request into your method.
    Any changes you make to this HttpServletResponse object directly affect the HTTP response that will be sent back to the client.

    Method Parameter Injection: Injecting HttpServletResponse as a method parameter is the standard approach.
    It’s stateless and ensures that you're working with the correct response object associated with the current request.
    It's only available to the controller method that declares it, which is usually what you want.
     */
    @PostMapping("members/login")
    public ResponseEntity<?> login(@RequestBody MemberLoginDto member, HttpServletResponse response) {
        log.info("Accessing api/v1/members/login login @PostMapping");
        Cookie cookie = memberService.loginUser(member.getUsername(), member.getPassword());
        Jwt jwt =  jwtDecoder.decode(cookie.getValue());
        response.addCookie(cookie);
        return ResponseEntity.ok(jwt.getClaim("roles"));
    }

    @GetMapping("members/check-session")
    public ResponseEntity<MemberCheckSessionDto> checkSession() {
        var status = memberService.checkSession();
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @GetMapping("members/confirm")
    public ResponseEntity<?> confirmAccount(@RequestParam String token) {
        log.info("Accessing api/v1/members/confirmation @Getmapping");
        var response = memberService.confirmMember(token);
        if (response.isSuccessful()) {
            return new ResponseEntity<>(response.getMsg(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response.getMsg(), HttpStatus.NOT_FOUND);
        }
    }


    @DeleteMapping("members")
    @PreAuthorize("hasRole('ROLE_ADMIN')") //works!
    public ResponseEntity<ResponseMessage> deleteMemberByUsername(@RequestParam String email) {
        log.info("Accessing api/v1/members deleteMemberByUsername @DeleteMapping");
        var responseMessage = memberService.deleteMemberByEmail(email);
        if (responseMessage.isSuccessful()) {
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
        }
    }


    @PutMapping("members")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ResponseMessage> updateMemberCredentials(@RequestBody MemberUpdateDto member) {
        log.info("Accessing api/v1/members updateMemberCredentiials @PutMapping");
        var responseMessage = memberService.updateMemberCredentials(member);
        if (responseMessage.isSuccessful()) {
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
        }
    }


}

