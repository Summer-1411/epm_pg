package com.fis.epm.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenPG {
    private String token;
    private Long expireDate;
}
