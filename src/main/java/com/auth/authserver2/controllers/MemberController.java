package com.auth.authserver2.controllers;

import com.auth.authserver2.domains.member.MemberDto;
import com.auth.authserver2.messages.ResponseMessage;
import com.auth.authserver2.services.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/")
@EnableMethodSecurity
public class MemberController {

    //todo: Crud + method level security (https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)

    //todo: Register + e-mail verification


    @Qualifier("memberService")
    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("members")
    public ResponseEntity<?> getMemberByEmail(@RequestParam String email) {

        Optional<MemberDto> member = memberService.getMemberByEmail(email);

        if (member.isPresent()) {
            return new ResponseEntity<>(member.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseMessage(false, "Member could not be found"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("members")
    public ResponseEntity<ResponseMessage> registerNewMember(@RequestBody MemberDto newMember) {
        log.info("Accessing api/v1/members registerNewMember @PostMapping");
        //todo: add e-mail verification for creating account
        var responseMessage = memberService.save(newMember);

        if (responseMessage.isSuccessful()) {
            return new ResponseEntity<>(responseMessage, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("members/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        log.info("Accessing api/v1/members/login login @PostMapping");
        return new ResponseEntity<>(memberService.loginUser(username, password), HttpStatus.OK);
    }


    @DeleteMapping("members")
    @PreAuthorize("hasRole('ROLE_ADMIN')") //works!
    public ResponseEntity<ResponseMessage> deleteMemberByUsername(@RequestParam String email) {
        log.info("Accessing api/v1/members deleteMemberByUsername @DeleteMapping");

        //todo: add e-mail verification for deleting account
        var responseMessage = memberService.deleteMemberByEmail(email);
        if (responseMessage.isSuccessful()) {
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
        }
    }


    @PutMapping("members")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostFilter("#member.firstname = @securityConditions.checkMemberId()") //todo: add memberId field to DTO so we can populate it! p.s this works!
    public ResponseEntity<ResponseMessage> updateMemberCredentials(@RequestBody MemberDto member) {
        log.info("Accessing api/v1/members updateMemberCredentiials @PutMapping");
        log.info("I SHOULD BE REPLACED AND POPULATE THE FUTURE MEMBERID INSIDE THIS CLASS INSTEAD {}", member.getFirstname()); //todo: temporary
        var responseMessage = memberService.updateMemberCredentials(member);
        if (responseMessage.isSuccessful()) {
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
        }
    }


}

