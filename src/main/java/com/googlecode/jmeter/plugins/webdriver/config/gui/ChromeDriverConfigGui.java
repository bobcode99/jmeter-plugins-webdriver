package com.googlecode.jmeter.plugins.webdriver.config.gui;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;

import com.googlecode.jmeter.plugins.webdriver.config.ChromeDriverConfig;

import kg.apc.jmeter.JMeterPluginsUtils;

public class ChromeDriverConfigGui extends WebDriverConfigGui {

    private static final long serialVersionUID = 100L;
    JTextField chromeServicePath;
    JCheckBox androidEnabled;
    private JCheckBox headlessEnabled;
    private JCheckBox insecureCertsEnabled;
    private JCheckBox incognitoEnabled;
    private JCheckBox noSandboxEnabled;
    private JCheckBox disableDevShmUsageEnabled;
    JTextField additionalArgs;
    JTextField binaryPath;

    @Override
    public String getStaticLabel() {
        return JMeterPluginsUtils.prefixLabel("Chrome Driver Config");
    }

    @Override
    public String getLabelResource() {
        return getClass().getCanonicalName();
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if(element instanceof ChromeDriverConfig) {
            ChromeDriverConfig config = (ChromeDriverConfig)element;
            chromeServicePath.setText(config.getChromeDriverPath());
            androidEnabled.setSelected(config.isAndroidEnabled());
            getHeadlessEnabled().setSelected(config.isHeadlessEnabled());
            getInsecureCertsEnabled().setSelected(config.isInsecureCertsEnabled());
            getIncognitoEnabled().setSelected(config.isIncognitoEnabled());
            getNoSandboxEnabled().setSelected(config.isNoSandboxEnabled());
            getDisableDevShmUsageEnabled().setSelected(config.isDisableDevShmUsage());
            additionalArgs.setText(config.getAdditionalArgs());
            binaryPath.setText(config.getBinaryPath());
        }
    }

    @Override
    public TestElement createTestElement() {
        ChromeDriverConfig element = new ChromeDriverConfig();
        modifyTestElement(element);
        return element;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        super.modifyTestElement(element);
        if(element instanceof ChromeDriverConfig) {
            ChromeDriverConfig config = (ChromeDriverConfig)element;
            config.setChromeDriverPath(chromeServicePath.getText());
            config.setAndroidEnabled(androidEnabled.isSelected());
            config.setHeadlessEnabled(getHeadlessEnabled().isSelected());
            config.setInsecureCertsEnabled(getInsecureCertsEnabled().isSelected());
            config.setIncognitoEnabled(getIncognitoEnabled().isSelected());
            config.setNoSandboxEnabled(getNoSandboxEnabled().isSelected());
            config.setDisableDevShmUsage(getDisableDevShmUsageEnabled().isSelected());
            config.setAdditionalArgs(additionalArgs.getText());
            config.setBinaryPath(binaryPath.getText());
        }
    }

    @Override
    public void clearGui() {
        super.clearGui();
        chromeServicePath.setText("path to chromedriver.exe");
        androidEnabled.setSelected(false);
        getHeadlessEnabled().setSelected(false);
        getInsecureCertsEnabled().setSelected(false);
        getIncognitoEnabled().setSelected(false);
        getNoSandboxEnabled().setSelected(false);
        getDisableDevShmUsageEnabled().setSelected(false);
        additionalArgs.setText("");
        binaryPath.setText("");
    }

    @Override
    protected JPanel createBrowserPanel() {
        return createServicePanel();
    }

    @Override
    protected String browserName() {
        return "Chrome";
    }

    @Override
    protected String getWikiPage() {
        return "ChromeDriverConfig";
    }

    private JPanel createServicePanel() {
        final JPanel browserPanel = new VerticalPanel();

        final JPanel chromeServicePanel = new HorizontalPanel();
        final JLabel chromeDriverServiceLabel = new JLabel("Path to Chrome Driver");
        chromeServicePanel.add(chromeDriverServiceLabel);
        chromeServicePath = new JTextField();
        chromeServicePanel.add(chromeServicePath);
        browserPanel.add(chromeServicePanel);

        final JPanel binaryPathPanel = new HorizontalPanel();
        final JLabel binaryPathLabel = new JLabel("Binary path");
        binaryPath = new JTextField();
        binaryPathPanel.add(binaryPathLabel);
        binaryPathPanel.add(binaryPath);
        browserPanel.add(binaryPathPanel);

        androidEnabled = new JCheckBox("Use Chrome on Android");
        browserPanel.add(androidEnabled);

        headlessEnabled = new JCheckBox("Use Chrome headless mode");
        browserPanel.add(getHeadlessEnabled());

        insecureCertsEnabled = new JCheckBox("Allow Insecure Certs");
        browserPanel.add(getInsecureCertsEnabled());

        incognitoEnabled = new JCheckBox("Run in Incognito mode");
        browserPanel.add(getIncognitoEnabled());

        noSandboxEnabled = new JCheckBox("Run in No sandbox mode");
        browserPanel.add(getNoSandboxEnabled());

        disableDevShmUsageEnabled=new JCheckBox("Run in disable dev shm (if run no-gui under docker)");
        browserPanel.add(getDisableDevShmUsageEnabled());

        final JPanel additionalArgsPanel = new HorizontalPanel();
        final JLabel additionalArgsLabel = new JLabel("Additional arguments");
        additionalArgs = new JTextField();
        additionalArgsPanel.add(additionalArgsLabel);
        additionalArgsPanel.add(additionalArgs);
        browserPanel.add(additionalArgsPanel);

        return browserPanel;
    }

	@Override
	protected boolean isProxyEnabled() {
		return true;
	}

	@Override
	protected boolean isExperimentalEnabled() {
		return true;
	}

    public JCheckBox getHeadlessEnabled() {
        return headlessEnabled;
    }
    
    public JCheckBox getInsecureCertsEnabled() {
        return insecureCertsEnabled;
    }

    public JCheckBox getIncognitoEnabled() {
        return incognitoEnabled;
    }

    public JCheckBox getNoSandboxEnabled() {
        return noSandboxEnabled;
    }

    public JCheckBox getDisableDevShmUsageEnabled(){
        return disableDevShmUsageEnabled;
    }
}
