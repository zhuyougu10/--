package com.stadium.booking.util;

import cn.hutool.crypto.digest.BCrypt;
import org.junit.jupiter.api.Test;

class PasswordEncryptorTest {

    @Test
    void encryptPassword() {
        String plainPassword = "admin123";
        
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        
        System.out.println();
        System.out.println("========================================");
        System.out.println("明文密码: " + plainPassword);
        System.out.println("加密后:   " + hashedPassword);
        System.out.println("========================================");
        System.out.println();
        System.out.println("SQL 插入语句:");
        System.out.println("INSERT INTO admin_user (username, password_hash, name, status)");
        System.out.println("VALUES ('admin', '" + hashedPassword + "', '系统管理员', 1);");
        System.out.println();
        
        boolean verified = BCrypt.checkpw(plainPassword, hashedPassword);
        System.out.println("验证结果: " + (verified ? "验证通过" : "验证失败"));
    }
}
