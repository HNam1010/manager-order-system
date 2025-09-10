package com.example.order_service.controller;

import com.example.be.commons.ApiResponse;
import com.example.order_service.dto.BankInfo; // Import DTO vừa tạo
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payment-info") // Base path cho API thông tin thanh toán
public class PaymentInfoController {


    @GetMapping("/bank-transfer")
    public ApiResponse<BankInfo> getBankTransferInfoHardcoded() {

        BankInfo bankInfo = new BankInfo(
                "CONG TY BÁN QUẦN ÁO",          // Tên chủ TK
                "0795862489",                               // Số TK
                "Ngan hang TMCP Ky Thuong (TP BANK)",       // Tên NH
                "Chi nhanh Long Xuyen",                     // Chi nhánh
                "TTDH"                                      // Tiền tố nội dung CK
        );

        return ApiResponse.success("Lấy thông tin chuyển khoản thành công", bankInfo);
    }

}