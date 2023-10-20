package com.calmwolfs.islazzifarming.config;

import com.calmwolfs.islazzifarming.config.updater.ConfigVersionDisplay;
import com.calmwolfs.islazzifarming.utils.WebUtils;
import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.Accordion;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigEditorButton;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDropdown;
import io.github.moulberry.moulconfig.annotations.ConfigEditorText;
import io.github.moulberry.moulconfig.annotations.ConfigOption;
import io.github.moulberry.moulconfig.observer.Property;

public class SettingsConfig {
    @ConfigOption(name = "Current Version", desc = "This is your current mod version")
    @ConfigVersionDisplay
    public transient Void currentVersion = null;

    @ConfigOption(name = "Auto Updates", desc = "Automatically check for updates on each startup")
    @Expose
    @ConfigEditorBoolean
    public boolean autoUpdates = true;

    @ConfigOption(name = "Update Stream", desc = "What sort of updates do you want to be notified about")
    @Expose
    @ConfigEditorDropdown
    public Property<UpdateStream> updateStream = Property.of(UpdateStream.RELEASES);

    public enum UpdateStream {
        NONE("None", "none"),
        PRE("Pre", "pre"),
        RELEASES("Full", "full");

        private final String label;
        private final String stream;

        UpdateStream(String label, String stream) {
            this.label = label;
            this.stream = stream;
        }

        public String getStream() {
            return stream;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @Expose
    @ConfigOption(name = "Api Key", desc = "Put your api key here. §eRemember this is against Hypixel's api rules")
    @ConfigEditorText
    public String apiKey = "";

    @Expose
    @ConfigOption(name = "Lazzi Notifications", desc = "")
    @Accordion
    public Notification notification = new Notification();

    public static class Notification {
        @Expose
        @ConfigOption(name = "Notification Corner", desc = "What corner do you want the notification to display in?")
        @ConfigEditorDropdown(values = {"Top Left", "Top Right"})
        public int corner = 0;

        @Expose
        @ConfigOption(name = "Animation Direction", desc = "What direction do you want it to appear from?")
        @ConfigEditorDropdown(values = {"Horizontal", "Vertical"})
        public int direction = 0;

        @ConfigOption(name = "Enable Animations", desc = "Animates the notifications appearing and disappearing")
        @Expose
        @ConfigEditorBoolean
        public boolean animations = true;

        @ConfigOption(name = "Show Items", desc = "Will attempt to show an item relevant to the notification")
        @Expose
        @ConfigEditorBoolean
        public boolean items = true;

        @ConfigOption(name = "Enabled", desc = "Shows notifications for Lazzi farming")
        @Expose
        @ConfigEditorBoolean
        public boolean enabled = true;

        @ConfigOption(name = "Sounds", desc = "Enables a sound for notifications")
        @Expose
        @ConfigEditorBoolean
        public boolean sound = true;
    }

    @ConfigOption(name = "Used Software", desc = "Information about used software and their licenses")
    @Accordion
    public Licenses licenses = new Licenses();

    public static class Licenses {
        @ConfigOption(name = "BedWar Mod", desc = "BedWar Mod is available under the LGPL 3.0 License or later version")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable bedwarMod = () -> WebUtils.openBrowser("https://github.com/BedWarMod/BedWar");

        @ConfigOption(name = "MoulConfig", desc = "MoulConfig is available under the LGPL 3.0 License or later version")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable moulConfig = () -> WebUtils.openBrowser("https://github.com/NotEnoughUpdates/MoulConfig");

        @ConfigOption(name = "NotEnoughUpdates", desc = "NotEnoughUpdates is available under the LGPL 3.0 License or later version")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable notEnoughUpdates = () -> WebUtils.openBrowser("https://github.com/NotEnoughUpdates/NotEnoughUpdates");

        @ConfigOption(name = "SkyHanni", desc = "SkyHanni is available under the GNU Lesser General Public License v2.1")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable skyhanni = () -> WebUtils.openBrowser("https://github.com/hannibal002/SkyHanni");

        @ConfigOption(name = "Forge", desc = "Forge is available under the LGPL 3.0 license")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable forge = () -> WebUtils.openBrowser("https://github.com/MinecraftForge/MinecraftForge");

        @ConfigOption(name = "LibAutoUpdate", desc = "LibAutoUpdate is available under the BSD 2 Clause License")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable libAutoUpdate = () -> WebUtils.openBrowser("https://git.nea.moe/nea/libautoupdate/");
    }
}