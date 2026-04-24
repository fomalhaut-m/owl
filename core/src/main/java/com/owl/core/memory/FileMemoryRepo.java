package com.owl.core.memory;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

/**
 * 基于文件系统的记忆仓库实现（JPA 命名风格）
 * <p>
 * 使用本地文件系统存储和管理用户记忆，采用以下目录结构：
 * <pre>
 * basePath/
 * └── userId/
 *     └── mnemosyne/
 *         ├── config.json
 *         └── ...
 * </pre>
 * </p>
 *
 * <h3>JPA 命名风格：</h3>
 * <ul>
 *   <li>save() - 保存</li>
 *   <li>findByUserIdAndPathAndName() - 查询</li>
 *   <li>delete() - 删除</li>
 *   <li>findAll() - 查询所有</li>
 *   <li>findConfigByUserId() - 查询配置</li>
 *   <li>saveConfig() - 保存配置</li>
 * </ul>
 *
 * @author Owl Team
 * @since 2026-04-23
 */
public record FileMemoryRepo(String basePath) implements MemoryRepo {

    private static final String DEFAULT_BASE_PATH = "/owl/memory/users";
    private static final String MEMORY_ROOT_DIR = "mnemosyne";
    private static final String CONFIG_FILE_NAME = "config.json";
    private static final String HIDDEN_FILE_PREFIX = ".";
    private static final int MAX_DEPTH = Integer.MAX_VALUE;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public FileMemoryRepo() {
        this(DEFAULT_BASE_PATH);
    }

    @Override
    public void save(String userId, List<String> path, String name, String content) {
        validateUserId(userId);
        validatePath(path);
        validateName(name);

        try {
            Path fullPath = getMemoryPath(userId, path, name);
            Path parent = fullPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Files.writeString(fullPath, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new MemoryException("保存记忆失败: " + path + "/" + name, e);
        }
    }

    @Override
    public String findByUserIdAndPathAndName(String userId, List<String> path, String name) {
        validateUserId(userId);
        validatePath(path);
        validateName(name);

        try {
            Path fullPath = getMemoryPath(userId, path, name);
            if (Files.exists(fullPath)) {
                return Files.readString(fullPath, StandardCharsets.UTF_8);
            }
            return null;
        } catch (IOException e) {
            throw new MemoryException("读取记忆失败: " + path + "/" + name, e);
        }
    }

    @Override
    public void delete(String userId, List<String> path, String name) {
        validateUserId(userId);
        validatePath(path);
        validateName(name);

        try {
            Path fullPath = getMemoryPath(userId, path, name);
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
            }
        } catch (IOException e) {
            throw new MemoryException("删除记忆失败: " + path + "/" + name, e);
        }
    }

