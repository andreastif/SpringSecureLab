package com.auth.authserver2.controllers;

import com.auth.authserver2.domains.member.MemberDto;
import com.auth.authserver2.messages.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/")
//@EnableMethodSecurity
public class MemberController {

    //todo: Crud + method level security (https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)

    //todo: Register + e-mail verification

    //todo: client can only be registered after User for THIS SERVER (!= Users for OTHER clients) has been registered

    //Todo: this server must has its own client in the frontend for login, logout and register to function.

    //Todo: use pagination if getting all users for a certain client

    private final MemberUserDetailService memberUserDetailService;

    @Autowired
    public MemberController(MemberUserDetailService memberUserDetailService) {
        this.MemberUserDetailService = memberUserDetailService;
    }


    @GetMapping("members")
    public ResponseEntity<MemberDto> getMemberByEmail(@RequestParam String email) {
        return new ResponseEntity<>(memberUserDetailService.getMemberByEmail(email), HttpStatus.OK);
    }

    @PostMapping("users") //anyone can register, post just need to come from an approved client with the clientId!
    public ResponseEntity<ResponseMessage> registerNewMember(@RequestBody MemberDto newMember) {
        //todo: add e-mail verification for creating account

        var responseMessage = memberUserDetailService.save(newMember);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    @DeleteMapping("users")
    public ResponseEntity<ResponseMessage> deleteMemberByUsername(@RequestParam String email) {
        //todo: add e-mail verification for deleting account

        var responseMessage = memberUserDetailService.deleteMemberByEmail(email);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }


    @PutMapping("users")
    public ResponseEntity<ResponseMessage> updateMemberInformation(@RequestBody MemberDto newMember) {
        return null;
    }

}

