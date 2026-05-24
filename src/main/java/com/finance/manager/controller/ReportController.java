package com.finance.manager.controller;

import com.finance.manager.dto.response.ResponseDtos.*;
import com.finance.manager.entity.User;
import com.finance.manager.exception.AppExceptions.BadRequestException;
import com.finance.manager.service.AuthService;
import com.finance.manager.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final AuthService authService;

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month,
            HttpServletRequest request) {
        // Validate month range (1-12)
        if (month < 1 || month > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }
        User user = authService.getCurrentUser(request);
        return ResponseEntity.ok(reportService.getMonthlyReport(user, year, month));
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(
            @PathVariable int year,
            HttpServletRequest request) {
        User user = authService.getCurrentUser(request);
        return ResponseEntity.ok(reportService.getYearlyReport(user, year));
    }
}