package com.googlecode.jmeter.plugins.webdriver.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverService;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kg.apc.jmeter.JMeterPluginsUtils;

public class FirefoxDriverConfig extends WebDriverConfig<FirefoxDriver> {

    private static final long serialVersionUID = 100L;
    private static final Logger log = LoggerFactory.getLogger(FirefoxDriverConfig.class);
    
    private static final String FIREFOX_SERVICE_PATH = "FirefoxDriverConfig.firefoxdriver_path";
    private static final String GENERAL_USERAGENT_OVERRIDE = "FirefoxDriverConfig.general.useragent.override";
    private static final String ENABLE_USERAGENT_OVERRIDE = "FirefoxDriverConfig.general.useragent.override.enabled";
    private static final String ENABLE_ACCEPT_INSECURE_CERTS = "FirefoxDriverConfig.general.accept-insecure-certs";
    private static final String ENABLE_HEADLESS = "FirefoxDriverConfig.general.headless";
    private static final String ENABLE_NTML = "FirefoxDriverConfig.network.negotiate-auth.allow-insecure-ntlm-v1";
    private static final String EXTENSIONS_TO_LOAD = "FirefoxDriverConfig.general.extensions";
    private static final String PREFERENCES = "FirefoxDriverConfig.general.preferences";
    private static final Map<String, FirefoxDriverService> services = new ConcurrentHashMap<String, FirefoxDriverService>();

    public void setFirefoxDriverPath(String path) {
        setProperty(FIREFOX_SERVICE_PATH, path);
    }

    public String getFirefoxDriverPath() {
        return getPropertyAsString(FIREFOX_SERVICE_PATH);
    }

	// Used only in unit tests
	private Boolean bUnitTests = false;
	public void enableUnitTests() {
		bUnitTests = true;
	}
    
    FirefoxOptions createOptions() {
    	FirefoxOptions options = new FirefoxOptions();
    	options.setProxy(createProxy());
    	options.setProfile(createProfile());
        options.setHeadless(isHeadless());
        options.setAcceptInsecureCerts(isAcceptInsecureCerts());
        return options;
    }

    FirefoxProfile createProfile() {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("app.update.enabled", false);
        
        String userAgentOverride = getUserAgentOverride();
        if (StringUtils.isNotEmpty(userAgentOverride)) {
            profile.setPreference("general.useragent.override", userAgentOverride);
        }
        
        String ntlmOverride = getNtlmSetting();
        if (StringUtils.isNotEmpty(ntlmOverride)) {
            profile.setPreference("network.negotiate-auth.allow-insecure-ntlm-v1", true);
        }

        addExtensions(profile);
        setPreferences(profile);

        return profile;
    }

    private void addExtensions(FirefoxProfile profile) {
        JMeterProperty property = getProperty(EXTENSIONS_TO_LOAD);
        if (property instanceof NullProperty) {
            return;
        }
        CollectionProperty rows = (CollectionProperty) property;
        for (int i = 0; i < rows.size(); i++) {
            ArrayList row = (ArrayList) rows.get(i).getObjectValue();
            String filename = ((JMeterProperty) row.get(0)).getStringValue();
            profile.addExtension(new File(filename));
        }
    }

    private void setPreferences(FirefoxProfile profile) {
        JMeterProperty property = getProperty(PREFERENCES);
        if (property instanceof NullProperty) {
            return;
        }
        CollectionProperty rows = (CollectionProperty) property;
        for (int i = 0; i < rows.size(); i++) {
            ArrayList row = (ArrayList) rows.get(i).getObjectValue();
            String name = ((JMeterProperty) row.get(0)).getStringValue();
            String value = ((JMeterProperty) row.get(1)).getStringValue();
            switch (value) {
                case "true":
                    profile.setPreference(name, true);
                    break;
                case "false":
                    profile.setPreference(name, false);
                    break;
                default:
                    profile.setPreference(name, value);
                    break;
            }
        }
    }

    Map<String, FirefoxDriverService> getServices() {
        return services;
    }

    @Override
    protected FirefoxDriver createBrowser() {
        final FirefoxDriverService service = getThreadService();
        FirefoxOptions options = createOptions();
        return service != null ? new FirefoxDriver(service, options) : null;
    }

    private FirefoxDriverService getThreadService() {
        FirefoxDriverService service = services.get(currentThreadName());
        if (service != null) {
            return service;
        }
        try {
			if (bUnitTests) {
			    service = GeckoDriverService.createDefaultService();
			} else {
                service = new GeckoDriverService.Builder().usingDriverExecutable(new File(getFirefoxDriverPath())).build();
			}
            service.start();
            services.put(currentThreadName(), service);
        } catch (IOException e) {
            log.error("Failed to start Firefox service");
            service = null;
        }
        return service;
    }

    public boolean isAcceptInsecureCerts() {
        return getPropertyAsBoolean(ENABLE_ACCEPT_INSECURE_CERTS);
    }

    public void setAcceptInsecureCerts(boolean acceptInsecureCerts) {
        setProperty(ENABLE_ACCEPT_INSECURE_CERTS, acceptInsecureCerts);
    }

    public boolean isHeadless() {
        return getPropertyAsBoolean(ENABLE_HEADLESS);
    }

    public void setHeadless(boolean headless) {
        setProperty(ENABLE_HEADLESS, headless);
    }

    public void setUserAgentOverride(String userAgent) {
        setProperty(GENERAL_USERAGENT_OVERRIDE, userAgent);
    }

    public String getUserAgentOverride() {
        return getPropertyAsString(GENERAL_USERAGENT_OVERRIDE);
    }

    public boolean isUserAgentOverridden() {
        return getPropertyAsBoolean(ENABLE_USERAGENT_OVERRIDE);
    }

    public void setUserAgentOverridden(boolean userAgentOverridden) {
        setProperty(ENABLE_USERAGENT_OVERRIDE, userAgentOverridden);
    }

    public void setNtlmSetting(boolean ntlm) {
        setProperty(ENABLE_NTML, ntlm);
    }

    public String getNtlmSetting() {
        return getPropertyAsString(ENABLE_NTML);
    }

    public void setExtensions(PowerTableModel model) {
        CollectionProperty prop = JMeterPluginsUtils.tableModelRowsToCollectionProperty(model, EXTENSIONS_TO_LOAD);
        setProperty(prop);
    }

    public void setPreferences(PowerTableModel model) {
        CollectionProperty prop = JMeterPluginsUtils.tableModelRowsToCollectionProperty(model, PREFERENCES);
        setProperty(prop);
    }

    public JMeterProperty getExtensions() {
        return getProperty(EXTENSIONS_TO_LOAD);
    }

    public JMeterProperty getPreferences() {
        return getProperty(PREFERENCES);
    }
}
