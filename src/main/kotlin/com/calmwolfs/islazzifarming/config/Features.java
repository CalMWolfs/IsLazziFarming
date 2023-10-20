package com.calmwolfs.islazzifarming.config;

import com.calmwolfs.islazzifarming.IsLazziFarmingMod;
import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.Config;
import io.github.moulberry.moulconfig.annotations.Category;

public class Features extends Config {
    @Override
    public void saveNow() {
        IsLazziFarmingMod.configManager.saveConfig("close-gui");
    }

    @Override
    public String getTitle() {
        return "IsLazziFarming " + IsLazziFarmingMod.getVersion() + " by §6CalMWolfs§r, config by §5Moulberry §rand §5nea89";
    }

    @Expose
    @Category(name = "Settings", desc = "The Settings")
    public SettingsConfig settings = new SettingsConfig();
}