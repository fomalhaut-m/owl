package com.owl.core.memory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FileMemoryRepo 单元测试
 * <p>
 * 测试基于文件系统的记忆仓库实现，包括记忆 CRUD、用户空间初始化、配置管理等。
 * </p>
 *
 * @author Owl Team
 */
@DisplayName("FileMemoryRepo 单元测试")
class FileMemoryRepoTest {

    private static final String TEST_USER_ID = "test_user_001";
    private static final String TEST_BASE_PATH = "target/test_memory/users";
    
    private FileMemoryRepo memoryRepo;

    @BeforeEach
    void setUp() {
        // 测试前先清理测试目录的所有数据
        cleanupTestData();
        
        // 使用测试路径构造实例
        memoryRepo = new FileMemoryRepo(TEST_BASE_PATH);
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        cleanupTestData();
    }

    /**
     * 清理测试数据目录
     */
    private void cleanupTestData() {
        try {
            Path testPath = Paths.get(TEST_BASE_PATH);
            if (Files.exists(testPath)) {
                deleteDirectory(testPath.toFile());
            }
        } catch (Exception e) {
            // 忽略清理异常
        }
    }

    /**
     * 递归删除目录
     */
    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }

    @Test
    @DisplayName("测试保存记忆")
    void testSaveMemory() {
        String path = "working/P0/test_memory.md";
        String content = "这是测试记忆内容";
        
        memoryRepo.saveMemory(TEST_USER_ID, path, content);
        
        Path memoryFile = Paths.get(TEST_BASE_PATH, TEST_USER_ID, "mnemosyne", path);
        assertTrue(Files.exists(memoryFile), "记忆文件应该被创建");
        
        try {
            String savedContent = Files.readString(memoryFile);
            assertEquals(content, savedContent, "保存的内容应该一致");
        } catch (IOException e) {
            fail("读取记忆文件失败");
        }
    }

    @Test
    @DisplayName("测试获取记忆")
    void testGetMemory() {
        String path = "working/P1/get_test.md";
        String content = "用于测试读取的记忆内容";
        
        memoryRepo.saveMemory(TEST_USER_ID, path, content);
        
        String retrievedContent = memoryRepo.getMemory(TEST_USER_ID, path);
        assertNotNull(retrievedContent, "应该能读取到记忆内容");
        assertEquals(content, retrievedContent, "读取的内容应该与保存的一致");
    }

    @Test
    @DisplayName("测试获取不存在的记忆返回 null")
    void testGetNonExistentMemory() {
        String path = "working/P0/non_existent.md";
        
        String content = memoryRepo.getMemory(TEST_USER_ID, path);
        assertNull(content, "不存在的记忆应该返回 null");
    }

    @Test
    @DisplayName("测试删除记忆")
    void testDeleteMemory() {
        String path = "working/P2/delete_test.md";
        String content = "将要被删除的记忆";
        
        memoryRepo.saveMemory(TEST_USER_ID, path, content);
        
        int deletedCount = memoryRepo.deleteMemory(TEST_USER_ID, path);
        assertEquals(1, deletedCount, "应该成功删除 1 条记忆");
        
        Path memoryFile = Paths.get(TEST_BASE_PATH, TEST_USER_ID, "mnemosyne", path);
        assertFalse(Files.exists(memoryFile), "记忆文件应该被删除");
    }

    @Test
    @DisplayName("测试删除不存在的记忆返回 0")
    void testDeleteNonExistentMemory() {
        String path = "working/P0/non_existent.md";
        
        int deletedCount = memoryRepo.deleteMemory(TEST_USER_ID, path);
        assertEquals(0, deletedCount, "删除不存在的记忆应该返回 0");
    }

    @Test
    @DisplayName("测试获取所有记忆路径")
    void testGetMemoryPaths() {
        memoryRepo.saveMemory(TEST_USER_ID, "working/P0/memory1.md", "记忆 1");
        memoryRepo.saveMemory(TEST_USER_ID, "working/P1/memory2.md", "记忆 2");
        memoryRepo.saveMemory(TEST_USER_ID, "long_term/S/memory3.md", "记忆 3");
        
        List<MemoryMetadata> paths = memoryRepo.getMemoryPaths(TEST_USER_ID);
        
        assertEquals(3, paths.size(), "应该有 3 条记忆");
        
        assertTrue(paths.stream().anyMatch(m -> m.getPath().contains("memory1.md")),
                "应该包含 memory1.md");
        assertTrue(paths.stream().anyMatch(m -> m.getPath().contains("memory2.md")),
                "应该包含 memory2.md");
        assertTrue(paths.stream().anyMatch(m -> m.getPath().contains("memory3.md")),
                "应该包含 memory3.md");
    }

    @Test
    @DisplayName("测试获取空用户的记忆路径返回空列表")
    void testGetMemoryPathsForEmptyUser() {
        List<MemoryMetadata> paths = memoryRepo.getMemoryPaths("non_existent_user");
        
        assertTrue(paths.isEmpty(), "不存在的用户应该返回空列表");
    }

    @Test
    @DisplayName("测试记忆元数据包含正确信息")
    void testMemoryMetadataContent() {
        String path = "working/P0/metadata_test.md";
        String content = "测试元数据";
        
        memoryRepo.saveMemory(TEST_USER_ID, path, content);
        
        List<MemoryMetadata> paths = memoryRepo.getMemoryPaths(TEST_USER_ID);
        assertEquals(1, paths.size());
        
        MemoryMetadata metadata = paths.get(0);
        assertEquals(path, metadata.getPath(), "路径应该正确");
        assertTrue(metadata.getContentSize() > 0, "内容大小应该大于 0");
        assertNotNull(metadata.getCreateTime(), "创建时间不应该为 null");
        assertNotNull(metadata.getUpdateTime(), "更新时间不应该为 null");
    }

    @Test
    @DisplayName("测试按时间范围获取记忆路径")
    void testGetMemoryPathsByTimeRange() throws InterruptedException {
        long now = System.currentTimeMillis();
        
        // 保存较早的记忆
        memoryRepo.saveMemory(TEST_USER_ID, "working/P1/older.md", "较早的记忆");
        Thread.sleep(10); // 确保时间戳有差异
        
        long middleTime = System.currentTimeMillis();
        Thread.sleep(10);
        
        // 保存最近的记忆
        memoryRepo.saveMemory(TEST_USER_ID, "working/P0/recent.md", "最近的记忆");
        long endTime = System.currentTimeMillis();
        
        // 只获取最近创建的 1 条记忆
        List<MemoryMetadata> recentPaths = memoryRepo.getMemoryPaths(
                TEST_USER_ID, middleTime, endTime + 1000);
        
        assertEquals(1, recentPaths.size(), "应该获取到最近创建的 1 条记忆");
        assertTrue(recentPaths.get(0).getPath().contains("recent.md"), "应该是 recent.md");
        
        // 获取全部 2 条记忆
        List<MemoryMetadata> allPaths = memoryRepo.getMemoryPaths(
                TEST_USER_ID, now - 1000, endTime + 1000);
        assertEquals(2, allPaths.size(), "应该获取到全部 2 条记忆");
    }

    @Test
    @DisplayName("测试初始化用户空间")
    void testInitUserSpace() {
        memoryRepo.initUserSpace(TEST_USER_ID);
        
        Path userRoot = Paths.get(TEST_BASE_PATH, TEST_USER_ID, "mnemosyne");

        // 验证配置文件
        Path configPath = userRoot.resolve("config.json");
        assertTrue(Files.exists(configPath), "应该创建 config.json 文件");
        
        try {
            String config = Files.readString(configPath);
            assertTrue(config.contains("contextMaxTokenRate"), "配置文件应该包含 contextMaxTokenRate");
            assertTrue(config.contains("compressRatio"), "配置文件应该包含 compressRatio");
        } catch (IOException e) {
            fail("读取配置文件失败");
        }
    }

    @Test
    @DisplayName("测试获取用户配置")
    void testGetUserConfig() {
        memoryRepo.initUserSpace(TEST_USER_ID);
        
        MemoryUserConfig config = memoryRepo.getUserConfig(TEST_USER_ID);
        assertNotNull(config, "应该能获取到用户配置");
        assertEquals(0.3, config.getContextMaxTokenRate(), "默认 contextMaxTokenRate 应该是 0.3");
        assertEquals(10, config.getCompressRatio(), "默认 compressRatio 应该是 10");
    }

    @Test
    @DisplayName("测试获取不存在用户的配置返回 null")
    void testGetUserConfigForNonExistentUser() {
        MemoryUserConfig config = memoryRepo.getUserConfig("non_existent_user");
        assertNull(config, "不存在的用户应该返回 null");
    }

    @Test
    @DisplayName("测试保存用户配置")
    void testSaveUserConfig() {
        memoryRepo.initUserSpace(TEST_USER_ID);
        
        MemoryUserConfig newConfig = MemoryUserConfig.builder()
                .contextMaxTokenRate(0.5)
                .compressRatio(20)
                .build();
        
        memoryRepo.saveUserConfig(TEST_USER_ID, newConfig);
        
        MemoryUserConfig savedConfig = memoryRepo.getUserConfig(TEST_USER_ID);
        assertNotNull(savedConfig, "应该能读取到保存的配置");
        assertEquals(0.5, savedConfig.getContextMaxTokenRate(), "配置应该被更新");
        assertEquals(20, savedConfig.getCompressRatio(), "compressRatio 应该被更新");
    }

    @Test
    @DisplayName("测试完整工作流：初始化 - 保存 - 读取 - 更新 - 删除")
    void testCompleteWorkflow() {
        // 1. 初始化用户空间
        memoryRepo.initUserSpace(TEST_USER_ID);
        
        // 2. 保存记忆
        String path = "working/P0/workflow_test.md";
        String initialContent = "初始内容";
        memoryRepo.saveMemory(TEST_USER_ID, path, initialContent);
        
        // 3. 读取记忆
        String content = memoryRepo.getMemory(TEST_USER_ID, path);
        assertEquals(initialContent, content);
        
        // 4. 更新记忆（重新保存）
        String updatedContent = "更新后的内容";
        memoryRepo.saveMemory(TEST_USER_ID, path, updatedContent);
        
        // 5. 验证更新
        content = memoryRepo.getMemory(TEST_USER_ID, path);
        assertEquals(updatedContent, content);
        
        // 6. 删除记忆
        int deleted = memoryRepo.deleteMemory(TEST_USER_ID, path);
        assertEquals(1, deleted);
        
        // 7. 验证删除
        content = memoryRepo.getMemory(TEST_USER_ID, path);
        assertNull(content);
    }

    @Test
    @DisplayName("测试中文内容保存和读取")
    void testChineseContent() {
        String path = "working/P0/chinese_test.md";
        String chineseContent = "这是一条中文测试记忆。\n包含换行符和特殊字符：！@#￥%……&*（）";
        
        memoryRepo.saveMemory(TEST_USER_ID, path, chineseContent);
        
        String retrieved = memoryRepo.getMemory(TEST_USER_ID, path);
        assertEquals(chineseContent, retrieved, "中文内容应该正确保存和读取");
    }

    @Test
    @DisplayName("测试多次初始化不会覆盖已有配置")
    void testMultipleInitPreservesConfig() {
        memoryRepo.initUserSpace(TEST_USER_ID);
        
        MemoryUserConfig customConfig = MemoryUserConfig.builder()
                .contextMaxTokenRate(0.8)
                .build();
        memoryRepo.saveUserConfig(TEST_USER_ID, customConfig);
        
        // 再次初始化
        memoryRepo.initUserSpace(TEST_USER_ID);
        
        MemoryUserConfig config = memoryRepo.getUserConfig(TEST_USER_ID);
        assertEquals(0.8, config.getContextMaxTokenRate(), "多次初始化不应该覆盖用户自定义配置");
    }

    @Test
    @DisplayName("测试删除用户空间")
    void testDeleteUserSpace() {
        // 初始化用户空间
        memoryRepo.initUserSpace(TEST_USER_ID);
        
        // 保存一些记忆
        memoryRepo.saveMemory(TEST_USER_ID, "working/P0/test.md", "测试内容");
        
        Path userRoot = Paths.get(TEST_BASE_PATH, TEST_USER_ID, "mnemosyne");
        assertTrue(Files.exists(userRoot), "用户空间应该存在");
        
        // 删除用户空间
        memoryRepo.deleteUserSpace(TEST_USER_ID);
        
        // 验证删除成功
        assertFalse(Files.exists(userRoot), "用户空间应该被删除");
    }

    @Test
    @DisplayName("测试删除不存在的用户空间不报错")
    void testDeleteNonExistentUserSpace() {
        // 删除不存在的用户空间不应该抛出异常
        assertDoesNotThrow(() -> memoryRepo.deleteUserSpace("non_existent_user"));
    }
}
