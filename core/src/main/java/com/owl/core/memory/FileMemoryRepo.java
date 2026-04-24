package com.owl.core.memory;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

/**
 * 基于文件系统的记忆仓库实现
 * <p>
 * 使用本地文件系统存储和管理用户记忆，支持完整的记忆生命周期管理。
 * </p>
 *
 * @author Owl Team
 */
public class FileMemoryRepo implements MemoryRepo {

    /**
     * 用户记忆根目录名称
     */
    private static final String MEMORY_ROOT_DIR = "mnemosyne";

    /**
     * 配置文件名
     */
    private static final String CONFIG_FILE_NAME = "config.json";

    /**
     * 隐藏文件前缀
     */
    private static final String HIDDEN_FILE_PREFIX = ".";

    /**
     * 最大遍历深度（无限制）
     */
    private static final int MAX_DEPTH = Integer.MAX_VALUE;

    /**
     * 删除成功返回值
     */
    private static final int DELETE_SUCCESS = 1;

    /**
     * 删除失败返回值
     */
    private static final int DELETE_FAILED = 0;

    /**
     * 记忆系统根路径
     */
    private final String basePath;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 构造记忆仓库实现
     *
     * @param basePath 记忆系统根路径
     */
    public FileMemoryRepo(String basePath) {
        this.basePath = basePath;
    }

    /**
     * 使用默认根路径构造记忆仓库实现
     */
    public FileMemoryRepo() {
        this("/owl/memory/users");
    }

