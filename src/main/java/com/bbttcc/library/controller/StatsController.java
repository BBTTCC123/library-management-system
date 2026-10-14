package com.bbttcc.library.controller;

import com.bbttcc.library.dto.ApiResponse;
import com.bbttcc.library.dto.borrow.LibraryStatsResponse;
import com.bbttcc.library.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "统计", description = "图书馆整体统计信息")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final BorrowService borrowService;

    @Operation(summary = "获取统计信息")
    @GetMapping
    public ApiResponse<LibraryStatsResponse> stats() {
        return ApiResponse.success(borrowService.stats());
    }
}
