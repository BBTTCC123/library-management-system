package com.bbttcc.library.entity;

/**
 * 借阅记录状态。
 */
public enum BorrowStatus {
    /** 借阅中（未归还）。 */
    BORROWED,
    /** 已归还。 */
    RETURNED,
    /** 逾期未归还（由定时任务或查询时动态计算）。 */
    OVERDUE
}