    @Override
    public void saveMemory(String userId, String path, String content) {
        try {
            Path fullPath = getMemoryPath(userId, path);
            Files.createDirectories(fullPath.getParent());
            Files.writeString(fullPath, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("保存记忆失败: " + path, e);
        }
    }

    @Override
    public String getMemory(String userId, String path) {
        try {
            Path fullPath = getMemoryPath(userId, path);
            if (Files.exists(fullPath)) {
                return Files.readString(fullPath, StandardCharsets.UTF_8);
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException("读取记忆失败: " + path, e);
        }
    }

    @Override
    public int deleteMemory(String userId, String path) {
        try {
            Path fullPath = getMemoryPath(userId, path);
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                return DELETE_SUCCESS;
            }
            return DELETE_FAILED;
        } catch (IOException e) {
            throw new RuntimeException("删除记忆失败: " + path, e);
        }
    }

    @Override
    public List<MemoryMetadata> getMemoryPaths(String userId) {
        List<MemoryMetadata> results = new ArrayList<>();
        Path root = getUserRoot(userId);

        if (!Files.exists(root)) {
            return results;
        }

        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().startsWith(HIDDEN_FILE_PREFIX))
                    .forEach(path -> {
                        try {
                            String relativePath = normalizePath(root.relativize(path).toString());
                            long size = Files.size(path);
                            long createTime = Files.getLastModifiedTime(path).toMillis();

                            results.add(MemoryMetadata.builder()
                                    .path(relativePath)
                                    .createTime(createTime)
                                    .updateTime(createTime)
                                    .contentSize(size)
                                    .build());
                        } catch (IOException e) {
                            // 跳过无法读取的文件
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("获取记忆路径失败", e);
        }

        return results;
    }

    @Override
    public List<MemoryMetadata> getMemoryPaths(String userId, long startTime, long endTime) {
        List<MemoryMetadata> results = new ArrayList<>();
        Path root = getUserRoot(userId);

        if (!Files.exists(root)) {
            return results;
        }

        // 时间范围过滤器
        BiPredicate<Path, BasicFileAttributes> timeFilter = (path, attrs) -> {
            long lastModified = attrs.lastModifiedTime().toMillis();
            return lastModified >= startTime && lastModified <= endTime;
        };

        try (Stream<Path> find = Files.find(root, MAX_DEPTH, timeFilter)) {
            find.filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().startsWith(HIDDEN_FILE_PREFIX))
                    .forEach(path -> {
                        try {
                            String relativePath = normalizePath(root.relativize(path).toString());
                            long size = Files.size(path);
                            long createTime = Files.getLastModifiedTime(path).toMillis();

                            results.add(MemoryMetadata.builder()
                                    .path(relativePath)
                                    .createTime(createTime)
                                    .updateTime(createTime)
                                    .contentSize(size)
                                    .build());
                        } catch (IOException e) {
                            // 跳过无法读取的文件
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("获取记忆路径失败", e);
        }

        return results;
    }

    @Override
    public List<MemoryMetadata> getMemoryPathsByPath(String userId, String rootPath, long startTime, long endTime) {
        List<MemoryMetadata> results = new ArrayList<>();
        Path base = getUserRoot(userId).resolve(rootPath);

        if (!Files.exists(base)) {
            return results;
        }

        // 时间范围过滤器
        BiPredicate<Path, BasicFileAttributes> timeFilter = (path, attrs) -> {
            long lastModified = attrs.lastModifiedTime().toMillis();
            return lastModified >= startTime && lastModified <= endTime;
        };

        try (Stream<Path> find = Files.find(base, MAX_DEPTH, timeFilter)) {
            find.filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().startsWith(HIDDEN_FILE_PREFIX))
                    .forEach(path -> {
                        try {
                            // 相对于 rootPath 的路径（不带 rootPath 前缀）
                            String relativePath = normalizePath(base.relativize(path).toString());
                            long size = Files.size(path);
                            long createTime = Files.getLastModifiedTime(path).toMillis();

                            results.add(MemoryMetadata.builder()
                                    .path(relativePath)
                                    .createTime(createTime)
                                    .updateTime(createTime)
                                    .contentSize(size)
                                    .build());
                        } catch (IOException e) {
                            // 跳过无法读取的文件
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("获取指定路径的记忆失败: " + rootPath, e);
        }

        return results;
    }

    @Override
    public MemoryUserConfig getUserConfig(String userId) {
        try {
            Path configPath = getUserRoot(userId).resolve(CONFIG_FILE_NAME);
            if (Files.exists(configPath)) {
                String json = Files.readString(configPath, StandardCharsets.UTF_8);
                return OBJECT_MAPPER.readValue(json, MemoryUserConfig.class);
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException("读取用户配置失败: " + userId, e);
        }
    }

    @Override
    public void saveUserConfig(String userId, MemoryUserConfig config) {
        try {
            Path configPath = getUserRoot(userId).resolve(CONFIG_FILE_NAME);
            Files.createDirectories(configPath.getParent());
            String json = OBJECT_MAPPER.writeValueAsString(config);
            Files.writeString(configPath, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("保存用户配置失败: " + userId, e);
        }
    }

    /**
     * 获取用户记忆根目录
     *
     * @param userId 用户 ID
     * @return 用户记忆根路径
     */
    private Path getUserRoot(String userId) {
        return Paths.get(basePath, userId, MEMORY_ROOT_DIR);
    }

    /**
     * 获取记忆文件的完整路径
     *
     * @param userId 用户 ID
     * @param path   记忆相对路径
     * @return 记忆文件完整路径
     */
    private Path getMemoryPath(String userId, String path) {
        return getUserRoot(userId).resolve(path);
    }

    /**
     * 将路径转换为统一的正斜杠格式，以兼容不同操作系统
     *
     * @param path 原始路径字符串
     * @return 统一后的路径字符串
     */
    private String normalizePath(String path) {
        return path.replace('\\', '/');
    }

    public void initUserSpace(String userId) {
        try {
            Path userRoot = getUserRoot(userId);

            // 确保根目录存在
            if (!Files.exists(userRoot)) {
                Files.createDirectories(userRoot);
            }

            // 创建默认配置文件
            Path configPath = userRoot.resolve(CONFIG_FILE_NAME);
            if (!Files.exists(configPath)) {
                Files.writeString(configPath, OBJECT_MAPPER.writeValueAsString(new MemoryUserConfig()), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("初始化用户空间失败: " + userId, e);
        }
    }

    /**
     * 删除用户记忆空间
     * <p>
     * 递归删除指定用户的所有记忆数据，包括目录结构和配置文件。
     * </p>
     *
     * @param userId 用户唯一标识
     */
    public void deleteUserSpace(String userId) {
        try {
            Path userRoot = getUserRoot(userId);
            if (Files.exists(userRoot)) {
                deleteDirectory(userRoot);
            }
        } catch (IOException e) {
            throw new RuntimeException("删除用户空间失败: " + userId, e);
        }
    }

    /**
     * 递归删除目录及其内容
     *
     * @param dir 要删除的目录路径
     * @throws IOException 删除失败时抛出
     */
    private void deleteDirectory(Path dir) throws IOException {
        if (Files.isDirectory(dir)) {
            try (var stream = Files.walk(dir)) {
                stream.sorted((a, b) -> b.compareTo(a)) // 从深到浅排序，先删除子文件
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new RuntimeException("删除文件失败: " + path, e);
                            }
                        });
            }
        } else if (Files.exists(dir)) {
            Files.delete(dir);
        }
    }
}
