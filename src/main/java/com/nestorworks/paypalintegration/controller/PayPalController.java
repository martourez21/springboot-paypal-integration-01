package com.nestorworks.paypalintegration.controller;

import com.nestorworks.paypalintegration.dto.OrderRequest;
import com.nestorworks.paypalintegration.service.PayPalService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PayPalController {


    private final PayPalService payPalService;

    public static final String SUCCESS_URL = "success";
    public static final String CANCEL_URL = "cancel";

    @GetMapping(value = {"/", "/shop"})
    public String getShopPage(){
        return "shop";
    }


    @PostMapping("/pay")
    public String payment(@ModelAttribute("order") OrderRequest order) throws PayPalRESTException {
       try{
           Payment payment =
                   payPalService.createPayment(
                           order.price(),
                           order.currency(),
                           order.method(),
                           order.intent(),
                           order.description(),
                           "http://localhost:9090/" + CANCEL_URL,
                           "http://localhost:9090/" + SUCCESS_URL
                   );

           for(Links link: payment.getLinks()){
               if(link.getRel().equals("approval_url")){
                   return "redirect:" +link.getHref();
               }
           }
       }catch(PayPalRESTException e){
            log.error("Error making payment for this purchase: {}", e.getDetails());
       }

       return "redirect:/";
    }

    @GetMapping(value = CANCEL_URL)
    public String cancelPayment(){
        return "cancel";
    }

    @GetMapping(value = SUCCESS_URL)
    public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
        try {
            Payment payment = payPalService.executePayment(paymentId, payerId);
            System.out.println(payment.toJSON());
            if (payment.getState().equals("approved")) {
                return "success";
            }
        } catch (PayPalRESTException e) {
            log.error("Payment Unsuccessful: {}", e.getCause());
        }
        return "redirect:/";
    }

}
