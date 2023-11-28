package com.auth.authserver2.utils;


import com.auth.authserver2.domains.member.MemberDto;
import com.auth.authserver2.domains.member.MemberEntity;
import com.auth.authserver2.domains.member.MemberUpdateDto;
import com.auth.authserver2.messages.ResponseMessage;
import com.auth.authserver2.repositories.MemberRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@NoArgsConstructor
@Component
public class MemberUtil {

    private static MemberRepository memberRepository;

    @Autowired
    public MemberUtil(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
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
                        field.set(memberEntity, ((String) value).toLowerCase());
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

        if (memberRepository.findMemberEntityByEmail(memberDto.getId()).isPresent() || memberRepository.findMemberEntityByUsername(memberDto.getUsername()).isPresent()) {
            return new ResponseMessage(false, "Member already exists");
        }

        dtoMap.forEach((key, value) -> {
            try {
                Field field = memberDto.getClass().getDeclaredField(key);
                field.setAccessible(true);
                if (field.getType().equals(String.class)) {
                    String stringValue = (String) value;
                    if (stringValue == null) {
                        throw new NoSuchFieldException("MemberDto values cannot be null");
                    }
                    if (!stringValue.toLowerCase().matches("^[^\\\\s]{2,44}$")) {
                        throw new NoSuchFieldException("MemberDto values cannot be less than 2 characters or greater than 44");
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
