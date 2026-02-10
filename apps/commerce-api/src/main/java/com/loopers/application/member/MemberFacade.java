package com.loopers.application.member;

import com.loopers.domain.member.Member;
import com.loopers.domain.member.MemberRepository;
import com.loopers.domain.member.MemberService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MemberFacade {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @Transactional
    public Member signup(MemberService.SignupCommand command) {
        return memberService.signup(command);
    }

    @Transactional
    public void changePassword(Member member, String currentPassword, String newPassword) {
        Member managedMember = memberRepository.findByMemberIdValue(member.getMemberId().getValue())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "회원을 찾을 수 없습니다."));
        memberService.changePassword(managedMember, currentPassword, newPassword);
    }
}
