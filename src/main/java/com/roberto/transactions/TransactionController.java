package com.roberto.transactions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class TransactionController {

    @Autowired
    private TransactionHelper helper;

    @RequestMapping(path="/authorizeTransaction", method=RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public Object authorizeTransaction(@RequestBody TransactionAuthorizationRequest request) {
        return this.helper.authorize(request);
    }
}
