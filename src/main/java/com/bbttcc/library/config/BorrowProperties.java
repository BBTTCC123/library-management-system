package com.bbttcc.library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 借阅规则配置，对应 {@code app.borrow.*}。
 *
 * @param maxBooksPerUser 单用户同时可借上限
 * @param loanPeriodDays  默认借阅天数
 * @param renewPeriodDays 每次续借延长天数
 * @param maxRenewCount   最多续借次数
 * @param finePerDay      逾期每天罚金（元）
 */
@ConfigurationProperties(prefix = "app.borrow")
public record BorrowProperties(
        int maxBooksPerUser,
        int loanPeriodDays,
        int renewPeriodDays,
        int maxRenewCount,
        double finePerDay
) {
}
