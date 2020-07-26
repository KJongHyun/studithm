package com.studithm.modules.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountFactory {

    private final AccountRepository accountRepository;

    public Account createAccount(String nickname) {
        Account account = new Account();
        account.setNickname(nickname);
        account.setEmail(nickname + "@email.com");
        accountRepository.save(account);

        return account;
    }

}
