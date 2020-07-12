package com.studithm;


import com.studithm.account.AccountRepository;
import com.studithm.account.AccountService;
import com.studithm.account.SignUpForm;
import com.studithm.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class AppRunner implements ApplicationRunner {

    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (accountRepository.existsByNickname("test"))
            return;
        System.out.println("======================= jonghyeon 계정 초기화 =======================");
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setEmail("test@naver.com");
        signUpForm.setNickname("test");
        signUpForm.setPassword("12345678");
        Account account = accountService.processNewAccount(signUpForm);
        account.setEmailCheckTokenGeneratedAt(LocalDateTime.now().minusHours(1));
        accountRepository.save(account);
        System.out.println(signUpForm.toString());
        System.out.println("======================= jonghyeon 계정 초기화 완료 =======================");
    }
}
