package com.roberto.transactions;

import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import sun.rmi.transport.tcp.TCPTransport;


import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
public class TransactionController {

    @RequestMapping(path="/authorizeTransaction", method=RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public Object authorizeTransaction(@RequestBody TransactionAuthorizationRequest request) {
        TransactionHelper helper = new TransactionHelper(request);
        return helper.analyze();
    }
}
