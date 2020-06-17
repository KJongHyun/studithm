package com.studithm;


import com.studithm.account.AccountService;
import com.studithm.account.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AppRunner implements ApplicationRunner {

    private final AccountService accountService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("======================= jonghyeon 계정 초기화 =======================");
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setEmail("whdgus8219@naver.com");
        signUpForm.setNickname("jonghyeon");
        signUpForm.setPassword("12345678");

        System.out.println(signUpForm.toString());
        System.out.println("======================= jonghyeon 계정 초기화 완료 =======================");

        accountService.processNewAccount(signUpForm);
    }
}
