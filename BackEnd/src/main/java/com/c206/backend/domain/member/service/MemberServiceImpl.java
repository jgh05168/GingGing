package com.c206.backend.domain.member.service;

import com.c206.backend.domain.achievement.dto.response.MemberAchievementListResponseDto;
import com.c206.backend.domain.achievement.entity.Achievement;
import com.c206.backend.domain.achievement.entity.MemberAchievement;
import com.c206.backend.domain.achievement.repository.AchievementRepository;
import com.c206.backend.domain.achievement.repository.MemberAchievementRepository;
import com.c206.backend.domain.achievement.service.MemberAchievementService;
import com.c206.backend.domain.member.dto.response.MemberInfoResponseDto;
import com.c206.backend.domain.member.dto.response.MemberTrashCountResDto;
import com.c206.backend.domain.member.entity.Member;
import com.c206.backend.domain.member.entity.MemberInfo;
import com.c206.backend.domain.member.exception.MemberError;
import com.c206.backend.domain.member.exception.MemberException;
import com.c206.backend.domain.member.repository.MemberInfoRepository;
import com.c206.backend.domain.pet.dto.response.MemberPetDetailResponseDto;
import com.c206.backend.domain.pet.dto.response.MemberPetListResponseDto;
import com.c206.backend.domain.pet.dto.response.PetListResponseDto;
import com.c206.backend.domain.pet.entity.MemberPet;
import com.c206.backend.domain.pet.entity.Pet;
import com.c206.backend.domain.pet.exception.PetError;
import com.c206.backend.domain.pet.exception.PetException;
import com.c206.backend.domain.pet.repository.MemberPetRepository;
import com.c206.backend.domain.pet.repository.PetRepository;
import com.c206.backend.domain.pet.service.MemberPetService;
import com.c206.backend.domain.pet.service.MemberPetServiceImpl;
import com.c206.backend.domain.quest.exception.MemberQuestException;
import com.c206.backend.domain.quest.service.QuestService;
import com.c206.backend.global.jwt.CustomUserDetailsService;
import com.c206.backend.domain.member.dto.request.SignInRequestDto;
import com.c206.backend.domain.member.dto.request.SignUpRequestDto;
import com.c206.backend.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.IThrottledTemplateProcessor;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final AchievementRepository achievementRepository;
    private final MemberAchievementRepository memberAchievementRepository;
    private final MemberPetService memberPetService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final QuestService questService;
    private final RedisService redisService;

    @Override
    public Boolean signUpProcess(SignUpRequestDto signupDto) {

        String email = signupDto.getEmail();
        String password = signupDto.getPassword();

        Optional<Member> isExist = memberRepository.findByEmail(email);

        if(isExist.isPresent()){
            System.out.println("이미 존재하는 사용자입니다.");
            throw new MemberException(MemberError.EXIST_MEMBER_EMAIL);
        }else{
            System.out.println("회원가입 가능");
            //회원가입
            signupDto.setPassword(bCryptPasswordEncoder.encode(password));
            memberRepository.save(signupDto.toEntity());
            System.out.println("회원가입 성공");

            Optional<Member> nowMember = memberRepository.findByEmail(email);
            if(nowMember.isEmpty()){
                return false;
            }

            System.out.println("기본펫 지급시작");
            //기본펫 1번 지급
            Member member = memberRepository.findById(nowMember.get().getId()).orElseThrow(()
                    -> new MemberException(MemberError.NOT_FOUND_MEMBER));
            memberPetService.provideBasePet(member);

            List<MemberPetListResponseDto> memberPetList = memberPetService.getMemberPetList(member.getId());

            redisService.setValues("latest pet id "+ nowMember.get().getId(), String.valueOf(memberPetList.get(0).getMemberPetId()), 14*24*60*60*1000L);
            System.out.println("기본펫 지급성공");

            System.out.println("회원정보 지정시작");
            //회원-정보 테이블에 기본사항 지정하기
            MemberInfo newMemberInfo= MemberInfo.builder()
                    .member(member)
                    .profilePetId(0L)
                    .exp(0)
                    .currency(0)
                    .build();
            memberInfoRepository.save(newMemberInfo);
            System.out.println("회원정보 지정성공");

            System.out.println("회원업적 지정시작");
            //회원-업적 테이블에 기본사항 지정하기
            List<Achievement> achievementList = achievementRepository.findAll();

            for (Achievement achievement : achievementList) {
                MemberAchievement newMemberAchievement = MemberAchievement.builder()
                        .member(member)
                        .achievement(achievement)
                        .progress(0)
                        .goalMultiply(1)
                        .build();
                memberAchievementRepository.save(newMemberAchievement);
            }
            System.out.println("회원업적 지정성공");

            System.out.println("회원퀘스트 지정시작");
            questService.addQuestList(member.getId());
            System.out.println("회원퀘스트 지정성공");

            return true;
        }
    }

    @Override
    public Boolean signInProcess(SignInRequestDto signInDto) {

        return false;
    }

    @Override
    public Boolean emailDupCheck(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Override
    public MemberTrashCountResDto getMemberInfo(Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(()->
                new MemberException(MemberError.NOT_FOUND_MEMBER));

        MemberInfo memberInfo = memberInfoRepository.findTopByMemberIdOrderByIdDesc(memberId);

        List<MemberPetListResponseDto> memberPetList = memberPetService.getMemberPetList(memberId);
        int totalTrashNormal = 0, totalTrashPlastic = 0, totalTrashCan = 0, totalTrashGlass = 0;
        for(MemberPetListResponseDto memberPetItem : memberPetList){
            MemberPetDetailResponseDto memberPetDetail = memberPetService.getMemberPetDetail(memberId, memberPetItem.getMemberPetId(), false);
            totalTrashNormal += memberPetDetail.getNormal();
            totalTrashNormal += memberPetDetail.getPlastic();
            totalTrashCan += memberPetDetail.getCan();
            totalTrashGlass += memberPetDetail.getGlass();
        }

        return new MemberTrashCountResDto(
                member.getEmail(),
                member.getNickname(),
                memberInfo.getProfilePetId(),
                memberInfo.getExp()/1000,
                memberInfo.getCurrency(),
                totalTrashNormal,
                totalTrashPlastic,
                totalTrashCan,
                totalTrashGlass
        );
    }

    @Override
    public boolean updateMemberInfoPicture(Long memberId, Long profilePetId) {
        MemberInfo memberInfo;
        try{
            memberInfo = memberInfoRepository.findTopByMemberIdOrderByIdDesc(memberId);
        }catch (Exception e){
            throw new MemberException(MemberError.NOT_FOUND_MEMBER);
        }


        List<PetListResponseDto> petList = memberPetService.getPetList(memberId);
        try{
            petList = memberPetService.getPetList(memberId);
        }catch (Exception e){
            throw new PetException(PetError.NOT_FOUND_MEMBER_PET);
        }


        for(PetListResponseDto petItem : petList){
            System.out.println(petItem.getName()+" "+petItem.isHave()+" "+ petItem.isActive());
        }

        if(profilePetId <= 0 || profilePetId > 4){
            throw new PetException(PetError.NOT_FOUND_PET);
        }

        if(!petList.get(profilePetId.intValue() - 1).isActive()){
            throw new PetException(PetError.NOT_ACTIVE_PET);
        }



        memberInfo.updateProfilePetId(profilePetId);
        memberInfoRepository.save(memberInfo);
        return true;
    }

    @Override
    public boolean updateMemberTutorial(Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(()
                -> new MemberException(MemberError.NOT_FOUND_MEMBER)
        );
        member.updateMemberTutorial();

        return true;
    }


}
