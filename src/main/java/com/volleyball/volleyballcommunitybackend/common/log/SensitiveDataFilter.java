/**
 * @description 敏感信息过滤工具
 * @author lihao
 * @date 2026/4/4
 */
package com.volleyball.volleyballcommunitybackend.common.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class SensitiveDataFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveDataFilter.class);

    // 敏感字段名称（不区分大小写）
    private static final Set<String> SENSITIVE_FIELD_NAMES = new HashSet<>();
    // 字段名包含这些关键词会被视为敏感
    private static final Set<String> SENSITIVE_KEYWORDS = new HashSet<>();
    // 敏感数据正则模式
    private static final Map<String, Pattern> PATTERNS = new HashMap<>();

    static {
        // 敏感字段名
        SENSITIVE_FIELD_NAMES.add("password");
        SENSITIVE_FIELD_NAMES.add("pwd");
        SENSITIVE_FIELD_NAMES.add("passwd");
        SENSITIVE_FIELD_NAMES.add("secret");
        SENSITIVE_FIELD_NAMES.add("token");
        SENSITIVE_FIELD_NAMES.add("accessToken");
        SENSITIVE_FIELD_NAMES.add("refreshToken");
        SENSITIVE_FIELD_NAMES.add("apiKey");
        SENSITIVE_FIELD_NAMES.add("apiSecret");
        SENSITIVE_FIELD_NAMES.add("privateKey");
        SENSITIVE_FIELD_NAMES.add("publicKey");
        SENSITIVE_FIELD_NAMES.add("credential");
        SENSITIVE_FIELD_NAMES.add("credentials");
        SENSITIVE_FIELD_NAMES.add("authToken");
        SENSITIVE_FIELD_NAMES.add("sessionId");
        SENSITIVE_FIELD_NAMES.add("jsessionid");
        SENSITIVE_FIELD_NAMES.add("sessionid");
        SENSITIVE_FIELD_NAMES.add("cookie");
        SENSITIVE_FIELD_NAMES.add("idCard");
        SENSITIVE_FIELD_NAMES.add("idcard");
        SENSITIVE_FIELD_NAMES.add("identity");
        SENSITIVE_FIELD_NAMES.add("bankCard");
        SENSITIVE_FIELD_NAMES.add("bankcard");
        SENSITIVE_FIELD_NAMES.add("cardNo");
        SENSITIVE_FIELD_NAMES.add("cardno");
        SENSITIVE_FIELD_NAMES.add("creditCard");
        SENSITIVE_FIELD_NAMES.add("cvv");
        SENSITIVE_FIELD_NAMES.add("cvc");
        SENSITIVE_FIELD_NAMES.add("phone");
        SENSITIVE_FIELD_NAMES.add("mobile");
        SENSITIVE_FIELD_NAMES.add("tel");
        SENSITIVE_FIELD_NAMES.add("telephone");
        SENSITIVE_FIELD_NAMES.add("email");
        SENSITIVE_FIELD_NAMES.add("address");
        SENSITIVE_FIELD_NAMES.add("realName");
        SENSITIVE_FIELD_NAMES.add("realname");
        SENSITIVE_FIELD_NAMES.add("name");
        SENSITIVE_FIELD_NAMES.add("idNumber");
        SENSITIVE_FIELD_NAMES.add("idnumber");
        SENSITIVE_FIELD_NAMES.add("socialSecurity");
        SENSITIVE_FIELD_NAMES.add("ssn");
        SENSITIVE_FIELD_NAMES.add("aesKey");
        SENSITIVE_FIELD_NAMES.add("hmacKey");
        SENSITIVE_FIELD_NAMES.add("encryptKey");
        SENSITIVE_FIELD_NAMES.add("signature");
        SENSITIVE_FIELD_NAMES.add("sign");

        // 敏感关键词
        SENSITIVE_KEYWORDS.add("password");
        SENSITIVE_KEYWORDS.add("passwd");
        SENSITIVE_KEYWORDS.add("secret");
        SENSITIVE_KEYWORDS.add("token");
        SENSITIVE_KEYWORDS.add("key");
        SENSITIVE_KEYWORDS.add("credential");

        // 手机号正则 (11位)
        PATTERNS.put("MOBILE_CN", Pattern.compile("(?<!\\d)(1[3-9]\\d{9})(?!\\d)"));
        // 身份证号正则 (15位和18位)
        PATTERNS.put("ID_CARD_15", Pattern.compile("(?<!\\d)(\\d{15})(?!\\d)"));
        PATTERNS.put("ID_CARD_18", Pattern.compile("(?<!\\d)(\\d{17}[\\dXx])(?!\\d)"));
        // 邮箱正则
        PATTERNS.put("EMAIL", Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"));
        // 银行卡号正则
        PATTERNS.put("BANK_CARD", Pattern.compile("(?<!\\d)([1-9]\\d{12,19})(?!\\d)"));
        // JWT Token正则
        PATTERNS.put("JWT", Pattern.compile("(?i)(Bearer\\s+)?([a-zA-Z0-9-_]+\\.[a-zA-Z0-9-_]+\\.[a-zA-Z0-9-_]+)"));
    }

    /**
     * 判断字段名是否为敏感字段
     */
    public boolean isSensitiveField(String fieldName) {
        if (fieldName == null) {
            return false;
        }
        String lowerFieldName = fieldName.toLowerCase();
        if (SENSITIVE_FIELD_NAMES.contains(lowerFieldName)) {
            return true;
        }
        for (String keyword : SENSITIVE_KEYWORDS) {
            if (lowerFieldName.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 过滤对象中的敏感信息
     */
    public String maskObject(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            String json = ObjectMapperHolder.MAPPER.writeValueAsString(obj);
            return maskJson(json);
        } catch (Exception e) {
            logger.debug("Failed to serialize object for masking", e);
            return obj.toString();
        }
    }

    /**
     * 过滤JSON字符串中的敏感信息
     */
    public String maskJson(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        String masked = json;

        // 过滤手机号
        masked = PATTERNS.get("MOBILE_CN").matcher(masked).replaceAll("***");
        // 过滤身份证号 15位
        masked = PATTERNS.get("ID_CARD_15").matcher(masked).replaceAll("***********");
        // 过滤身份证号 18位
        masked = PATTERNS.get("ID_CARD_18").matcher(masked).replaceAll("******************");
        // 过滤邮箱
        masked = maskEmail(masked);
        // 过滤银行卡号
        masked = PATTERNS.get("BANK_CARD").matcher(masked).replaceAll("****");
        // 过滤JWT Token
        masked = PATTERNS.get("JWT").matcher(masked).replaceAll("***JWT_TOKEN***");

        return masked;
    }

    /**
     * 邮箱脱敏
     */
    private String maskEmail(String email) {
        java.util.regex.Matcher matcher = PATTERNS.get("EMAIL").matcher(email);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String mail = matcher.group();
            int atIndex = mail.indexOf('@');
            if (atIndex > 1) {
                String prefix = mail.substring(0, atIndex);
                String suffix = mail.substring(atIndex);
                if (prefix.length() <= 2) {
                    matcher.appendReplacement(sb, "*" + suffix);
                } else {
                    matcher.appendReplacement(sb, prefix.charAt(0) + "***" + prefix.charAt(prefix.length() - 1) + suffix);
                }
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 通用脱敏方法
     */
    public String mask(String value, String type) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return switch (type.toLowerCase()) {
            case "phone", "mobile" -> maskMobile(value);
            case "email" -> maskEmailSimple(value);
            case "idcard", "id_card" -> maskIdCard(value);
            case "bankcard", "bank_card" -> maskBankCard(value);
            case "name", "realname" -> maskName(value);
            default -> maskDefault(value);
        };
    }

    private String maskMobile(String mobile) {
        if (mobile.length() == 11) {
            return mobile.substring(0, 3) + "****" + mobile.substring(7);
        }
        return "***";
    }

    private String maskEmailSimple(String email) {
        if (email.contains("@")) {
            int atIndex = email.indexOf('@');
            String prefix = email.substring(0, atIndex);
            String suffix = email.substring(atIndex);
            if (prefix.length() <= 2) {
                return "*" + suffix;
            }
            return prefix.charAt(0) + "***" + prefix.charAt(prefix.length() - 1) + suffix;
        }
        return "***";
    }

    private String maskIdCard(String idCard) {
        if (idCard.length() == 15) {
            return "***********";
        } else if (idCard.length() == 18) {
            return "******************";
        }
        return "***";
    }

    private String maskBankCard(String cardNo) {
        if (cardNo.length() >= 12) {
            return "****" + cardNo.substring(cardNo.length() - 4);
        }
        return "****";
    }

    private String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return "*";
        } else if (name.length() == 2) {
            return name.charAt(0) + "*";
        } else {
            return name.charAt(0) + "*" + name.charAt(name.length() - 1);
        }
    }

    private String maskDefault(String value) {
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }

    /**
     * ObjectMapper holder with sensitive data filtering
     */
    public static class ObjectMapperHolder {
        public static final ObjectMapper MAPPER = createMapper();

        private static ObjectMapper createMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            return mapper;
        }
    }
}
