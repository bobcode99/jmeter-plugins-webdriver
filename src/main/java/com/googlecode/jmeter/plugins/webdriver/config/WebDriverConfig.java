package com.googlecode.jmeter.plugins.webdriver.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jmeter.plugins.webdriver.proxy.ProxyFactory;
import com.googlecode.jmeter.plugins.webdriver.proxy.ProxyHostPort;
import com.googlecode.jmeter.plugins.webdriver.proxy.ProxyType;

public abstract class WebDriverConfig<T extends WebDriver> extends ConfigTestElement implements LoopIterationListener, ThreadListener {

    private static final long serialVersionUID = 100L;
	private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverConfig.class);

    /**
     * This is the key used to store a WebDriver instance in the {@link org.apache.jmeter.threads.JMeterVariables} object.
     */
    public static final String BROWSER = "Browser";

    /**
     * Ideally we would have stored the WebDriver instances in the JMeterVariables object, however the JMeterVariables
     * is cleared BEFORE threadFinished() callback is called (hence would never be able to quit the WebDriver).
     */
    // PATCH: Added changes to stow the WebDriver in both places, regardless. This is so other samplers who might
    // be interested in leveraging the WebDriver instance can get a handle to it without too much trouble.
    private static final Map<String, WebDriver> webdrivers = new ConcurrentHashMap<String, WebDriver>();

    private static final String PROXY_PAC_URL = "WebDriverConfig.proxy_pac_url";
    private static final String HTTP_HOST = "WebDriverConfig.http_host";
    private static final String HTTP_PORT = "WebDriverConfig.http_port";
    private static final String USE_HTTP_FOR_ALL_PROTOCOLS = "WebDriverConfig.use_http_for_all_protocols";
    private static final String HTTPS_HOST = "WebDriverConfig.https_host";
    private static final String HTTPS_PORT = "WebDriverConfig.https_port";
    private static final String FTP_HOST = "WebDriverConfig.ftp_host";
    private static final String FTP_PORT = "WebDriverConfig.ftp_port";
    private static final String SOCKS_HOST = "WebDriverConfig.socks_host";
    private static final String SOCKS_PORT = "WebDriverConfig.socks_port";
    private static final String NO_PROXY = "WebDriverConfig.no_proxy";
    private static final String PROXY_TYPE = "WebDriverConfig.proxy_type";
    private static final String MAXIMIZE_WINDOW = "WebDriverConfig.maximize_browser";

    /*
     * THE FOLLOWING CONFIGS ARE EXPERIMENTAL AND ARE SUBJECT TO CHANGE/REMOVAL.
     */
    private static final String RECREATE_ON_ITERATION_START = "WebDriverConfig.reset_per_iteration";
    private static final String DEV_MODE = "WebDriverConfig.dev_mode";

    private final transient ProxyFactory proxyFactory;

    protected WebDriverConfig() {
        this(ProxyFactory.getInstance());
    }

    protected WebDriverConfig(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    public void setProxyPacUrl(String pacUrl) {
        setProperty(PROXY_PAC_URL, pacUrl);
    }

    public String getProxyPacUrl() {
        return getPropertyAsString(PROXY_PAC_URL);
    }

    public void setHttpHost(String host) {
        setProperty(HTTP_HOST, host);
    }

    public String getHttpHost() {
        return getPropertyAsString(HTTP_HOST);
    }

    public void setHttpPort(int port) {
        setProperty(HTTP_PORT, port);
    }

    public int getHttpPort() {
        return getPropertyAsInt(HTTP_PORT);
    }

    public void setUseHttpSettingsForAllProtocols(boolean override) {
        setProperty(USE_HTTP_FOR_ALL_PROTOCOLS, override);
    }

    public boolean isUseHttpSettingsForAllProtocols() {
        return getPropertyAsBoolean(USE_HTTP_FOR_ALL_PROTOCOLS, true);
    }

    public void setHttpsHost(String httpsHost) {
        setProperty(HTTPS_HOST, httpsHost);
    }

    public String getHttpsHost() {
        return getPropertyAsString(HTTPS_HOST);
    }

    public void setHttpsPort(int port) {
        setProperty(HTTPS_PORT, port);
    }

    public int getHttpsPort() {
        return getPropertyAsInt(HTTPS_PORT);
    }

    public void setFtpHost(String host) {
        setProperty(FTP_HOST, host);
    }

    public String getFtpHost() {
        return getPropertyAsString(FTP_HOST);
    }

    public void setFtpPort(int port) {
        setProperty(FTP_PORT, port);
    }

    public int getFtpPort() {
        return getPropertyAsInt(FTP_PORT);
    }

    public void setSocksHost(String host) {
        setProperty(SOCKS_HOST, host);
    }

    public String getSocksHost() {
        return getPropertyAsString(SOCKS_HOST);
    }

    public void setSocksPort(int port) {
        setProperty(SOCKS_PORT, port);
    }

    public int getSocksPort() {
        return getPropertyAsInt(SOCKS_PORT);
    }

    public void setNoProxyHost(String noProxyHostList) {
        setProperty(NO_PROXY, noProxyHostList);
    }

    public String getNoProxyHost() {
        return getPropertyAsString(NO_PROXY);
    }

    public void setProxyType(ProxyType type) {
        setProperty(PROXY_TYPE, type.name());
    }

    public ProxyType getProxyType() {
        return ProxyType.valueOf(getPropertyAsString(PROXY_TYPE, ProxyType.SYSTEM.name()));
    }

    public boolean isBrowserMaximized() {
        return getPropertyAsBoolean(MAXIMIZE_WINDOW, false);
    }

    public void setBrowserMaximized(boolean state) {
        setProperty(MAXIMIZE_WINDOW, state);
    }

    /**
     * Call this method to create a {@link Proxy} instance for use when creating a {@link org.openqa.selenium.WebDriver}
     * instance.  The values/settings of the proxy depends entirely on the values set on this config instance.
     *
     * @return a {@link Proxy}
     */
    public Proxy createProxy() {
        switch (getProxyType()) {
            case PROXY_PAC:
                return proxyFactory.getConfigUrlProxy(getProxyPacUrl());
            case DIRECT:
                return proxyFactory.getDirectProxy();
            case AUTO_DETECT:
                return proxyFactory.getAutodetectProxy();
            case MANUAL:
                if (isUseHttpSettingsForAllProtocols()) {
                    ProxyHostPort proxy = new ProxyHostPort(getHttpHost(), getHttpPort());
                    return proxyFactory.getManualProxy(proxy, proxy, proxy, proxy, getNoProxyHost());
                }
                ProxyHostPort http = new ProxyHostPort(getHttpHost(), getHttpPort());
                ProxyHostPort https = new ProxyHostPort(getHttpsHost(), getHttpsPort());
                ProxyHostPort ftp = new ProxyHostPort(getFtpHost(), getFtpPort());
                ProxyHostPort socks = new ProxyHostPort(getSocksHost(), getSocksPort());
                return proxyFactory.getManualProxy(http, https, ftp, socks, getNoProxyHost());
            default:
                return proxyFactory.getSystemProxy();
        }
    }

    @Override
    public void iterationStart(LoopIterationEvent loopIterationEvent) {
        if (isRecreateBrowserOnIterationStart() && !isDevMode()) {
            final T browser = getThreadBrowser();
            quitBrowser(browser);
            setThreadBrowser(getPreparedBrowser());
            LOGGER.info("Created browser object: " + browser);
        }
    }

    @Override
    public void threadStarted() {
        // don't create new browser if there is one there already
        if (hasThreadBrowser()) {
            LOGGER.warn("Thread: " + currentThreadName() + " already has a WebDriver(" + getThreadBrowser() + ") associated with it. ThreadGroup can only contain a single WebDriverConfig.");
            return;
        }

        // create new browser instance
        final T browser = getPreparedBrowser();
        setThreadBrowser(browser);

        // ensures the browser will quit when JVM exits (especially important in devMode)
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                quitBrowser(browser);
            }
        });
    }

    private T getPreparedBrowser() {
        T browser = createBrowser();
        if (isBrowserMaximized()) {
            browser.manage().window().maximize();
        }
        return browser;
    }

    @Override
    public void threadFinished() {
        if (!isDevMode()) {
            final T browser = removeThreadBrowser();
            quitBrowser(browser);
        }
    }


    protected String currentThreadName() {
        return Thread.currentThread().getName();
    }

    /**
     * Creates a new browser at the start of the tests. This method will always return a new instance of a {@link WebDriver}
     * class and is called per thread.
     *
     * @return a new {@link WebDriver} object.
     */
    protected abstract T createBrowser();

    /**
     * Quits browser at the end of the tests. This will be envoked per thread/browser instance created.
     *
     * @param browser is the browser instance to quit. Will not quit if argument is null.
     */
    protected void quitBrowser(final T browser) {
        if (browser != null) {
            try {
                browser.quit();
            } catch (SessionNotCreatedException e) {
                LOGGER.warn("Attempting to quit browser instance that has already exited.");
            }
        }
    }

    protected T getThreadBrowser() {
        return (T) webdrivers.get(currentThreadName());
    }

    protected boolean hasThreadBrowser() {
        if (webdrivers.containsKey(currentThreadName())) {
            WebDriver browser = webdrivers.get(currentThreadName());
            try {
                browser.getCurrentUrl();
                return true;
            } catch (Exception ex) {
                LOGGER.warn("Old browser object is inaccessible, will create new", ex);
                webdrivers.remove(currentThreadName());
            }
        }
        return false;
    }

    protected void setThreadBrowser(T browser) {
        if (browser != null) {
            webdrivers.put(currentThreadName(), browser);
            // Make sure we stow the object in both places, in case someone wants a copy
            if (getThreadContext().getVariables() != null)
            {
                getThreadContext().getVariables().putObject(WebDriverConfig.BROWSER, browser);
            }
        }
    }

    protected T removeThreadBrowser() {
        return (T) webdrivers.remove(currentThreadName());
    }

    void clearThreadBrowsers() {
        webdrivers.clear();
    }

    Map<String, WebDriver> getThreadBrowsers() {
        return webdrivers;
    }

    public boolean isRecreateBrowserOnIterationStart() {
        return getPropertyAsBoolean(RECREATE_ON_ITERATION_START);
    }

    public void setRecreateBrowserOnIterationStart(boolean recreate) {
        setProperty(RECREATE_ON_ITERATION_START, recreate);
    }

    public boolean isDevMode() {
        return getPropertyAsBoolean(DEV_MODE);
    }

    public void setDevMode(boolean devMode) {
        setProperty(DEV_MODE, devMode);
    }
}