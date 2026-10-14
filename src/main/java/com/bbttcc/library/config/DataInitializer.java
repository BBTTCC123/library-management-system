package com.bbttcc.library.config;

import com.bbttcc.library.entity.Book;
import com.bbttcc.library.entity.Category;
import com.bbttcc.library.entity.Role;
import com.bbttcc.library.entity.User;
import com.bbttcc.library.repository.BookRepository;
import com.bbttcc.library.repository.CategoryRepository;
import com.bbttcc.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * 首次启动时初始化演示数据（内置账号、分类、图书）。
 * 可通过 {@code app.seed.enabled=false} 关闭。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner seedData() {
        return args -> {
            seedUsers();
            seedCatalog();
        };
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        userRepository.saveAll(List.of(
                User.builder().username("admin").password(passwordEncoder.encode("admin123"))
                        .fullName("系统管理员").email("admin@library.local").role(Role.ADMIN).enabled(true).build(),
                User.builder().username("librarian").password(passwordEncoder.encode("librarian123"))
                        .fullName("图书管理员").email("librarian@library.local").role(Role.LIBRARIAN).enabled(true).build(),
                User.builder().username("alice").password(passwordEncoder.encode("alice123"))
                        .fullName("爱丽丝").email("alice@library.local").role(Role.MEMBER).enabled(true).build(),
                User.builder().username("bob").password(passwordEncoder.encode("bob123"))
                        .fullName("鲍勃").email("bob@library.local").role(Role.MEMBER).enabled(true).build()
        ));
        log.info("已初始化演示用户：admin/admin123, librarian/librarian123, alice/alice123, bob/bob123");
    }

    private void seedCatalog() {
        if (bookRepository.count() > 0) {
            return;
        }
        Category computer = categoryRepository.save(Category.builder().name("计算机").description("计算机科学与技术").build());
        Category literature = categoryRepository.save(Category.builder().name("文学").description("中外文学作品").build());
        Category history = categoryRepository.save(Category.builder().name("历史").description("历史与人文").build());
        Category science = categoryRepository.save(Category.builder().name("科学").description("自然科学").build());

        bookRepository.saveAll(List.of(
                book("9787111544937", "深入理解计算机系统", "Randal E. Bryant", "机械工业出版社", 2016, computer, 5,
                        "从程序员视角全面剖析计算机系统，理解程序性能与底层原理的经典之作。"),
                book("9787111213826", "Effective Java", "Joshua Bloch", "机械工业出版社", 2018, computer, 3,
                        "Java 编程最佳实践，涵盖泛型、枚举、并发等主题。"),
                book("9787115546081", "Spring 实战（第 6 版）", "Craig Walls", "人民邮电出版社", 2021, computer, 4,
                        "Spring Boot 应用开发实战指南。"),
                book("9787020002207", "红楼梦", "曹雪芹", "人民文学出版社", 1996, literature, 2,
                        "中国古典四大名著之一。"),
                book("9787544253994", "百年孤独", "加西亚·马尔克斯", "南海出版公司", 2011, literature, 3,
                        "魔幻现实主义文学的代表作。"),
                book("9787508647357", "人类简史", "尤瓦尔·赫拉利", "中信出版社", 2014, history, 4,
                        "从认知革命到科学革命，重新理解人类的过去与未来。"),
                book("9787535732309", "时间简史", "史蒂芬·霍金", "湖南科学技术出版社", 2010, science, 2,
                        "探索宇宙起源与命运的科普经典。")
        ));
        log.info("已初始化演示分类与图书数据");
    }

    private Book book(String isbn, String title, String author, String publisher, int year,
                      Category category, int copies, String description) {
        return Book.builder()
                .isbn(isbn).title(title).author(author).publisher(publisher)
                .publishYear(year).category(category)
                .totalCopies(copies).availableCopies(copies)
                .description(description)
                .build();
    }
}
