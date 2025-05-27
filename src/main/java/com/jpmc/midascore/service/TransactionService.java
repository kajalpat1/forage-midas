package com.jpmc.midascore.service;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRecordRepository;

@Service
public class TransactionService {
    private final UserRecordRepository userRepo;
    private final TransactionRecordRepository transactionRepo;
    private final RestTemplate restTemplate;

    public TransactionService(UserRecordRepository userRepo,
                              TransactionRecordRepository transactionRepo,
                              RestTemplate restTemplate) {
        this.userRepo = userRepo;
        this.transactionRepo = transactionRepo;
        this.restTemplate = restTemplate;
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

        // 💰 Deduct from sender
        sender.setBalance(sender.getBalance() - t.getAmount());

        // 📞 Call Incentive API
        ResponseEntity<Incentive> response = restTemplate.postForEntity(
            "http://localhost:8080/incentive",
            t,
            Incentive.class
        );
        float incentive = response.getBody().getAmount();

        // Add to recipient (including incentive)
        recipient.setBalance(recipient.getBalance() + t.getAmount() + incentive);

        // Save users
        userRepo.save(sender);
        userRepo.save(recipient);

        // Save transaction record with incentive
        TransactionRecord record = new TransactionRecord(sender, recipient, t.getAmount(), incentive);
        transactionRepo.save(record);

        // Log Wilbur’s balance if needed
        Optional<UserRecord> wilburOpt = userRepo.findAll().stream()
        .filter(u -> u.getName().equalsIgnoreCase("wilbur"))
        .findFirst();

        wilburOpt.ifPresent(wilbur -> System.out.println("Wilbur's final balance: " + wilbur.getBalance()));
    }
}


