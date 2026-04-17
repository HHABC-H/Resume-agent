package com.h.resumeagent.utils;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class ResumeDocumentUtils {

    private static final Set<String> SUPPORTED_SUFFIXES = Set.of(".pdf", ".doc", ".docx", ".txt");

    public boolean isSupportedResumeFile(String fileName) {
        if (fileName == null) {
            return false;
        }
        String lowerName = fileName.toLowerCase(Locale.ROOT);
        for (String suffix : SUPPORTED_SUFFIXES) {
            if (lowerName.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    public String extractResumeText(MultipartFile file) throws IOException {
        String fileName = file == null ? null : file.getOriginalFilename();
        try {
            return readTextByTika(file);
        } catch (RuntimeException ex) {
            if (isPdfFile(fileName) && isPdfFontScanException(ex)) {
                sleepQuietly(300L);
                try {
                    return readTextByTika(file);
                } catch (RuntimeException retryEx) {
                    throw new IllegalArgumentException("PDF 解析失败：检测到系统字体文件异常，请重试或将文件转为 DOCX/TXT 后上传。");
                }
            }
            throw new IllegalArgumentException("文件解析失败：请确认文件未损坏且内容可读取。");
        }
    }

    private String readTextByTika(MultipartFile file) {
        TikaDocumentReader reader = new TikaDocumentReader(file.getResource());
        List<Document> docs = reader.read();
        if (docs == null || docs.isEmpty() || docs.get(0) == null) {
            throw new IllegalArgumentException("文件内容为空或无法解析。");
        }
        String text = docs.get(0).getText();
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("文件内容为空或无法解析。");
        }
        return text;
    }

    private boolean isPdfFile(String fileName) {
        return fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".pdf");
    }

    private boolean isPdfFontScanException(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase(Locale.ROOT);
                if (lower.contains("eof at")
                        || lower.contains("fontbox")
                        || lower.contains("filesystemfontprovider")
                        || lower.contains("ttf")) {
                    return true;
                }
            }
            for (StackTraceElement element : current.getStackTrace()) {
                String className = element.getClassName();
                if (className.contains("org.apache.fontbox.ttf")
                        || className.contains("org.apache.pdfbox.pdmodel.font.FileSystemFontProvider")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}
