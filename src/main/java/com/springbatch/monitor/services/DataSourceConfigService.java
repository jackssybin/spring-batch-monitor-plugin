package com.springbatch.monitor.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.ide.util.PropertiesComponent;
import com.springbatch.monitor.model.DataSourceConfig;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 数据源配置管理服务
 */
public class DataSourceConfigService {
    private static final DataSourceConfigService INSTANCE = new DataSourceConfigService();
    private static final String DATASOURCE_CONFIG_KEY = "spring.batch.monitor.datasources";
    
    private final List<DataSourceConfig> dataSourceConfigs = new CopyOnWriteArrayList<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final List<DataSourceConfigListener> listeners = new CopyOnWriteArrayList<>();

    private DataSourceConfigService() {
        loadConfigurations();
    }

    public static DataSourceConfigService getInstance() {
        return INSTANCE;
    }

    /**
     * 添加配置变更监听器
     */
    public void addListener(DataSourceConfigListener listener) {
        listeners.add(listener);
    }

    /**
     * 移除配置变更监听器
     */
    public void removeListener(DataSourceConfigListener listener) {
        listeners.remove(listener);
    }

    /**
     * 通知监听器配置已变更
     */
    private void notifyConfigChanged() {
        for (DataSourceConfigListener listener : listeners) {
            try {
                listener.onConfigChanged(new ArrayList<>(dataSourceConfigs));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取所有数据源配置
     */
    public List<DataSourceConfig> getAllConfigurations() {
        return new ArrayList<>(dataSourceConfigs);
    }

    /**
     * 根据ID获取数据源配置
     */
    public DataSourceConfig getConfiguration(String id) {
        return dataSourceConfigs.stream()
                .filter(config -> id.equals(config.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 添加数据源配置
     */
    public void addConfiguration(DataSourceConfig config) {
        if (config.getId() == null || config.getId().trim().isEmpty()) {
            config.setId(UUID.randomUUID().toString());
        }
        
        // 检查ID是否已存在
        if (getConfiguration(config.getId()) != null) {
            throw new IllegalArgumentException("Data source with ID '" + config.getId() + "' already exists");
        }
        
        dataSourceConfigs.add(config);
        saveConfigurations();
        notifyConfigChanged();
    }

    /**
     * 更新数据源配置
     */
    public void updateConfiguration(DataSourceConfig config) {
        for (int i = 0; i < dataSourceConfigs.size(); i++) {
            if (dataSourceConfigs.get(i).getId().equals(config.getId())) {
                dataSourceConfigs.set(i, config);
                saveConfigurations();
                notifyConfigChanged();
                return;
            }
        }
        throw new IllegalArgumentException("Data source with ID '" + config.getId() + "' not found");
    }

    /**
     * 删除数据源配置
     */
    public void removeConfiguration(String id) {
        boolean removed = dataSourceConfigs.removeIf(config -> id.equals(config.getId()));
        if (removed) {
            saveConfigurations();
            notifyConfigChanged();
        }
    }

    /**
     * 获取活跃的数据源配置
     */
    public List<DataSourceConfig> getActiveConfigurations() {
        return dataSourceConfigs.stream()
                .filter(DataSourceConfig::isActive)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 设置数据源激活状态
     */
    public void setConfigurationActive(String id, boolean active) {
        DataSourceConfig config = getConfiguration(id);
        if (config != null) {
            config.setActive(active);
            saveConfigurations();
            notifyConfigChanged();
        }
    }

    /**
     * 保存配置到持久化存储
     */
    private void saveConfigurations() {
        try {
            String json = gson.toJson(dataSourceConfigs);
            PropertiesComponent.getInstance().setValue(DATASOURCE_CONFIG_KEY, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从持久化存储加载配置
     */
    private void loadConfigurations() {
        try {
            String json = PropertiesComponent.getInstance().getValue(DATASOURCE_CONFIG_KEY);
            if (json != null && !json.trim().isEmpty()) {
                Type listType = new TypeToken<List<DataSourceConfig>>(){}.getType();
                List<DataSourceConfig> configs = gson.fromJson(json, listType);
                if (configs != null) {
                    dataSourceConfigs.clear();
                    dataSourceConfigs.addAll(configs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 如果加载失败，创建默认配置
            createDefaultConfiguration();
        }
        
        // 如果没有配置，创建默认配置
        if (dataSourceConfigs.isEmpty()) {
            createDefaultConfiguration();
        }
    }

    /**
     * 创建默认配置
     */
    private void createDefaultConfiguration() {
        DataSourceConfig defaultConfig = new DataSourceConfig(
                "default-h2",
                "Default H2 Database",
                DataSourceConfig.DatabaseType.H2,
                "jdbc:h2:mem:batch_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "sa",
                ""
        );
        defaultConfig.setDescription("Default H2 in-memory database for testing");
        dataSourceConfigs.add(defaultConfig);
        saveConfigurations();
    }

    /**
     * 导出配置
     */
    public String exportConfigurations() {
        return gson.toJson(dataSourceConfigs);
    }

    /**
     * 导入配置
     */
    public void importConfigurations(String json) {
        try {
            Type listType = new TypeToken<List<DataSourceConfig>>(){}.getType();
            List<DataSourceConfig> configs = gson.fromJson(json, listType);
            if (configs != null) {
                dataSourceConfigs.clear();
                dataSourceConfigs.addAll(configs);
                saveConfigurations();
                notifyConfigChanged();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import configurations: " + e.getMessage(), e);
        }
    }

    /**
     * 清空所有配置
     */
    public void clearAllConfigurations() {
        dataSourceConfigs.clear();
        saveConfigurations();
        notifyConfigChanged();
    }

    /**
     * 配置变更监听器接口
     */
    public interface DataSourceConfigListener {
        void onConfigChanged(List<DataSourceConfig> configurations);
    }
}
