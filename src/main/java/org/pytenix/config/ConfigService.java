package org.pytenix.config;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;

public class ConfigService {



    private final Yaml yaml;
    private final String fileName;



    @Getter
    Configuration configuration;

    public ConfigService(File dataFolder) {

        this.fileName = new File(dataFolder, "config.yaml").getAbsolutePath();

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(true);

        Representer representer = new Representer(options);
        representer.addClassTag(Object.class, Tag.MAP);

        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setTagInspector(tag -> true);
        this.yaml = new Yaml(new Constructor(Object.class, loaderOptions), representer, options);


        if(!existsConfig())
            saveConfig(Configuration.defaultConfiguration());

        this.configuration = loadConfig();

        if(this.configuration == null)
        {
            System.err.println("Error loading configuration from file, loading default configuration!");
            this.configuration = Configuration.defaultConfiguration();
        }
    }

    public void reloadConfig() {
        Configuration reloaded = loadConfig();
        if (reloaded != null) {
            this.configuration = reloaded;
        } else {
            System.err.println("Fehler beim Neuladen der Config!");
        }
    }

    public boolean existsConfig()
    {
        return new File(fileName).exists();
    }

    public void saveConfig(Configuration configuration)
    {
        if(!saveFile(configuration, new File(fileName)))
            System.err.println("Error saving configuration!");
    }

    public @Nullable Configuration loadConfig()
    {
        return loadFile(Configuration.class, new File(fileName));
    }





    public boolean saveFile(Object data, File file) {
        try (Writer writer = new FileWriter(file)) {
            yaml.dump(data, writer);
            return true;
        } catch (IOException e) {
            System.err.println("[Config] Fehler beim Speichern: " + e.getMessage());
            return false;
        }
    }

    public <T> @Nullable T loadFile(Class<T> clazz, File file) {
        if (!file.exists())
            return null;


        try (InputStream inputStream = new FileInputStream(file)) {
            LoaderOptions loaderOptions = new LoaderOptions();
            loaderOptions.setTagInspector(tag -> true);
            Object loaded = new Yaml(new Constructor(clazz, loaderOptions)).load(inputStream);
            return clazz.cast(loaded);
        } catch (Exception e) {
            System.err.println("[Config] Fehler beim Laden von " + file.getName() + ": " + e.getMessage());
            return null;
        }
    }
}