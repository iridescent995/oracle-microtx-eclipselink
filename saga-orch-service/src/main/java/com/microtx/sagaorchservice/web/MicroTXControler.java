package com.microtx.sagaorchservice.web;

import com.microtx.sagaorchservice.services.IAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.sql.SQLException;

@RestController
@RequestMapping("/saga")
public class MicroTXControler {

    @Autowired
    @Qualifier("MicroTxXaWebClientBuilder")
    @Lazy
    WebClient.Builder webClientBuilder;

    private static final Logger LOG = LoggerFactory.getLogger(MicroTXControler.class);

    @Autowired
    IAccountService accountService;

    @RequestMapping(value = "/get/{account}", method = RequestMethod.GET)
//    @Transactional
    public ResponseEntity<?> getAccountDetails(@PathVariable("account") String account) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(this.accountService.accountDetails(account));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/{accountId}/withdraw", method = RequestMethod.POST)
    public ResponseEntity<?>  withdraw(@PathVariable("accountId") String accountId, @RequestParam("amount") double amount) {
        try {
            if(this.accountService.withdraw(accountId, amount)) {
                LOG.info(amount + " withdrawn from account: " + accountId);
                return ResponseEntity.ok("Amount withdrawn from the account");
            }
        } catch (SQLException | IllegalArgumentException e) {
            LOG.error(e.getLocalizedMessage());
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
        return ResponseEntity.internalServerError().body("Withdraw failed");
    }

}
