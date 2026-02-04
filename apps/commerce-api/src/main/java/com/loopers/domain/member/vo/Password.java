package com.loopers.domain.member.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Password {
    private String value;

    public Password(String pw, String birth) {
        validate(pw, birth);
        this.value = pw;
    }

    private void validate(String pw, String birth) {
        if(pw == null || pw.length() < 8 || pw.length() > 16) {
            throw new CoreException(ErrorType.BAD_REQUEST, "비밀번호는 8~16자여야 합니다.");
        }

        String regex = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).+$";
        if (!pw.matches(regex)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.");
        }

        checkBirthPatterns(pw, birth);
    }

    private void checkBirthPatterns(String rawPassword, String birth) {
        String cleanBirth = birth.replaceAll("-", "");
        if (cleanBirth.length() != 8) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일 형식이 올바르지 않습니다.");
        }

        String year = cleanBirth.substring(0, 4);
        String month = cleanBirth.substring(4, 6);
        String day = cleanBirth.substring(6, 8);

        String yy = year.substring(2);
        String mmdd = month + day;

        String[] blackListPatterns = {
                year,
                yy,
                mmdd,
                yy + mmdd,
                year + mmdd
        };

        for (String pattern : blackListPatterns) {
            if (rawPassword.contains(pattern)) {
                throw new CoreException(ErrorType.BAD_REQUEST, "비밀번호에 생년월일과 관련된 숫자(" + pattern + ")를 포함할 수 없습니다.");
            }
        }
    }
}
