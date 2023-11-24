package com.auth.authserver2.controllers;

import com.auth.authserver2.domains.member.MemberDto;
import com.auth.authserver2.messages.ResponseMessage;
import com.auth.authserver2.services.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/")
@CrossOrigin(origins = "http://localhost:5173")
//@EnableMethodSecurity
public class MemberController {

    //todo: Crud + method level security (https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)

    //todo: Register + e-mail verification

    //todo: client can only be registered after User for THIS SERVER (!= Users for OTHER clients) has been registered

    //Todo: this server must has its own client in the frontend for login, logout and register to function.

    //Todo: use pagination if getting all users for a certain client


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

    @PostMapping("members") //anyone can register, post just need to come from an approved client with the clientId!
    public ResponseEntity<ResponseMessage> registerNewMember(@RequestBody MemberDto newMember) {

        //todo: add e-mail verification for creating account
        var responseMessage = memberService.save(newMember);

        if (responseMessage.isSuccessful()) {
            return new ResponseEntity<>(responseMessage, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("members")
    public ResponseEntity<ResponseMessage> deleteMemberByUsername(@RequestParam String email) {

        //todo: add e-mail verification for deleting account
        var responseMessage = memberService.deleteMemberByEmail(email);
        if (responseMessage.isSuccessful()) {
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
        }
    }


    @PutMapping("members")
    public ResponseEntity<ResponseMessage> updateMemberCredentials(@RequestBody MemberDto member) {

        var responseMessage = memberService.updateMemberCredentials(member);
        if (responseMessage.isSuccessful()) {
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
        }
    }

}

