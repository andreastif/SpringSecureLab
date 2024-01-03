package com.SpringSecureLab.utils;


import com.SpringSecureLab.exceptions.CustomEmailValidationException;
import com.SpringSecureLab.exceptions.CustomFirstNameOrLastNameValidationException;
import com.SpringSecureLab.exceptions.CustomPasswordValidationException;
import com.SpringSecureLab.exceptions.CustomUsernameValidationException;
import com.SpringSecureLab.messages.ResponseMessage;
import com.SpringSecureLab.repositories.MemberRepository;
import com.SpringSecureLab.domains.member.MemberDto;
import com.SpringSecureLab.domains.member.MemberEntity;
import com.SpringSecureLab.domains.member.MemberUpdateDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;


@Slf4j
@NoArgsConstructor
@Component
public class MemberUtil {

    private static MemberRepository memberRepository;

    private static PasswordEncoder passwordEncoder;

    @Autowired
    public MemberUtil(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        MemberUtil.memberRepository = memberRepository;
        MemberUtil.passwordEncoder = passwordEncoder;
    }

    public static MemberEntity toExistingEntityWithUpdatedCredentials(MemberUpdateDto memberDto, MemberEntity memberEntity) {
        log.info("toExistingEntityWithUpdatedCredentials() called with memberDto {} and memberEntity {}", memberDto, memberEntity);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        //i Map<String, Object>, object = vi säger att vi tillåter att vi sparar av alla typer av objekt, typen bestäms under runtime (String/Boolean i detta fall)
        Map<String, Object> dtoMap = objectMapper.convertValue(memberDto, new TypeReference<>(){}); //skapar en hashmap av memberdto
        Map<String, Object> entityMap = objectMapper.convertValue(memberEntity, new TypeReference<>(){});

        //we care about username, email, firstname, lastname, password, accNonExp/NonLock/CredNonExp/enabl
        //rest will come from the old object (id, uuid, roles)

        dtoMap.forEach((key, value) -> {
            if (entityMap.containsKey(key)) {
                try {
                    var field = memberEntity.getClass().getDeclaredField(key);
                    field.setAccessible(true);

                    if (field.getType().equals(String.class) && value instanceof String) {
                        if (key.equals("password")) {
                            field.set(memberEntity, passwordEncoder.encode(((String) value)));
                        } else {
                            field.set(memberEntity, ((String) value).toLowerCase());
                        }
                    } else if ((field.getType().equals(boolean.class) || field.getType().equals(Boolean.class))
                            && value instanceof Boolean) {
                        field.setBoolean(memberEntity, (Boolean) value);
                    }
                    field.setAccessible(false); // Restore the original accessibility state
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException("Error updating field: " + key, e);
                }
            }
        });

        log.info("toExistingEntityWithUpdatedCredentials() finished, returning memberEntity {}", memberEntity);
        //this is the object we update with the new values.
        return memberEntity;
    }

    public static ResponseMessage validateMemberDto(MemberUpdateDto memberDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());


        Map<String, Object> dtoMap = objectMapper.convertValue(memberDto, new TypeReference<>(){});

        //search to see if the email or username the member wants to update to, already exists
        if (memberRepository.findMemberEntityByEmail(memberDto.getId()).isPresent() || memberRepository.findMemberEntityByUsername(memberDto.getUsername()).isPresent()) {
            return new ResponseMessage(false, "Sorry, could not register with the username and/or email. Try something else");
        }

        dtoMap.forEach((key, value) -> {
            try {
                Field field = memberDto.getClass().getDeclaredField(key);
                field.setAccessible(true);
                if (field.getType().equals(String.class)) {
                    String stringValue = (String) value;
                    if (stringValue == null) {
                        return; //skips validation for null values, which is ok
                    }
                    if (key.equals("username") && !stringValue.toLowerCase().matches("^[^\\s]{2,45}$")) {
                        throw new CustomUsernameValidationException("Username has to be between 2 and 45 characters");
                    }
                    if (key.equals("email") && !stringValue.toLowerCase().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                        throw new CustomEmailValidationException("Email has to be atleast 6 characters long and be of valid format i.e user@provider.com");
                    }
                    if (key.equals("password") && !stringValue.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
                        throw new CustomPasswordValidationException("Password has to be at least 8 characters in length, contain one uppercase and lowercase letter, one digit and one special character.");
                    }
                    if (key.equals("firstname") || key.equals("lastname") && !stringValue.toLowerCase().matches("^[A-Za-z]{2,}$")) {
                        throw new CustomFirstNameOrLastNameValidationException("First and lastnames must be at least 2 latin characters with no special characters or digits");
                    }
                }
                field.setAccessible(false);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Error accessing field: " + key, e);
            }
        });

        return new ResponseMessage(true, "Validation successful");
    }

    public static MemberEntity toNewEntity(MemberDto memberDto) {
        return MemberEntity
                .builder()
                .uuid(UUID.randomUUID())
                .username(memberDto.getUsername().toLowerCase())
                .email(memberDto.getEmail().toLowerCase())
                .created(Instant.now())
                .lastUpdated(Instant.now())
                .password(memberDto.getPassword())
                .firstname(memberDto.getFirstname().toLowerCase())
                .lastname(memberDto.getLastname().toLowerCase())
                .registeredToClientId(memberDto.getRegisteredToClientId().toLowerCase())
                .accountNonExpired(memberDto.isAccountNonExpired())
                .accountNonLocked(memberDto.isAccountNonLocked())
                .credentialsNonExpired(memberDto.isCredentialsNonExpired())
                .enabled(memberDto.isEnabled())
                .build();
    }

    public static MemberDto toDto(MemberEntity memberEntity) {
        return MemberDto.builder()
                .username(memberEntity.getUsername())
                .email(memberEntity.getEmail())
                .firstname(memberEntity.getFirstname())
                .lastname(memberEntity.getLastname())
                //Password NOT included for safety reasons
//                .password(memberEntity.getPassword())
                .memberRoles(RoleUtil.toRoleSet(memberEntity.getMemberRoles()))
                .enabled(memberEntity.isEnabled())
                .accountNonExpired(memberEntity.isAccountNonExpired())
                .registeredToClientId(memberEntity.getRegisteredToClientId())
                .credentialsNonExpired(memberEntity.isCredentialsNonExpired())
                .accountNonLocked(memberEntity.isAccountNonLocked())
                .build();
    }


}