    @Override
    public List<MemoryMetadata> findAllByUserId(String userId) {
        validateUserId(userId);

        Path root = getUserRoot(userId);
        if (!Files.exists(root)) {
            return List.of();
        }

        List<MemoryMetadata> results = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(Files::isRegularFile)
                    .filter(this::isNotHiddenFile)
                    .forEach(path -> addMemoryMetadata(results, root, path));
        } catch (IOException e) {
            throw new MemoryException("查询所有记忆失败", e);
        }
        return results;
    }

    @Override
    public List<MemoryMetadata> findAllByUserIdAndTimeBetween(String userId, long startTime, long endTime) {
        validateUserId(userId);
        validateTimeRange(startTime, endTime);

        Path root = getUserRoot(userId);
        if (!Files.exists(root)) {
            return List.of();
        }

        BiPredicate<Path, BasicFileAttributes> timeFilter = createTimeFilter(startTime, endTime);
        List<MemoryMetadata> results = new ArrayList<>();
        try (Stream<Path> find = Files.find(root, MAX_DEPTH, timeFilter)) {
            find.filter(Files::isRegularFile)
                    .filter(this::isNotHiddenFile)
                    .forEach(path -> addMemoryMetadata(results, root, path));
        } catch (IOException e) {
            throw new MemoryException("按时间范围查询记忆失败", e);
        }
        return results;
    }

    @Override
    public List<MemoryMetadata> findAllByUserIdAndSubPathAndTimeBetween(String userId, String subPath, long startTime, long endTime) {
        validateUserId(userId);
        validateTimeRange(startTime, endTime);

        Path base = (subPath == null || subPath.isBlank())
                ? getUserRoot(userId)
                : getUserRoot(userId).resolve(subPath);

        if (!Files.exists(base)) {
            return List.of();
        }

        BiPredicate<Path, BasicFileAttributes> timeFilter = createTimeFilter(startTime, endTime);
        List<MemoryMetadata> results = new ArrayList<>();
        try (Stream<Path> find = Files.find(base, MAX_DEPTH, timeFilter)) {
            find.filter(Files::isRegularFile)
                    .filter(this::isNotHiddenFile)
                    .forEach(path -> addMemoryMetadata(results, base, path));
        } catch (IOException e) {
            throw new MemoryException("按路径和时间范围查询记忆失败: " + subPath, e);
        }
        return results;
    }

    @Override
    public MemoryUserConfig findConfigByUserId(String userId) {
        validateUserId(userId);

        try {
            Path configPath = getUserRoot(userId).resolve(CONFIG_FILE_NAME);
            if (Files.exists(configPath)) {
                String json = Files.readString(configPath, StandardCharsets.UTF_8);
                return OBJECT_MAPPER.readValue(json, MemoryUserConfig.class);
            }
            return null;
        } catch (IOException e) {
            throw new MemoryException("查询用户配置失败: " + userId, e);
        }
    }

    @Override
    public void saveConfig(String userId, MemoryUserConfig config) {
        validateUserId(userId);

        try {
            Path configPath = getUserRoot(userId).resolve(CONFIG_FILE_NAME);
            Files.createDirectories(configPath.getParent());
            String json = OBJECT_MAPPER.writeValueAsString(config);
            Files.writeString(configPath, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new MemoryException("保存用户配置失败: " + userId, e);
        }
    }

    public void initUserSpace(String userId) {
        validateUserId(userId);

        try {
            Path userRoot = getUserRoot(userId);
            if (!Files.exists(userRoot)) {
                Files.createDirectories(userRoot);
            }

            Path configPath = userRoot.resolve(CONFIG_FILE_NAME);
            if (!Files.exists(configPath)) {
                String json = OBJECT_MAPPER.writeValueAsString(new MemoryUserConfig());
                Files.writeString(configPath, json, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new MemoryException("初始化用户空间失败: " + userId, e);
        }
    }

    public void deleteUserSpace(String userId) {
        validateUserId(userId);

        try {
            Path userRoot = getUserRoot(userId);
            if (Files.exists(userRoot)) {
                deleteDirectory(userRoot);
            }
        } catch (IOException e) {
            throw new MemoryException("删除用户空间失败: " + userId, e);
        }
    }

    private Path getUserRoot(String userId) {
        return Paths.get(basePath, userId, MEMORY_ROOT_DIR);
    }

    private Path getMemoryPath(String userId, List<String> path, String name) {
        Path userRoot = getUserRoot(userId);
        if (path == null || path.isEmpty()) {
            return userRoot.resolve(name);
        }
        return userRoot.resolve(Paths.get(String.join("/", path), name));
    }

    private void deleteDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        try (var stream = Files.walk(dir)) {
            stream.sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new MemoryException("删除文件失败: " + path, e);
                        }
                    });
        }
    }

    private void addMemoryMetadata(List<MemoryMetadata> results, Path root, Path filePath) {
        try {
            Path relativePath = root.relativize(filePath);
            String fullPathStr = normalizePath(relativePath.toString());

            List<String> pathParts = new ArrayList<>();
            String fileName = fullPathStr;

            int lastSlashIndex = fullPathStr.lastIndexOf('/');
            if (lastSlashIndex > 0) {
                String dirPart = fullPathStr.substring(0, lastSlashIndex);
                fileName = fullPathStr.substring(lastSlashIndex + 1);
                for (String part : dirPart.split("/")) {
                    if (!part.isEmpty()) {
                        pathParts.add(part);
                    }
                }
            }

            long size = Files.size(filePath);
            long modifiedTime = Files.getLastModifiedTime(filePath).toMillis();

            results.add(MemoryMetadata.builder()
                    .path(pathParts)
                    .name(fileName)
                    .createTime(modifiedTime)
                    .updateTime(modifiedTime)
                    .contentSize(size)
                    .build());
        } catch (IOException e) {
            // 跳过无法读取的文件
        }
    }

    private BiPredicate<Path, BasicFileAttributes> createTimeFilter(long startTime, long endTime) {
        return (path, attrs) -> {
            long lastModified = attrs.lastModifiedTime().toMillis();
            return lastModified >= startTime && lastModified <= endTime;
        };
    }

    private boolean isNotHiddenFile(Path path) {
        return !path.getFileName().toString().startsWith(HIDDEN_FILE_PREFIX);
    }

    private String normalizePath(String path) {
        return path.replace('\\', '/');
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
    }

    private void validatePath(List<String> path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        for (String part : path) {
            if (part == null || part.isBlank()) {
                throw new IllegalArgumentException("路径列表中包含空元素");
            }
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
    }

    private void validateTimeRange(long startTime, long endTime) {
        if (startTime < 0 || endTime < 0) {
            throw new IllegalArgumentException("时间戳不能为负数");
        }
        if (startTime > endTime) {
            throw new IllegalArgumentException("开始时间不能大于结束时间");
        }
    }
}
