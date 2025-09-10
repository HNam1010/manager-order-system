package com.example.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankInfo {
    private String accountName;       // Tên chủ tài khoản
    private String accountNumber;     // Số tài khoản
    private String bankName;          // Tên ngân hàng
    private String bankBranch;        // Chi nhánh (có thể null)
    private String transferContentPrefix; // Tiền tố nội dung CK (ví dụ: TTDH)
}