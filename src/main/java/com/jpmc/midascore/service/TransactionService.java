// TransactionService.java
package com.jpmc.midascore.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRecordRepository;

@Service
public class TransactionService {
    private final UserRecordRepository userRepo;
    private final TransactionRecordRepository transactionRepo;

    public TransactionService(UserRecordRepository userRepo, TransactionRecordRepository transactionRepo) {
        this.userRepo = userRepo;
        this.transactionRepo = transactionRepo;
    }

    public void process(Transaction t) {
        Optional<UserRecord> senderOpt = userRepo.findById(t.getSenderId());
        Optional<UserRecord> recipientOpt = userRepo.findById(t.getRecipientId());

        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
            return; // invalid users
        }

        UserRecord sender = senderOpt.get();
        UserRecord recipient = recipientOpt.get();

        if (sender.getBalance() < t.getAmount()) {
            return; // insufficient balance
        }

        sender.setBalance(sender.getBalance() - t.getAmount());
        recipient.setBalance(recipient.getBalance() + t.getAmount());

        userRepo.save(sender);
        userRepo.save(recipient);

        TransactionRecord record = new TransactionRecord(sender, recipient, t.getAmount());
        transactionRepo.save(record);

        // LOG WALDORF BALANCE
        Optional<UserRecord> waldorfOpt = userRepo.findAll().stream()
                .filter(u -> u.getName().equalsIgnoreCase("waldorf"))
                .findFirst();

        waldorfOpt.ifPresent(waldorf -> System.out.println("Waldorf's current balance: " + waldorf.getBalance()));
    }
}

