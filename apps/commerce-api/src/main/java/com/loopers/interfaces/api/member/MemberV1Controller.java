package com.loopers.interfaces.api.member;

import com.loopers.domain.member.Member;
import com.loopers.domain.member.MemberService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberV1Controller {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ApiResponse<MemberV1Dto.SignupResponse> signup(@RequestBody MemberV1Dto.SignupRequest request) {
        Member member = memberService.signup(request.toCommand());
        return ApiResponse.success(MemberV1Dto.SignupResponse.from(member));
    }
}
