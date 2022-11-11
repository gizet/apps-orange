package com.orange.corepayments.controller;

import com.orange.corepayments.client.CorePaymentDto;
import com.orange.corepayments.client.CorePaymentResponse;
import com.orange.corepayments.client.PaymentDto;
import com.orange.corepayments.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.orange.corepayments.client.PaymentStatusType.PENDING_AUTHORIZATION;
import static com.orange.corepayments.client.PaymentStatusType.UNPROCESSED;
import static com.orange.corepayments.converter.Converter.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    @Operation(summary = "List of payments with statuses. Receives driver`s payments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "404", description = "Data not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CorePaymentResponse readPayments(@RequestParam List<String> requestIds) {
        final var uuids = requestIds.stream()
                .map(r -> {
                    var uid = r.replace("[", "").replace("]", "");
                    return UUID.fromString(uid);
                })
                .collect(Collectors.toList());

        final var payments = paymentService.findPayments(uuids);
        return CorePaymentResponse.builder()
                .payments(toCorePaymentDtos(payments))
                .build();
    }


    @Operation(summary = "Authorize payment. Receives an unprocessed payment which will be AUTHORIZED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "404", description = "Data not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Async
    @PostMapping
    public PaymentDto authorizePayment(@RequestBody CorePaymentDto paymentRequest) {
        Assert.isTrue(paymentRequest.getPaymentStatus().equals(UNPROCESSED), "Payment can only be UNPROCESSED");
        final var authorizePayment = paymentService.authorizePayment(toPayment(paymentRequest));
        return toPaymentDto(authorizePayment);
    }


    @Operation(summary = "Confirmation of payment. Receives an authorized payment which will be CONFIRMED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "404", description = "Data not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Async
    @PutMapping
    public PaymentDto confirmPayment(@RequestBody PaymentDto paymentRequest) {
        Assert.isTrue(paymentRequest.getPaymentStatus().equals(PENDING_AUTHORIZATION), "Payment can only be CONFIRMED");
        final var payment = paymentService.confirmPayment(toPayment(paymentRequest));
        return toPaymentDto(payment);
    }
}
