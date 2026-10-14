package com.bbttcc.library.entity;

/**
 * 用户角色。
 *
 * <ul>
 *   <li>{@link #ADMIN} 系统管理员：可管理图书、分类、用户。</li>
 *   <li>{@link #LIBRARIAN} 图书管理员：可管理图书与借阅记录。</li>
 *   <li>{@link #MEMBER} 普通读者：可浏览、借阅、归还、续借。</li>
 * </ul>
 */
public enum Role {
    ADMIN,
    LIBRARIAN,
    MEMBER
}
