/*
* Licensed Materials - Property of IBM* and/or HCL**
* UrbanCode Deploy
* UrbanCode Build
* UrbanCode Release
* AnthillPro
* (c) Copyright IBM Corporation 2011, 2017. All Rights Reserved.
* (c) Copyright HCL Technologies Ltd. 2018. All Rights Reserved.
*
* U.S. Government Users Restricted Rights - Use, duplication or disclosure restricted by
* GSA ADP Schedule Contract with IBM Corp.
*
* * Trademark of International Business Machines
* ** Trademark of HCL Technologies Limited
*/


/*
This script requires the Groovy scripting language.  You can find Groovy at
http://groovy.codehaus.org/.  Download Groovy at http://dist.codehaus.org/groovy/distributions/.
To install it, follow the instructions at http://groovy.codehaus.org/install.html.

Automatic import packages & classes:
  java.io/lang/net/util
  java.math.BigDecimal/BigInteger
  groovy.lang/util
*/
import groovy.json.JsonOutput;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.Certificate
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringEscapeUtils
import org.apache.tools.ant.*
import org.apache.tools.ant.taskdefs.condition.Os

import com.urbancode.air.keytool.Extension
import com.urbancode.air.keytool.KeytoolHelper
import com.urbancode.air.validation.WebAgentProxyValidationRule
import com.urbancode.air.validation.WebJMSValidationRule
import com.urbancode.commons.util.IO
import com.urbancode.commons.util.SortedProperties;
import com.urbancode.commons.util.unix.Unix
import com.urbancode.commons.util.agent.AgentVersionHelper
import com.urbancode.commons.util.crypto.CryptStringUtil
import com.urbancode.commons.util.crypto.SecureRandomHelper
import com.urbancode.commons.validation.ValidationException
import com.urbancode.commons.validation.ValidationRules
import com.urbancode.commons.validation.format.JreHomeValidationRule
import com.urbancode.commons.validation.format.NoSpaceValidationRule
import com.urbancode.commons.validation.format.RequiredValueValidationRule
import com.urbancode.commons.validation.format.SocketPortValidationRule
import com.urbancode.commons.validation.format.WebUriValidationRule
import com.urbancode.commons.validation.format.YesNoValidationRule
import com.urbancode.commons.validation.rules.NumericValueRule
import com.urbancode.shell.Os

import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.*
import com.urbancode.commons.httpcomponentsutil.HttpClientBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils
import groovy.json.JsonSlurper

class AgentInstaller {

    boolean isUnix = Os.isFamily("unix")
    boolean isWindows = Os.isFamily("windows")
    boolean isZos = Os.isFamily("z/os")

    def agentEncoding = "UTF-8"
    def systemEncoding = getSystemEncoding()
    def systemIn = System.in.newReader(systemEncoding)


    def NL = System.getProperty('line.separator')

    def nonInteractive = false
    def installzOSDeploymentTools = false

    def ant = null

    def doUpgrade = false;

    def productName = null
    def productPrefix = null
    def productCapitalName = null

    def srcDir = null
    def unpackDir = null
    def javaHome = null
    def currentJavaHome = null



    private class AgentInstallOptions {
        def installPropertiesFile = null
        def installOs = null
        def installArch = null
        def installAsService = null
        def installServiceName = null
        def installServiceLogin = null
        def installServicePassword = null
        def installServiceAutostart = null
        def doServiceInstall = null

        def installAgentType = null;
        def installAgentBinaryDir = null;
        def installAgentConfigDir = null;
        def installAgentId = null
        def installAgentName = null
        def generateAgentName = false
        def installAgentTeams = null

        def installAgentRemoteHost = null
        def installAgentRemotePort = null
        def installAgentBrokerUrl = null
        def installAgentProxyHost = null
        def installAgentProxyPort = null
        def installDisableHttpFailover = null

        def installAgentRemoteHostList = []
        def installAgentRemotePortList = []
        def installAgentProxyHostList = []
        def installAgentProxyPortList = []
        def installBrokerURL = null

        def agentCommWebServerUri = null

        def agentCommWebEnabled = null
        def agentCommWebServerUriList = []
        def agentCommProxyUriList = []

        def installAgentMutualAuth = null
        def disableFullEncryption = null
        def installAgentKeystore = null
        def installAgentKeystorePwd = null

        def installAgentVerifyServerIdentity = null
        def installAgentServerUrl = null

        def installInitUser = null
        def installInitGroup = null

        def encryptionKeyStorePath = null
        def keyStorePassword = null
        def encryptionKeystoreAlias = null

        def installAgentBinaryDirTokenReplacement = null
        def installAgentConfigDirTokenReplacement = null
        def javaHomeTokenReplacement = null
        def javaHome = null;
        def hostName = null

        def zTkServerURL = null
        def zTkServerToken = null
        def zTkMvshlq = null
        def zTkMvsvolume = null
        def zTkRepositoryType = null
        def zTkSmpe = false
        def zPrepareForUpgrade = false
        def zSkipToolkitInstall = false
    }

    def portValidator = null
    def yesNoValidator = null
    def jreHomeValidator = null
    def optionalValidator = null
    def requiredValidator = null
    def numberValidator = null
    def serviceNameValidator = null
    def webJmsValidator = null
    def webUriValidator = null
    def webAgentProxyValidator = null


    def val = null // for groovy bug work-around

    def classpath = null

    AgentInstaller(classpath) {
        def requiredValidationRule = new RequiredValueValidationRule()

        optionalValidator = new ValidationRules()

        requiredValidator = new ValidationRules()
        requiredValidator.addRule(requiredValidationRule)

        portValidator = new ValidationRules()
        portValidator.addRule(requiredValidationRule)
        portValidator.addRule(new SocketPortValidationRule())

        yesNoValidator = new ValidationRules()
        yesNoValidator.addRule(requiredValidationRule)
        yesNoValidator.addRule(new YesNoValidationRule())

        jreHomeValidator = new ValidationRules()
        jreHomeValidator.addRule(requiredValidationRule)
        jreHomeValidator.addRule(new JreHomeValidationRule());

        def numericValidationRule = new NumericValueRule()
        numericValidationRule.setLowerBound(1)
        numericValidationRule.setUpperBound(Integer.MAX_VALUE)
        numberValidator = new ValidationRules()
        numberValidator.addRule(requiredValidationRule)
        numberValidator.addRule(numericValidationRule)

        serviceNameValidator = new ValidationRules();
        serviceNameValidator.addRule(requiredValidationRule);
        serviceNameValidator.addRule(new NoSpaceValidationRule());

        webJmsValidator = new ValidationRules();
        webJmsValidator.addRule(requiredValidationRule);
        webJmsValidator.addRule(new WebJMSValidationRule());

        webUriValidator = new ValidationRules();
        webUriValidator.addRule(requiredValidationRule);
        webUriValidator.addRule(new WebUriValidationRule());

        webAgentProxyValidator = new ValidationRules();
        webAgentProxyValidator.addRule(requiredValidationRule);
        webAgentProxyValidator.addRule(new WebAgentProxyValidationRule());

        this.classpath = classpath
    }

    void setAntBuilder(antBuilder) {
        ant = new AntBuilder(antBuilder.project)
        // have to do this, otherwise properties don't work right
        antBuilder.project.copyInheritedProperties(ant.project)
        antBuilder.project.copyUserProperties(ant.project)
    }

    AgentInstallOptions initNonInteractiveProperties() {
        AgentInstallOptions opts = new AgentInstallOptions();
        def installAgentDir = ant.project.properties.'install.agent.dir';
        if (installAgentDir != null) {
            opts.installAgentBinaryDir = new File(installAgentDir).getAbsolutePath();
            opts.installAgentConfigDir = opts.installAgentBinaryDir;
        }

        srcDir = ant.project.properties.'src.dir'

        if (ant.project.properties.'locked/agent.home') {
            installAgentDir = ant.project.properties.'locked/agent.home'
            opts.installAgentBinaryDir = new File(installAgentDir).getAbsolutePath();
            opts.installAgentConfigDir = opts.installAgentBinaryDir;
        }
        if (ant.project.properties.'locked/agent.install.type') {
            opts.installAgentType = ant.project.properties.'locked/agent.install.type'
            if (ant.project.properties.'locked/agent.binary.home') {
                def installAgentBinaryDir = ant.project.properties.'locked/agent.binary.home'
                opts.installAgentBinaryDir = new File(installAgentBinaryDir).getAbsolutePath();
            }
        }

        // properties used when constructing an install package (msi, etc)
        if (ant.project.properties.'package.replacement.agent.dir') {
            opts.installAgentBinaryDirTokenReplacement = ant.project.properties.'package.replacement.agent.dir';
            opts.installAgentConfigDirTokenReplacement = opts.installAgentBinaryDirTokenReplacement;
        }
        if (ant.project.properties.'package.replacement.java.home') {
            opts.javaHomeTokenReplacement = ant.project.properties.'package.replacement.java.home'
        }
        if (ant.project.properties.'package.java.home') {
            opts.javaHome = ant.project.properties.'package.java.home'
        }
        if (ant.project.properties.'package.os') {
            opts.installOs = ant.project.properties.'package.os'
        }
        if (ant.project.properties.'package.arch') {
            opts.installArch = ant.project.properties.'package.arch'
        }
        if (ant.project.properties.'package.generate.agent.name') {
            opts.generateAgentName = Boolean.valueOf(ant.project.properties.'package.generate.agent.name');
        }
        if (ant.project.properties.'package.init.user') {
            opts.installInitUser = ant.project.properties.'package.init.user'
        }
        if (ant.project.properties.'package.init.group') {
            opts.installInitGroup = ant.project.properties.'package.init.group'
        }

        if (opts.javaHome == null) {
            opts.javaHome = System.getenv().'JAVA_HOME'
        }
        if (opts.javaHome == null) {
            opts.javaHome = System.getProperty('java.home')
        }

        // Protection against invalid java paths set in the install props or JAVA_HOME
        jreHomeValidator.validate(opts.javaHome)

        if (ant.project.properties.'install.properties.file') {
            opts.installPropertiesFile = ant.project.properties.'install.properties.file'
        }

        initInstalledProperties(opts);
        return opts;
    }

    void initInstalledProperties(AgentInstallOptions opts) {
        // always written by installer:
        // locked/agent.brokerUrl
        // locked/agent.home
        // locked/ant.home

        // properties from the existing agent's installed.properties
        if (ant.project.properties.'locked/agent.id') {
            opts.installAgentId = ant.project.properties.'locked/agent.id'
        }
        if (ant.project.properties.'locked/agent.name') {
            opts.installAgentName = ant.project.properties.'locked/agent.name'
        }
        if (ant.project.properties.'locked/agent.initial.teams') {
            opts.installAgentTeams = ant.project.properties.'locked/agent.initial.teams'
        }
        if (ant.project.properties.'locked/agent.jms.remote.host') {
            opts.installAgentRemoteHostList.addAll(ant.project.properties.'locked/agent.jms.remote.host'.split(','))
        }
        if (ant.project.properties.'locked/agent.jms.remote.port') {
            opts.installAgentRemotePortList.addAll(ant.project.properties.'locked/agent.jms.remote.port'.split(','))
        }
        // validate the input for agent.jms.remote.* properties
        if (opts.installAgentRemoteHostList.size() != opts.installAgentRemotePortList.size()) {
            throw new IllegalArgumentException(
                "You only specified " +
                opts.installAgentRemotePortList.size() +
                " ports. Expected " +
                opts.installAgentRemoteHostList.size() + ".")
        }
        for (int i = 0; i < opts.installAgentRemotePortList.size(); i++) {
            portValidator.validate(opts.installAgentRemotePortList.get(i))
        }
        if (ant.project.properties.'locked/agent.http.proxy.host') {
            opts.installAgentProxyHost = ant.project.properties.'locked/agent.http.proxy.host'
        }
        if (ant.project.properties.'locked/agent.http.proxy.port') {
            opts.installAgentProxyPort = ant.project.properties.'locked/agent.http.proxy.port'
        }
        if (ant.project.properties.'locked/agent.mutual_auth') {
            opts.installAgentMutualAuth = ant.project.properties.'locked/agent.mutual_auth'
        }

        if (ant.project.properties.'agent.jms.disable_full_encryption') {
            opts.disableFullEncryption = ant.project.properties.'agent.jms.disable_full_encryption'
        }

        if (ant.project.properties.'locked/agent.keystore') {
            opts.installAgentKeystore = ant.project.properties.'locked/agent.keystore'
        }
        if (ant.project.properties.'locked/agent.keystore.pwd') {
            opts.installAgentKeystorePwd = CryptStringUtil.decrypt(ant.project.properties.'locked/agent.keystore.pwd')
        }
        if (ant.project.properties.'locked/agent.service') {
            opts.installAsService = ant.project.properties.'locked/agent.service'
        }
        if (ant.project.properties.'locked/agent.service.name') {
            opts.installServiceName = ant.project.properties.'locked/agent.service.name'
        }
        if (ant.project.properties.'locked/agent.service.login') {
            opts.installServiceLogin = ant.project.properties.'locked/agent.service.login'
            opts.installServiceLogin = opts.installServiceLogin.replaceAll("(\\\\\\\\)+", "\\\\");
            opts.installServiceLogin = opts.installServiceLogin.replace("\\\\", "\\")
        }
        if (ant.project.properties.'locked/agent.service.password') {
            opts.installServicePassword = CryptStringUtil.decrypt(ant.project.properties.'locked/agent.service.password')
        }
        if (ant.project.properties.'locked/agent.service.autostart') {
            opts.installServiceAutostart = ant.project.properties.'locked/agent.service.autostart'
        }
        if (ant.project.properties[productName + "/java.home"]) {
            opts.javaHome = ant.project.properties[productName + "/java.home"]
            currentJavaHome = opts.javaHome
        }
        if (ant.project.properties['agent.HttpFailoverHandler.disabled']) {
            opts.installDisableHttpFailover = ant.project.properties.'agent.HttpFailoverHandler.disabled'
        }
        if (ant.project.properties.'locked/agent.brokerUrl') {
            opts.installBrokerURL = ant.project.properties.'locked/agent.brokerUrl'
        }
        if (ant.project.properties.'verify.server.identity') {
            opts.installAgentVerifyServerIdentity = ant.project.properties.'verify.server.identity'
        }
        if (ant.project.properties.'server.url') {
            opts.installAgentServerUrl = ant.project.properties.'server.url'
            opts.zTkServerURL = ant.project.properties.'server.url'
        }
        if (ant.project.properties.'encryption.keystore') {
            opts.encryptionKeyStorePath = ant.project.properties.'encryption.keystore'
        }
        if (ant.project.properties.'encryption.keystore.password') {
            opts.keyStorePassword = CryptStringUtil.class.decrypt(ant.project.properties.'encryption.keystore.password')
        }
        if (ant.project.properties.'encryption.keystore.alias') {
            opts.encryptionKeystoreAlias = ant.project.properties.'encryption.keystore.alias'
        }
        if (ant.project.properties.'server.token') {
            opts.zTkServerToken = CryptStringUtil.decrypt(ant.project.properties.'server.token')
        }
        if (ant.project.properties.'repository.type') {
            opts.zTkRepositoryType = ant.project.properties.'repository.type'
        }
        if (ant.project.properties.'mvs.hlq') {
            opts.zTkMvshlq = ant.project.properties.'mvs.hlq'
        }
        if (ant.project.properties.'mvs.volume') {
            opts.zTkMvsvolume = ant.project.properties.'mvs.volume'
        }

        if (ant.project.properties.'agentcomm.enabled') {
            opts.agentCommWebEnabled = Boolean.valueOf(ant.project.properties.'agentcomm.enabled')
        }
        if (ant.project.properties.'experimental.comm2017.enabled') {
            opts.agentCommWebEnabled = Boolean.valueOf(ant.project.properties.'experimental.comm2017.enabled')
        }
        if (!this.nonInteractive && !opts.agentCommWebEnabled) {
            // They are manually upgrading we should explicitly ask them again
            opts.agentCommWebEnabled = null;
        }
        if (this.nonInteractive && opts.agentCommWebEnabled == null) {
            // Its an automated upgrade we should assume jms since we were not provided a value
            opts.agentCommWebEnabled = false;
        }

        if (ant.project.properties.'agentcomm.server.uri') {
            def uris = derandomizeList(ant.project.properties.'agentcomm.server.uri')
            opts.agentCommWebServerUriList.addAll(uris.split(','))
        }
        if (ant.project.properties.'experimental.comm2017.agent.server.uri') {
            def uris = ant.project.properties.'experimental.comm2017.agent.server.uri'
            opts.agentCommWebServerUriList.addAll(uris.split(','))
        }

        if (ant.project.properties.'agentcomm.proxy.uri') {
            def addresses = derandomizeList(ant.project.properties.'agentcomm.proxy.uri')
            opts.agentCommProxyUriList.addAll(addresses.split(','))
        }
        if (ant.project.properties.'experimental.comm2017.agent.proxy.address') {
            def addresses = ant.project.properties.'experimental.comm2017.agent.proxy.address'
            opts.agentCommProxyUriList.addAll(addresses.split(',').collect{it.trim()}.findAll{!it.empty}.collect{"http://${it}"})
        }
    }

    String derandomizeList(list) {
        def result = list
        if (list.startsWith("random:(")) {
            result = result.substring(8) // remove 'random:('
            if (list.endsWith(")")) {
                result = result.substring(0, result.length()-1) // remove ')'
            }
        }
        return result
    }

    String randomizeList(list) {
        def result = "random:(" + list + ")"
        return result
    }

    void setNonInteractive(mode) {
        this.nonInteractive = mode
    }

    void install(productName, productPrefix, productCapitalName) {
        this.productName = productName
        this.productPrefix = productPrefix
        this.productCapitalName = productCapitalName
        def type = ant.project.properties['agent.install.type'];
        AgentInstallOptions opts = initNonInteractiveProperties()
        if(opts.installAgentType){
            type = opts.installAgentType;
        }
        if (type && type == "binary") {
            getInstallDir("agent_binary", opts, "installAgentBinaryDir", true);
            readInstalledPropertiesForZos(opts)
            collectZosDatasetInfomation(opts);
            doBinaryInstall(opts);
            completeZosToolkitInstall(opts);
            System.exit(0);
        }
        else if (type && type == "config") {
            getBinaryDirectoryForConfigInstall(opts);
            getInstallDir("agent", opts, "installAgentConfigDir",true);
            readInstalledProperties(opts);
            if (!checkForUpgrade(opts.installAgentConfigDir, opts.installServiceName)) {
                collectInstallProperties(opts);
                collectZosToolkitSettings(opts);
                doConfigInstall(opts);
                completeZosToolkitInstall(opts);
                System.exit(0);
            }
            else {
                throw new Exception("Upgrading of config installs is not supported. Upgrade binary and then restart agent to upgrade this agent.");
            }
        }
        else if (type){
            throw new Exception("Unknown install type. Valid value for agent.install.type is binary or config.");
        }
        else {
            getInstallDir("agent", opts, "installAgentBinaryDir", true);
            opts.installAgentConfigDir = opts.installAgentBinaryDir;
            readInstalledProperties(opts);
            if (!checkForUpgrade(opts.installAgentConfigDir, opts.installServiceName)) {
                collectZosDatasetInfomation(opts);
                collectInstallProperties(opts);
                collectZosToolkitSettings(opts);
                doBinaryInstall(opts);
                doConfigInstall(opts);
            }
            else {
                determineAgentCommType(opts)
                if (opts.agentCommWebEnabled) {
                    collectWebRelaySettings(opts)
                    collectWebAgentCommSettings(opts)
                }
                if (isZos) {
                    if (isZosToolkitInstalled(opts)) {
                        //upgrade zos toolkit start script (buztool.sh) so auto config upgrade is possible
                        collectZosDatasetInfomation(opts);
                        prepareForToolkitConfigUpgrade(opts);
                    }
                    else {
                        //early version of agent may not have zos toolkit installed.
                        if (!nonInteractive) {
                            // Install it when in interactive mode
                            collectZosDatasetInfomation(opts);
                            collectZosToolkitSettings(opts);
                            dozOSToolkitConfigInstall(opts);
                        }
                        else {
                            //NonInteractive mode can not ask for setting, so we won't install the toolkit.
                            opts.zSkipToolkitInstall = true;
                        }
                    }
                }
                doBinaryInstall(opts);
            }
            completeZosToolkitInstall(opts);
            writeProperties(opts)
            System.exit(0);
        }
    }

    private def doBinaryInstall(AgentInstallOptions opts) {
        copyAgentBinaryFiles(opts);
        if (isZos && (!opts.zSkipToolkitInstall)) {
            copyZosToolkitBinaryFiles(opts);
            tokenReplaceForZosJob(opts);
        }
    }

    private void readInstalledProperties(AgentInstallOptions opts) {
        readInstalledPropertiesForZos(opts);

        File oldPropsFile = new File(opts.installAgentConfigDir, "conf/agent/agent.properties");
        File propsFile = new File(opts.installAgentConfigDir, "conf/agent/installed.properties");
        if (oldPropsFile.exists()) {
            ant.move(tofile: propsFile.absolutePath,
                     file: oldPropsFile.absolutePath);
        }

        if (propsFile.exists()) {
            ant.property(file: propsFile.absolutePath)
            initInstalledProperties(opts);
        }
        else if (opts.installPropertiesFile) {
            // if we were given a properties file on input, copy it to the agent's installed.properties
            // to preserve extra properties

            def props = new Properties()
            new File(opts.installPropertiesFile).withInputStream {
                props.load(it)
            }

            // Filter properties used to configure creation of install packages
            def propsCopy = new Properties()
            props.each {
                if (!it.key.startsWith("package.") && !it.key.contains("agent.service.password")) {
                    propsCopy[it.key] = it.value
                }
            }

            propsFile.parentFile.mkdirs()
            propsFile.withOutputStream {
                propsCopy.store(it, null)
            }
        }
    }

    private def doConfigInstall(AgentInstallOptions opts) {
        copyAgentConfigFiles(opts);
        tokenReplace(opts);
        writeProperties(opts);
        fixPermissions(opts);
        if (opts.doServiceInstall) {
            installService(opts);
        }
        if (isZos) {
            dozOSToolkitConfigInstall(opts);
        }
    }

    private def dozOSToolkitConfigInstall(AgentInstallOptions opts) {
        copyZosToolkitConfigFiles(opts);
        tokenReplaceZosToolkit(opts);
        writeZosToolkitProperties(opts);
        fixZosToolkitPermissions(opts);
    }

    private void tokenReplace(AgentInstallOptions opts) {
        ensureTokenReplacementsAreSet(opts);

        // these need to run after the embedded Java is copied so it can be used.
        getOs(opts, classpath)
        getArch(opts, classpath)

        def javaDebugOpts = '-Xdebug -Xrunjdwp:transport=dt_socket,address=localhost:10001,server=y,suspend=n -Dcom.sun.management.jmxremote';
        if (isWindows) {
            javaDebugOpts = '-Xdebug -Xrunjdwp:transport=dt_socket,address=10001,server=y,suspend=n -Dcom.sun.management.jmxremote'
        }
        ant.replace(dir:opts.installAgentConfigDirTokenReplacement, includes:"**/*", excludes:"opt/**/*,lib/**/*") {
            replacefilter(token: "@AGENT_HOME@", value: opts.installAgentConfigDirTokenReplacement)
            replacefilter(token: "@AGENT_BIN_HOME@", value: opts.installAgentBinaryDirTokenReplacement)
            replacefilter(token: "@JAVA_HOME@", value: opts.javaHomeTokenReplacement)
            replacefilter(token: "@product.prefix@",   value: productPrefix)
            replacefilter(token: "@product.name@",   value: productName)
            replacefilter(token: "@product.capital.name@",   value: productCapitalName)
            replacefilter(token: "@ARCH@", value: opts.installArch)
            replacefilter(token: "@JAVA_OPTS@", value: "-Xmx512m")
            replacefilter(token: "@JAVA_DEBUG_OPTS@", value: javaDebugOpts);
            replacefilter(token: "@AGENT_USER@",    value: opts.installInitUser)
            replacefilter(token: "@AGENT_GROUP@",   value: opts.installInitGroup)
        }
    }

    private void collectInstallProperties(AgentInstallOptions opts) {
        getJavaHome(opts);
        determineAgentCommType(opts)
        if (opts.agentCommWebEnabled) {
            collectWebRelaySettings(opts)
            collectWebAgentCommSettings(opts)
        }
        else {
            collectJMSRelaySettings(opts);
            collectJMSSettings(opts);
        }
        getAgentName(opts);
        getAgentTeams(opts);
        getServiceSettings(opts);
    }

    private ensureTokenReplacementsAreSet(AgentInstallOptions opts) {
        if (opts.installAgentId == null) {
            opts.installAgentId = ""
        }

        // ensure replacements are initialized
        if (opts.installAgentConfigDirTokenReplacement == null) {
            opts.installAgentConfigDirTokenReplacement = opts.installAgentConfigDir
        }
        if (opts.installAgentBinaryDirTokenReplacement == null) {
            opts.installAgentBinaryDirTokenReplacement = opts.installAgentBinaryDir
        }
        if (opts.javaHomeTokenReplacement == null) {
            opts.javaHomeTokenReplacement = opts.javaHome
        }
    }

    private void writeProperties(AgentInstallOptions opts) {
        ensureTokenReplacementsAreSet(opts);
        def brokerURL = null

        // construct the broker URL if not already defined
        if (opts.installBrokerURL == null) {
            // if installer is interactive and installWithRelay is true, no default is provided and
            // no validation is done. so if user just presses enter through the prompt, null gets
            // added to the list
            opts.installAgentRemoteHostList.remove(null)

            brokerURL = "failover:("
            for (int i = 0; i < opts.installAgentRemoteHostList.size(); i++) {
                brokerURL += "ah3://" + opts.installAgentRemoteHostList.get(i) + ":" + opts.installAgentRemotePortList.get(i)
                if (i+1 != opts.installAgentRemoteHostList.size()) {
                    brokerURL += ","
                }
            }
            brokerURL += ")"
        }
        else {
            brokerURL = opts.installBrokerURL
        }

        def agentCommProxyUris = ""
        if (opts.agentCommProxyUriList.size() > 0) {
            agentCommProxyUris = opts.agentCommProxyUriList.collect{it.trim()}.findAll{!it.empty}.unique().join(",")
            agentCommProxyUris = randomizeList(agentCommProxyUris)
        }

        def agentCommWebServerUris = ""
        if (opts.agentCommWebServerUriList.size() > 0) {
            agentCommWebServerUris = opts.agentCommWebServerUriList.collect{it.trim()}.findAll{!it.empty}.unique().join(",")
            agentCommWebServerUris = randomizeList(agentCommWebServerUris)
        }

        if (!opts.installAgentKeystore) {
            opts.installAgentKeystore = "../conf/agent.keystore"
        }
        if (!opts.installAgentKeystorePwd) {
            opts.installAgentKeystorePwd = "changeit"
        }

        //check whether or not the file path is absolute
        File keyStoreFile = new File(opts.installAgentKeystore);
        if (!keyStoreFile.isAbsolute()) {
            //if the file path is relative it must be relative from the agent's bin directory
            keyStoreFile = new File(opts.installAgentConfigDir+"/bin", opts.installAgentKeystore);
        }

        if (!opts.encryptionKeyStorePath) {
            opts.encryptionKeyStorePath = "../conf/encryption.keystore";
        }
        if (!opts.keyStorePassword) {
            //set to default
            opts.keyStorePassword = "changeit"
        }

        if (!opts.encryptionKeystoreAlias) {
            def uniquePart = RandomStringUtils.randomAlphanumeric(4)
            def prefix = "aes128key"
            opts.encryptionKeystoreAlias = "${prefix}${uniquePart}".toLowerCase()
        }

        ant.propertyfile(file: opts.installAgentConfigDir + "/conf/agent/installed.properties") {
            entry(key: "locked/agent.brokerUrl", default: brokerURL)
            if (opts.installAgentProxyHost) {
                entry(key: "locked/agent.http.proxy.host", default: opts.installAgentProxyHost)
                entry(key: "locked/agent.http.proxy.port", default: opts.installAgentProxyPort)
            }
            entry(key: productName + "/java.home", default: opts.javaHomeTokenReplacement)
            entry(key: "locked/agent.home", default: opts.installAgentConfigDirTokenReplacement)
            entry(key: "locked/agent.mutual_auth", default: opts.installAgentMutualAuth)
            entry(key: "agent.jms.full_encryption", operation:"del");
            entry(key: "locked/agent.jms.remote.host", operation:"del");
            entry(key: "locked/agent.jms.remote.port", operation:"del");
            if (opts.disableFullEncryption != null && opts.disableFullEncryption) {
                entry(key: "agent.jms.disable_full_encryption", default: opts.disableFullEncryption)
            }
            entry(key: "locked/agent.keystore", default: opts.installAgentKeystore)
            entry(key: "locked/agent.keystore.pwd", default: CryptStringUtil.encrypt(opts.installAgentKeystorePwd))
            entry(key: "system.default.encoding", default: systemEncoding)
            entry(key: "agent.HttpFailoverHandler.disabled", default: opts.installDisableHttpFailover)
            entry(key: "verify.server.identity", default: opts.installAgentVerifyServerIdentity)
            entry(key: "encryption.keystore.password", default: CryptStringUtil.encrypt(opts.keyStorePassword))
            entry(key: "encryption.keystore", default: opts.encryptionKeyStorePath)
            entry(key: "encryption.keystore.alias", default: opts.encryptionKeystoreAlias)
            if (opts.installAgentServerUrl != null && !opts.installAgentServerUrl.isAllWhitespace()) {
                entry(key: "server.url", default: opts.installAgentServerUrl)
            }
            if (opts.installAgentId != null && !opts.installAgentId.isAllWhitespace()) {
                entry(key: "locked/agent.id", default: opts.installAgentId)
            }
            if (opts.installAgentName != null && !opts.installAgentName.isAllWhitespace()) {
                entry(key: "locked/agent.name", default: opts.installAgentName)
            }
            if (opts.installAgentTeams != null && !opts.installAgentTeams.isAllWhitespace()) {
                entry(key: "locked/agent.initial.teams", default: opts.installAgentTeams)
            }
            if (isZos) {
                entry(key: "com.urbancode.shell.impersonation.unix.suFormat", default: "%s -s %u -c %c")
            }
            entry(key: "agentcomm.enabled", value: opts.agentCommWebEnabled)
            if (opts.agentCommWebEnabled) {
                entry(key: "agentcomm.server.uri", value: agentCommWebServerUris)
            }
            if (agentCommProxyUris) {
                entry(key: "agentcomm.proxy.uri", value: agentCommProxyUris)
            }
        }
        removeExperimentalProperties(opts)
    }

    private void removeExperimentalProperties(AgentInstallOptions opts) {
        File tempFile = new File(opts.installAgentConfigDir + "/conf/agent/temp.properties")
        File installPropsFile = new File(opts.installAgentConfigDir + "/conf/agent/installed.properties")

        SortedProperties props = new SortedProperties();
        FileInputStream inStream = new FileInputStream(installPropsFile);
        try {
            props.load(inStream);
        }
        finally {
            inStream.close();
        }

        props.remove("experimental.comm2017.enabled")
        props.remove("experimental.comm2017.agent.server.uri")
        props.remove("experimental.comm2017.agent.proxy.address")

        FileOutputStream outStream = new FileOutputStream(tempFile);
        try {
            props.store(outStream);
        }
        finally {
            outStream.close();
        }
        installPropsFile.delete();
        tempFile.renameTo(installPropsFile);
    }

    private void copyAgentConfigFiles(AgentInstallOptions opts) {
        def agentInstallDir = opts.installAgentConfigDir;
        updateJRE(agentInstallDir);

        ant.unzip(src:"config.zip", overwrite:"false", dest:agentInstallDir) {
            patternset {
                 include(name:"conf/**/*");
                 include(name:"bin/**/*");
                 include(name:"properties/**/*");//for the record, i haven't the slightest idea what this is for
                 include(name:"var/**/*");
                 if (isUnix || isZos) {
                     exclude(name:"bin/service/**/*");
                     exclude(name:"bin/service");
                     exclude(name:"bin/**/*.cmd");
                     exclude(name:"bin/classpath.conf.WIN");
                     exclude(name:"bin/worker-args.conf.WIN");
                 }
                 else {
                     exclude(name:"bin/agent");
                     exclude(name:"bin/configure-agent");
                     exclude(name:"bin/init");
                     exclude(name:"bin/init/**/*");
                     exclude(name:"bin/classpath.conf.UNIX");
                     exclude(name:"bin/worker-args.conf.UNIX");
                     include(name:"Impersonater.exe");
                 }
            }
            chainedmapper() {
                def fromString = isWindows ? "*.WIN" : "*.UNIX";
                globmapper(from:fromString, to:"*");
                identitymapper();
            }
        }

    }

    private void fixPermissions(AgentInstallOptions opts) {
        // Link old executables to the new one if upgrading.
        makeSymlinksToNewExec(opts.installAgentConfigDir)

        if (isUnix && (!isZos)) {
            ant.fixcrlf(srcDir: opts.installAgentConfigDir, eol: "lf", encoding: "UTF-8", outputEncoding: "UTF-8") {
                include(name: "bin/classpath.conf")
                include(name: 'bin/agent')
                include(name: 'bin/init/agent')
                include(name: 'bin/configure-agent')
                include(name: 'opt/apache-ant-*/bin/ant')
                include(name: 'opt/groovy-*/bin/groovy')
                include(name: 'opt/groovy-*/bin/groovyc')
            }
        }
        else if (isWindows) {
            ant.fixcrlf(srcDir: opts.installAgentConfigDir, encoding: "UTF-8", outputEncoding: "UTF-8") {
                include(name: "bin\\agent.cmd")
                include(name: "bin\\classpath.conf")
                include(name: "bin\\worker-args.conf")
                include(name: "bin\\configure-agent.cmd");
                include(name: "bin\\run_agent.cmd")
                include(name: "bin\\start_agent.cmd")
                include(name: "bin\\stop_agent.cmd")
                include(name: "bin\\service\\agent.cmd")
                include(name: "bin\\service\\_agent.cmd")
                include(name: "bin\\service\\agent_srvc_configurator.vbs")
                include(name: "bin\\service\\agent_srvc_stop.vbs")
                include(name: "bin\\service\\agent_srvc_install.vbs")
            }
        }

        if (isUnix) {
            println "chmodding"
            println opts.installAgentConfigDir
            ant.chmod(perm: "+x", type: "file") {
                fileset(dir: opts.installAgentConfigDir) {
                    include(name: "bin/agent")
                    include(name: "bin/init/agent")
                    include(name: "bin/configure-agent")
                }
            }
        }

        if (isZos) {
            //Convert the file from native encoding to UTF-8
            ant.fixcrlf(srcDir: opts.installAgentConfigDir, eol: "lf", encoding: systemEncoding, outputEncoding: "UTF-8") {
                include(name: 'bin/worker-args.conf')
            }
        }
    }

    private void copyAgentBinaryFiles(AgentInstallOptions opts) {
        updateJRE(opts.installAgentBinaryDir);

        // these need to run after the embedded Java is copied so it can be used.
        getOs(opts, classpath)
        getArch(opts, classpath)

        //we need to not fail on error because agents >= 6.2.1.0 will open commonsutil in wrong exe causing it to not delete
        //FIX: WI 163861
        ant.delete(failonerror:"false") {
            fileset(dir:opts.installAgentBinaryDir, includes:"lib/**/*");
        }

        ant.unzip(src:"binary.zip", overwrite:"true", dest:opts.installAgentBinaryDir) {
            patternset {
                 include(name:"conf/**/*");
                 include(name:"lib/**/*");
                 include(name:"monitor/**/*");
                 include(name:"native/${opts.installOs}/${opts.installArch}/**/*");
                 include(name:"opt/**/*");
                 if (isUnix) {
                     exclude(name:"opt/udclient/udclient.cmd")
                 }
                 else {
                     include(name:"Impersonater.exe");
                     exclude(name:"opt/udclient/udclient")
                 }
                 if(isZos){
                     include(name:"native/zos/*");
                 }
                 if (!isZos) {
                     exclude(name:"lib/zos/*")
                 }
            }
            mapper {
                globmapper(from:"native/${opts.installOs}/*", to:"native/*");
                globmapper(from:"native/zos/*", to:"native/*");
            }
        }

        if (isUnix) {
            removeWorkingDirFromClasspathUnix(opts.installAgentBinaryDir)
        }
        else if (isWindows) {
            removeWorkingDirFromClasspathWindows(opts.installAgentBinaryDir)
        }

        if (isZos) {
            //convert to native encoding
            ant.fixcrlf(srcDir: opts.installAgentBinaryDir, eol: "lf", encoding: "UTF-8", outputEncoding: systemEncoding) {
                include(name: 'opt/apache-ant-*/bin/ant')
                include(name: 'opt/apache-ant-*/bin/antRun')
                include(name: 'opt/groovy-*/bin/groovy')
                include(name: 'opt/groovy-*/bin/groovyc')
                include(name: 'opt/groovy-*/bin/startGroovy')
                include(name: 'opt/udclient/udclient')
            }
            ant.chmod(file: opts.installAgentBinaryDir + "/native/CheckRDT.exe", perm:"+x")
        }

        //transcode removes execute permission
        ant.chmod(file: opts.installAgentBinaryDir + "/opt/udclient/udclient", perm:"+x")

        // chmod opt bin files
        ant.chmod(perm: "+x", type: "file") {
            fileset(dir: opts.installAgentBinaryDir) {
                include(name: "opt/*/bin/*")
                exclude(name: "opt/*/bin/*.bat")
                exclude(name: "opt/*/bin/*.cmd")
            }
        }
    }

    private void updateJRE(def installAgentDir) {
        //what happens if i try to delete the jre when an agent is running it?
        // i think on windows it will just fail
        // on unix???
        final def jreDir = new File(srcDir + '/java')
        if (jreDir.exists()) {
            ant.delete(dir: installAgentDir + "/java", quiet: 'true', failonerror: 'false')
            ant.copy(todir: installAgentDir + "/java") {
                fileset(dir: srcDir + "/java") {
                    include(name: "**/*")
                }
            }
            ant.chmod(perm: "+x", type: "file", dir: installAgentDir + "/java/bin", includes: "**")
        }
    }


    private void getJavaHome(AgentInstallOptions opts) {
        // make sure we have the JAVA_HOME
        final def jreDir = new File(srcDir + File.separator + 'java')
        if (jreDir.exists()) {
            opts.javaHome = opts.installAgentBinaryDir + File.separator + 'java'
        }
        else {
            opts.javaHome = prompt(
                    currentJavaHome,
                    "Please enter the home directory of the JRE/JDK used to run this agent. " +
                            "[Default: " + opts.javaHome + "]",
                    opts.javaHome,
                    jreHomeValidator)
        }

        opts.javaHome = new File(opts.javaHome).absolutePath
        println("JAVA_HOME: " + opts.javaHome + "\n")
    }

    private boolean shouldInstallWithRelay(def propertyToCheck, def opts) {
        def installWithRelay = null
        if (propertyToCheck) {
            installWithRelay = "Y"
        }
        else {
            if (!nonInteractive) {
                installWithRelay = null
                if (doUpgrade && opts.agentCommWebServerUriList.size() > 0) {
                    installWithRelay = "N"
                }
            }
            else {
                installWithRelay = "N"
            }
        }

        installWithRelay = parseBoolean(prompt(
            installWithRelay,
            "Will the agent connect to an agent relay instead of directly to the server? y,N [Default: N]",
            "N",
            yesNoValidator))
        return installWithRelay
    }

    private void collectWebRelaySettings(AgentInstallOptions opts) {
        def installWithRelay = shouldInstallWithRelay(opts.agentCommProxyUriList.size() > 0, opts)
        def inputAnotherHost = true

        if (opts.agentCommProxyUriList.size() == 0 && !nonInteractive) {
            while(inputAnotherHost && installWithRelay) {
                opts.agentCommProxyUriList.add(prompt(
                        null,
                        "Enter the address of a relay for the agent to use as a proxy. [Default: http://localhost:20080]",
                        "http://localhost:20080",
                        webAgentProxyValidator))

                inputAnotherHost = parseBoolean(prompt(
                        null,
                        "Do you want to configure another relay to use as a proxy? y,N [Default: N]",
                        "N",
                        yesNoValidator))
            }
        }
    }

    private void collectJMSRelaySettings(AgentInstallOptions opts) {
        def installWithRelay = shouldInstallWithRelay(opts.installAgentProxyPort, opts)
        def inputAnotherHost = true

        if (nonInteractive) {
            inputAnotherHost = false
            // add default values if needed
            if (opts.installAgentRemoteHostList.size() == 0) {
                opts.installAgentRemoteHostList.add("localhost");
            }
            if (opts.installAgentRemotePortList.size() == 0) {
                opts.installAgentRemotePortList.add("7918");
            }
        }
        while (inputAnotherHost) {
            if (installWithRelay) {
                opts.installAgentRemoteHostList.add(prompt(
                        opts.installAgentRemoteHost,
                        "Enter the hostname or address of the agent relay the agent will connect to."))
                opts.installAgentRemotePortList.add(prompt(
                        opts.installAgentRemotePort,
                        "Enter the agent communication port for the agent relay. [Default: 7916]",
                        "7916",
                        portValidator))
                inputAnotherHost = parseBoolean(prompt(
                        null,
                        "Do you want to configure another failover relay connection? y,N [Default: N]",
                        "N",
                        yesNoValidator))
            }
            else {
                opts.installAgentRemoteHostList.add(prompt(
                        opts.installAgentRemoteHost,
                        "Enter the hostname or address of the server the agent will connect to. [Default: localhost]",
                        "localhost"))
                opts.installAgentRemotePortList.add(prompt(
                        opts.installAgentRemotePort,
                        "Enter the agent communication port for the server. [Default: 7918]",
                        "7918",
                        portValidator))
                inputAnotherHost = parseBoolean(prompt(
                        null,
                        "Do you want to configure another failover server connection? y,N [Default: N]",
                        "N",
                        yesNoValidator))
            }
        }

        if (opts.installAgentRemoteHostList.isEmpty()) {
            opts.installAgentRemoteHostList.add(opts.installAgentRemoteHost)
        }
        if (opts.installAgentRemotePortList.isEmpty()) {
            opts.installAgentRemotePortList.add(opts.installAgentRemotePort)
        }

        if (installWithRelay) {
            opts.installAgentProxyHost = prompt(
                    opts.installAgentProxyHost,
                    "Enter the hostname or address of the HTTP proxy server for the agent relay. " +
                    "[Default: " + opts.installAgentRemoteHostList.get(0) + "]",
                    opts.installAgentRemoteHostList.get(0))
            opts.installAgentProxyPort = prompt(
                    opts.installAgentProxyPort,
                    "Enter the HTTP proxy port for the agent relay. [Default: 20080]",
                    "20080",
                    portValidator)
            opts.installDisableHttpFailover = parseBoolean(prompt(
                    opts.installDisableHttpFailover,
                    "Do you want to disable HTTP Failover Handling? This is necessary if the relay is behind a firewall and accessed through a load balancer. N,y [Default: N]",
                    "N",
                    yesNoValidator))
        }
    }

    private void determineAgentCommType(AgentInstallOptions opts) {
        def type = null
        if (opts.agentCommWebEnabled) {
            type = "WEB"
        }
        if (type == null && opts.agentCommWebEnabled == false) {
            type = "JMS"
        }

        def defaultValue = null;
        if (!nonInteractive) {
            defaultValue = "WEB"
        }


        String promptText =
                "Will this agent use web or JMS communication? Web communication is a " +
                "new and more scalable replacement for JMS. Contact your UCD administrator to " +
                "determine if your server is ready to use Web communication. " +
                "Otherwise, enter 'jms' for legacy JMS communication. [Default: web]"
        type = prompt(type, promptText, defaultValue, webJmsValidator)

        opts.agentCommWebEnabled = type.toUpperCase().equals("WEB")
    }

    private void collectWebAgentCommSettings(AgentInstallOptions opts) {
        def inputAnotherHost = true
        if (opts.agentCommWebServerUriList.size == 0) {
            while(inputAnotherHost) {
                opts.agentCommWebServerUriList.add(prompt(
                    null,
                    "Enter the web agent communication URL for your UCD server. It must begin with 'wss://'. [Default: wss://localhost:7919]",
                    "wss://localhost:7919",
                    webUriValidator))

                inputAnotherHost = parseBoolean(prompt(
                    null,
                    "Do you want to configure another failover server connection? y,N [Default: N]",
                    "N",
                    yesNoValidator))
            }
        }
    }

    private void collectJMSSettings(AgentInstallOptions opts) {
        opts.installAgentMutualAuth = parseBoolean(prompt(
            opts.installAgentMutualAuth,
            "Enable mutual (two-way) authentication with SSL for server/agent JMS communication? This setting must match that of the server. y,N [Default: N]",
            "n"))

        // Prompt for whether to enable end-to-end encryption (for JMS),
        // the default is "N" if upgrade, "Y" if new installation.
        String promptAgentFullEncryption =
            "End-to-end encryption enhances the security of UrbanCode messages sent between\n" +
            "the agent and the server, and requires an initial HTTPS connection to set up\n" +
            "keys. (WARNING: If your server has been configured to require end-to-end\n" +
            "encryption exclusively, you must not disable this agent feature and must supply\n" +
            "the full web URL below or your agent will not come online.)\n" +
            "\n" +
            "Disable end-to-end encryption for server/agent JMS communication?"
        String defaultAgentFullEncryption = "n"

        promptAgentFullEncryption = promptAgentFullEncryption + " y,N [Default: N]"
        opts.disableFullEncryption = parseBoolean(prompt(
            opts.disableFullEncryption,
            promptAgentFullEncryption,  // user prompt
            defaultAgentFullEncryption))

        opts.installAgentVerifyServerIdentity = parseBoolean(prompt(
            opts.installAgentVerifyServerIdentity,
            "Enable the agent to verify the server HTTPS certificate?" +
                " If enabled, you must import the server certificate" +
                " to the JRE keystore on the agent. y,N [Default: N]",
            "n"))

        // If full encryption is enabled, process the required value for 'server.url'.
        if (opts.disableFullEncryption == null || !opts.disableFullEncryption) {

            String promptAgentServerUrl =
                "Enter the full web URL for the central UrbanCode Deploy server to validate\n" +
                "the connection. (WARNING: If your server has been configured to require\n" +
                "end-to-end encryption exclusively, you must supply the URL here or your agent\n" +
                "will not come online.)\n" +
                "Leave empty to skip."

            if (doUpgrade) {
                // "Prompt" for server.url value if current value is null. No validation.
                opts.installAgentServerUrl = prompt(opts.installAgentServerUrl, promptAgentServerUrl)
            }
            // Else if silent-mode and value for server.url is empty, print warning--prompt() will
            // do that for empty values--and continue with silent installation
            else if (nonInteractive && !StringUtils.isNotBlank(opts.installAgentServerUrl)) {
                opts.installAgentServerUrl = prompt(opts.installAgentServerUrl, promptAgentServerUrl)
            }
            // Else, new installation, and either interactive mode or silent w/server.url value not empty
            else {
                boolean serverUrlIsValid = false

                // Loop while we prompt the user for the central server and Test the connection.
                while (!serverUrlIsValid) {
                    opts.installAgentServerUrl = prompt(opts.installAgentServerUrl, promptAgentServerUrl, null)

                    // Loop until they do not enter the url or we can validate it
                    if (opts.installAgentServerUrl == null || opts.installAgentServerUrl.isAllWhitespace()) {
                        serverUrlIsValid = true;
                    }
                    else {
                        try {
                            // Test server with http request
                            // Display message to wait while we send a test request to the central server.
                            println("Sending a request to the central UrbanCode Deploy server...")
                            probeGenerateKeyAPI(opts.installAgentServerUrl, opts)
                            serverUrlIsValid = true
                            println("Server URL is valid.")
                        }
                        catch (Exception e) {
                            println("Error: Server access attempt failed for URL [${opts.installAgentServerUrl}]: ${e.message}.\n")
                        }
    
                        if (!serverUrlIsValid) {
                            if (nonInteractive) {   // For Non-Interactive mode, fail-out
                                throw new IllegalArgumentException(
                                    "Non-Interactive Mode: problem with full web URL for central UrbanCode Deploy server: '${opts.installAgentServerUrl}' .")
                            }
                            else {  // interactive mode: server url access failed, clear value and ask user again
                                opts.installAgentServerUrl = null
                            }
                        }
                    }
                }
            }
        }
    }

    private void getAgentName(AgentInstallOptions opts) {
        try {
            opts.hostName = InetAddress.localHost.canonicalHostName
        }
        catch (UnknownHostException e) {
            opts.hostName = "localhost"
        }

        if (!opts.generateAgentName) {
            opts.installAgentName = prompt(
                opts.installAgentName,
                "Enter the name for this agent. [Default: "+opts.hostName+"]",
                opts.hostName)
        }
    }

    private getAgentTeams(AgentInstallOptions opts) {
        if (!doUpgrade) {
            ant.echo(message: "The agent can be added to one or more teams when it first connects to the server. " +
                "Changing this setting after initial connection to the server will not have any effect. " +
                "You can also add a specific type associated with a team by using the format <team>:<type>")
            opts.installAgentTeams = prompt(
                opts.installAgentTeams,
                "Enter teams (and types) to add this agent to, separated by commas. [Default: None]",
                "")
        }
    }

    private void getServiceSettings(AgentInstallOptions opts) {
        //install service
        if (isWindows) {
            opts.doServiceInstall = false
            if (opts.installServiceName != null) {
                opts.doServiceInstall = true
            }
            else {
                opts.installAsService = parseBoolean(prompt(
                        opts.installAsService,
                        "Do you want to install the Agent as Windows service? y,N [Default: N]",
                        "N",
                        yesNoValidator))
                opts.doServiceInstall = opts.installAsService
            }

            def defaultName = productName.toLowerCase().replace(" ", "-")+"-agent"
            def strLocalsystem = /.\localsystem/
            def strPath = /'.\'/
            if (opts.doServiceInstall == true) {
                if (opts.installServiceName == null) {
                    opts.installServiceName = prompt(
                        opts.installServiceName,
                        "Enter a unique service name for the Agent. No spaces please. [Default: " + defaultName+"]",
                        defaultName,
                        serviceNameValidator)
                }

                if (opts.installServiceLogin == null) {
                    opts.installServiceLogin = prompt(
                        opts.installServiceLogin,
                        "Enter the user account name including domain path to run the service as (for local use " +
                        strPath + " before login), The local system account will be used by default. [Default: " +
                        strLocalsystem + "]",
                        strLocalsystem,
                        requiredValidator)
                }

                if (opts.installServiceLogin != strLocalsystem) {
                    opts.installServicePassword = prompt(
                            opts.installServicePassword,
                            "Please enter your password for desired account.",
                            "nopass",
                            requiredValidator)
                }
                else {
                    opts.installServicePassword = "nopass"
                }

                opts.installServiceAutostart = prompt(
                        opts.installServiceAutostart,
                        "Do you want to start the '" + opts.installServiceName + "' service automatically? y,N " +
                        "[Default: N]",
                        "N",
                        yesNoValidator)
            }
        }
        else if (isWindows) {
            println("\nYou can install service manually (see documentation).\n\n")
        }
    }

    private void installService(AgentInstallOptions opts) {
        ant.exec(dir: opts.installAgentConfigDir + "/bin/service", executable:"cscript.exe") {
            arg(value:"//I")
            arg(value:"//Nologo")
            arg(value:opts.installAgentConfigDir + "/bin/service/agent_srvc_install.vbs")
            arg(value:opts.installServiceName)
            arg(value:opts.installServiceLogin)
            arg(value:opts.installServicePassword)
            arg(value:opts.installServiceAutostart)
        }

        // read service installation status properties
        if (new File(opts.installAgentConfigDir + "/bin/service/srvc.properties").exists()) {
            ant.property(file: opts.installAgentConfigDir + "/bin/service/srvc.properties")
            def installServiceStatus = ant.project.properties.'install.service.status'
            if (installServiceStatus == "OK") {
                ant.propertyfile(file: opts.installAgentConfigDir + "/conf/agent/installed.properties") {
                    entry(key: "install.service.name", value: opts.installServiceName)
                }
            }
            ant.delete(file: opts.installAgentConfigDir + "/bin/service/srvc.properties")
        }
    }


    //
    // private method for linking old executable files to the new when upgrading.
    //

    private void makeSymlinksToNewExec(installAgentDir) {
        // List of known deprecated executable file names.
        def oldExecNames = ["udagent", "ibm-ucdagent"]

        // The executable name currently in use.
        def newExecName = "agent"

        // Necessary for creating symlinks.
        Unix unix = new Unix();

        for(String n : oldExecNames) {
            // File objects for an old executable and its corresponding .bak file.
            File oldExec = new File(installAgentDir + "/bin/" + n)
            File oldExecBak = new File(installAgentDir + "/bin/" + n + ".bak")

            // Same as above, but for the executable in the init directory.
            File oldExecInit = new File(installAgentDir + "/bin/init/" + n)
            File oldExecInitBak = new File(installAgentDir + "/bin/init/" + n + ".bak")

            if (oldExec.exists() && !oldExec.isDirectory()) {
                if (!oldExecBak.exists()) {
                    IO.copy(oldExec, oldExecBak)
                }
                IO.delete(oldExec)
                unix.mksymlink(oldExec, installAgentDir + "/bin/" + newExecName)
            }

            if (oldExecInit.exists() && !oldExecInit.isDirectory()) {
                if (!oldExecInitBak.exists()) {
                    IO.copy(oldExecInit, oldExecInitBak)
                }
                IO.delete(oldExecInit)
                unix.mksymlink(oldExecInit, installAgentDir + "/bin/init/" + newExecName)
            }
        }
    }

    private void getBinaryDirectoryForConfigInstall(AgentInstallOptions opts) {
        getInstallDir("agent_binary", opts, "installAgentBinaryDir", false);
    }

    private void getInstallDir(installComponent, AgentInstallOptions opts, String propNameToSet, boolean shouldCreateDir) {
        String defaultDir =  null
        if (Os.isFamily('mac')) {
            defaultDir = '/Library/' + productPrefix + '/'+installComponent
        }
        else if (Os.isFamily('unix')) {
            defaultDir = '/opt/' + productPrefix + '/' + installComponent
            if (Os.isFamily("z/os") && "agent".equals(installComponent)) {
                 println "The agent directory length must not exceed 38 characters in z/OS."
            }
        }
        else if (Os.isFamily('windows')) {
            String progFiles = ant.project.properties.'ProgramFiles'
            if (progFiles != null && progFiles.length() > 0 ) {
                defaultDir = progFiles+'\\' + productPrefix + '\\'+installComponent
            }
            else {
                defaultDir = "C:\\Program Files" + File.separator + installComponent.replace('/', '\\')
            }
        }

        opts."${propNameToSet}" = prompt(
                opts."${propNameToSet}",
                'Enter the directory where ' + installComponent + ' should be installed.' +
                (defaultDir == null ? '' : ' [Default: '+defaultDir+']'),
                defaultDir,
                requiredValidator)

        if (shouldCreateDir && !new File(opts."${propNameToSet}").exists()) {
            String createDir = prompt(
                    null,
                    'The specified directory does not exist. Do you want to create it? Y,n [Default: Y]',
                    'Y',
                    yesNoValidator)
            if ('Y'.equalsIgnoreCase(createDir) || 'YES'.equalsIgnoreCase(createDir)) {
                new File(opts."${propNameToSet}").mkdirs()
            }
            else {
                ant.fail('Can not install without creating installation directory.')
            }
        }

        opts."${propNameToSet}" = new File(opts."${propNameToSet}").absolutePath
    }

    private boolean checkForUpgrade(installDir, installServiceName) {
        if (new File(installDir + '/conf/installed.version').exists()) {
            ant.property(file: installDir + '/conf/installed.version')

            if (new File(installDir + '/var/' + productName + '-agent.pid').exists()) {
                ant.fail('A previously installed version of ${component.name} is running. ' +
                     'Please shutdown the running ${component.name} and start the installation again.')
            }

            if (nonInteractive) {
                return true
            }

            doUpgrade = prompt(
                    null,
                    'A previous version (' + ant.project.properties.'installed.version' + ') ' +
                    'exists in the specified directory. Do you want to upgrade the currently ' +
                    'installed version? [Y,n]',
                    'Y',
                    yesNoValidator)
            if (doUpgrade == 'Y' || doUpgrade == 'y') {
                doUpgrade = true;

                //stopping service if running
                if (installServiceName != null && isWindows) {
                       println('\nYour Agent is installed as "' + installServiceName + '" service.\n\n')
                    ant.exec(dir: './bin/agent/service', executable: 'cscript.exe') {
                        arg(value:'//I')
                        arg(value:'//Nologo')
                        arg(value:'agent_srvc_stop.vbs')
                        arg(value:installServiceName)
                    }
                }

                return true
            }
            ant.fail('Not upgrading the existing installation.')
        }

        return false
    }

    private String prompt(promptText) {
        return prompt(null, promptText, null)
    }

    private String prompt(curValue, promptText) {
        return prompt(curValue, promptText, null)
    }

    private String prompt(curValue, promptText, defaultValue) {
        return prompt(curValue, promptText, defaultValue, null)
    }

    private String prompt(curValue, promptText, defaultValue, validator) {
        // use curValue if not null and not empty
        if (curValue != null && curValue.trim()) {
            return curValue
        }

        if (nonInteractive) {
            println(promptText)

            def warningMessage = 'Warning: Installer prompting for input in non-interactive mode.'
            if (defaultValue) {
                warningMessage += '  Returning default: ' + defaultValue
            }
            println(warningMessage)

            if (validator != null) {
                try {
                    validator.validate(defaultValue)
                }
                catch (ValidationException ve) {
                    throw new IllegalArgumentException(
                            "Non-Interactive Mode: problem with default value of '${defaultValue}' " +
                            "for '${promptText}' - " + ve.getValidationMessageArray().join(' '))
                }
            }
            return defaultValue
        }

        def userValue = null
        def valid = false
        while (!valid) {
            println(promptText)
            userValue = read(defaultValue)

            if (validator != null) {
                try {
                    validator.validate(userValue)
                    valid = true
                }
                catch (ValidationException ve) {
                    for (message in ve.getValidationMessageArray()) {
                        println(message)
                    }
                }
            }
            else {
                valid = true
            }
        }

        return userValue
    }

    private String read(defaultValue) {
        def line = systemIn.readLine()?.trim()
        return line ?: defaultValue
    }

    private void println(displayText) {
        if (displayText != null) {
            ant.echo(displayText)
        }
    }

    private String getOs(opts, classpath) {
        def javaHome = opts.javaHome;
        if (opts.installOs == null) {
            def process = ["${javaHome}" + File.separator + "bin" + File.separator + "java",
                           "-Xmx64m", "-Xms64m", "-classpath", classpath,
                           "com.urbancode.commons.detection.GetOs"].execute()
            OutputStream outText = new ByteArrayOutputStream();
            process.waitForProcessOutput(outText, System.err);
            opts.installOs = outText.toString().trim();
            outText.close();
            if (opts.installOs == "") {
                throw new Exception("No OS was retrieved.")
            }
        }
        return opts.installOs;
    }

    private String getArch(opts, classpath) {
        def javaHome = opts.javaHome;
        if (opts.installArch == null) {
            try {
                def process = ["${javaHome}" + File.separator + "bin" + File.separator + "java",
                               "-Xmx64m", "-Xms64m", "-classpath", classpath,
                               "com.urbancode.commons.detection.GetArch"].execute()
                OutputStream outText = new ByteArrayOutputStream();
                process.waitForProcessOutput(outText, System.err);
                opts.installArch = outText.toString().trim();
                outText.close();
            }
            catch (Exception e) {
                println "Error retrieving system architecture. Installation may not complete correctly. Error: ${e.message}"
            }
        }
        return opts.installArch
    }

    // Get the system encoding. Use console.encoding (ibm jdk) if set, otherwise use file.encoding
    private String getSystemEncoding() {
        def systemEncoding = System.properties.'console.encoding'
        if (systemEncoding == null) {
            systemEncoding = System.properties.'file.encoding'
        }
        return systemEncoding
    }

    private boolean parseBoolean(String s) {
        return Boolean.valueOf(s) ||
            "Y".equalsIgnoreCase(s) ||
            "YES".equalsIgnoreCase(s)
    }


    /*
     * Groovy 1.8.8 adds the current working directory to the CLASSPATH even
     * when the CLASSPATH is explicitly defined. This is incorrect behavior and
     * will cause issues since users may download artifacts and or set a current
     * working directory which contains classes which we don't want to have
     * loaded.
     *
     * We modify the startGroovy scripts on to fix this issue. If the version of
     * groovy is changed it will likely break our fix.I have put a check in
     * place to alert future DEVs to this if or when the groovy version is
     * updated.
     */
    private void warnAboutGroovyVersion(String contents) {
        // I split the string here to prevent a a find/replace from changing this
        if (contents.indexOf("groovy-1"+".8"+".8.jar") == -1) {
            println("Warning: using a version of groovy other than 1"+".8"+".8 may introduce a regression where" +
                "the current working directory is added onto Groovy's classpath.")
        }
    }

    private void removeWorkingDirFromClasspathUnix(String agentDir) {
        File startGroovyFile = new File(agentDir + "/opt/groovy-1.8.8/bin/startGroovy");
        String contents = startGroovyFile.getText();
        warnAboutGroovyVersion(contents)
        contents = contents.replace('CP="$CLASSPATH":.', 'CP="$CLASSPATH"');
        contents = contents.replace('CP="$CP":.', 'CP="$CP"')
        contents = contents.replace('CP="$CP:.', 'CP=')
        startGroovyFile.write(contents)
    }

    private void removeWorkingDirFromClasspathWindows(String agentDir) {
        File startGroovyFile = new File(agentDir + "/opt/groovy-1.8.8/bin/startGroovy.bat");
        String contents = startGroovyFile.getText();
        warnAboutGroovyVersion(contents)
        contents = contents.replace('set CP=%CP%;.":.', 'set CP=%CP%')
        contents = contents.replace('set CP=.', 'set CP=');
        startGroovyFile.write(contents)
    }

    private probeGenerateKeyAPI(requestUrl, opts) {
        while (requestUrl.endsWith("/")) {
            requestUrl = requestUrl.substring(0, requestUrl.length() - 1);
        }

        def cookie = UUID.randomUUID().toString();

        def requestJson = JsonOutput.toJson([probe: true, cookie: cookie]);

        def request = new HttpPost(requestUrl + "/rest/agent/generateKey")
        def entity = new StringEntity(requestJson.toString())
        entity.setContentType("application/json")
        request.setEntity(entity)

        def response = createHttpClient(opts).execute(request)
        def statusCode = response.statusLine.statusCode
        def statusReason = response.statusLine.reasonPhrase
        if (statusCode != 200) {
            throw new Exception("Probe failed: HTTP call failed: reason: \"${statusReason}\"; status code: ${statusCode}")
        }
        def responseContent = EntityUtils.toString(response.getEntity())

        def responseJson = new JsonSlurper().parseText(responseContent)
        if (!responseJson.probe) {
            throw new Exception("Probe failed: invalid probe response")
        }
        if (cookie != responseJson.cookie) {
            throw new Exception("Probe failed: probe cookie corrupt")
        }
    }

    /**
     * Create a instance of HttpClient that shares connections and adheres to the JVM proxy settings.
     */
    private HttpClient createHttpClient(AgentInstallOptions opts) {

        HttpClientBuilder builder = new HttpClientBuilder()

        // so we do not get "null"
        def tmpInstallAgentProxyHost = opts.installAgentProxyHost ? opts.installAgentProxyHost : ""
        def tmpInstallAgentProxyPort = opts.installAgentProxyPort ? opts.installAgentProxyPort : ""

        int httpProxyPort = 0
        if (StringUtils.isNotBlank(tmpInstallAgentProxyPort)) {
            httpProxyPort = Integer.parseInt(tmpInstallAgentProxyPort)
        }

        if (StringUtils.isNotBlank(tmpInstallAgentProxyHost)) {
            builder.setProxyHost(tmpInstallAgentProxyHost)
        }
        if (httpProxyPort > 0) {
            builder.setProxyPort(httpProxyPort)
        }

        boolean trustAllCerts = !opts.installAgentVerifyServerIdentity
        builder.setTrustAllCerts(trustAllCerts)

        builder.setTimeoutMillis(10000)

        return builder.buildClient()
    }

    private def prepareForToolkitConfigUpgrade(AgentInstallOptions opts) {
        if (isZos) {
            ant.unzip(src:"zostoolkit.zip", overwrite:"true", dest:opts.installAgentConfigDir) {
                patternset {
                     include(name:"bin/buztool.sh");
                }
            }

            ant.replace(dir:opts.installAgentConfigDir, includes:"bin/buztool.sh") {
                replacefilter(token: "@AGENT_HOME@", value: opts.installAgentConfigDir)
                replacefilter(token: "@AGENT_BIN_HOME@", value: opts.installAgentBinaryDir)
                replacefilter(token: "@JAVA_HOME@", value: opts.javaHome)
            }

            ant.chmod(perm: "+x", type: "file") {
                fileset(dir: opts.installAgentConfigDir) {
                    include(name: "bin/buztool.sh")
                }
            }
            opts.zPrepareForUpgrade = true;
        }
    }

    private void collectZosDatasetInfomation(AgentInstallOptions opts) {
        if (isZos) {
            if (StringUtils.isNotBlank(opts.zTkMvshlq) && StringUtils.isBlank(opts.zTkMvsvolume)) {
                opts.zTkMvsvolume =  findVolume(opts.zTkMvshlq+".SBUZEXEC");
                if (opts.zTkMvsvolume == null) {
                    println "Failed to locate volume serial number for ${opts.zTkMvshlq}"
                }
            }

            opts.zTkMvshlq = prompt(opts.zTkMvshlq,
                "Enter the high-level qualifier to install product data sets. [Default: BUZ]","BUZ",requiredValidator)
            opts.zTkMvshlq = opts.zTkMvshlq.toUpperCase();

            opts.zTkMvsvolume = prompt(opts.zTkMvsvolume,
                            "Enter the volume serial to install product data sets.",null,requiredValidator)
            if (opts.zTkMvsvolume != null) {
                 opts.zTkMvsvolume = opts.zTkMvsvolume.trim().toUpperCase();
            }
        }
    }

    private void collectZosToolkitSettings(AgentInstallOptions opts) {
        if (isZos) {
            if (StringUtils.isNotBlank(opts.installAgentServerUrl) && StringUtils.isBlank(opts.zTkServerURL)) {
                opts.zTkServerURL = opts.installAgentServerUrl;
            }

            opts.zTkServerURL = prompt(opts.zTkServerURL,
                "Enter the URL of the UrbanCode Deploy server. [Default: https://${opts.installAgentRemoteHostList.get(0)}:8443/]","https://${opts.installAgentRemoteHostList.get(0)}:8443/",requiredValidator)

            opts.zTkServerToken = prompt(opts.zTkServerToken,
                "Enter the authentication token for the UrbanCode Deploy server. " +
                "You can modify the token later in installed.properties file. [Default: empty]","",null)

            def useCodestation = null
            if ( opts.zTkRepositoryType == "CODESTATION") {
                useCodestation = 'Y'
            }
            else if (opts.zTkRepositoryType == "HFS") {
                useCodestation = 'N'
            }
            if (parseBoolean( prompt(useCodestation,
                'Do you want to store versions in the UCD server CodeStation? ' +
                'If you choose No, versions will be stored in local directory. Y,n [Default: Y]',
                'Y',
                yesNoValidator))) {
                opts.zTkRepositoryType = "CODESTATION"

            }
            else {
                opts.zTkRepositoryType = "HFS"
            }
        }
    }

    private void copyZosToolkitBinaryFiles(AgentInstallOptions opts) {
        //copy the toolkit zip to the binary directory so we have the files for upgrading
        ant.copy(todir: opts.installAgentBinaryDir + "/native", failonerror: 'true', overwrite:"true") {
            fileset(dir: srcDir) {
                include(name: "zostoolkit.zip")
            }
        }
        ant.unzip(src:"zostoolkit.zip", overwrite:"true", dest:opts.installAgentBinaryDir) {
            patternset {
                 include(name:"mvs/**/*");
                 include(name:"lib/**/*");
                 include(name:"auth/checkaccess");
                 include(name:"conf/toolkit/installed.version");
            }
            mapper {
                globmapper(from:"conf/toolkit/installed.version", to:"conf/toolkit/binary.version");
            }
        }

        ant.copy(todir: opts.installAgentBinaryDir + "/conf", failonerror: 'false', overwrite:"true") {
            fileset(dir: srcDir) {
                include(name: "smpe.version")
            }
        }
        if (new File(opts.installAgentBinaryDir + "/conf/smpe.version").exists()) {
            opts.zTkSmpe = true
        }

        ant.exec(executable : "extattr", failonerror: "false") {
            arg(line : "+a ${opts.installAgentBinaryDir}/auth/checkaccess")
        }

        ant.chmod(perm: "+x", type: "file") {
            fileset(dir: opts.installAgentBinaryDir) {
                include(name: "auth/checkaccess")
            }
        }
    }

    private void fixZosToolkitPermissions(AgentInstallOptions opts) {
        ant.chmod(perm: "+x", type: "file") {
            fileset(dir: opts.installAgentConfigDir) {
                include(name: "bin/*.sh")
                include(name: "conf/toolkit/ISPZXENV")
            }
        }
        ant.chmod(perm: "g+w", type: "dir") {
            fileset(dir: opts.installAgentConfigDir) {
                include(name: "conf/agent")
                include(name: "var/deploy")
                include(name: "var/repository")
                include(name: "var/log/ispf")
                include(name: "var/log/ispf/WORKAREA")
            }
        }
    }

    private void tokenReplaceZosToolkit(AgentInstallOptions opts) {
        ant.replace(dir:opts.installAgentConfigDir, includes:"conf/toolkit/*,bin/*.sh,zossamples/**/*") {
            replacefilter(token: "@AGENT_HOME@", value: opts.installAgentConfigDir)
            replacefilter(token: "@AGENT_BIN_HOME@", value: opts.installAgentBinaryDir)
            replacefilter(token: "@JAVA_HOME@", value: opts.javaHome)
            replacefilter(token: "@HLQ@", value: opts.zTkMvshlq)
        }
    }

    private void copyZosToolkitConfigFiles(AgentInstallOptions opts) {
        ant.unzip(src:"zostoolkit.zip", overwrite:"true", dest:opts.installAgentConfigDir) {
            patternset {
                 include(name:"conf/**/*");
                 include(name:"bin/**/*");
                 include(name:"zossamples/**/*");
                 include(name:"var/**/*");
            }
        }
    }

    private void writeZosToolkitProperties(AgentInstallOptions opts) {
        ant.propertyfile(file: opts.installAgentConfigDir + "/conf/agent/installed.properties") {
            entry(key: "server.url", value: opts.zTkServerURL)
            entry(key: "server.token", value:
                   StringUtils.isNotBlank(opts.zTkServerToken) ? CryptStringUtil.encrypt(opts.zTkServerToken) : "")
            entry(key: "repository.type", value: opts.zTkRepositoryType)
        }
    }

    private void readInstalledPropertiesForZos(AgentInstallOptions opts) {
        if (isZos) {
            File file = new File(opts.installAgentBinaryDir, "mvs/dataset.properties");
            if (file.exists()) {
                ant.property(file: file.absolutePath)
            }
            file = new File(opts.installAgentBinaryDir, "conf/toolkit/installed.properties");
            if (file.exists()) {
                ant.property(file: file.absolutePath)
            }
            initInstalledProperties(opts);
        }
    }

    private void tokenReplaceForZosJob(AgentInstallOptions opts) {
        ant.replace(dir:opts.installAgentBinaryDir, includes:"mvs/BUZEXPD,mvs/dataset.properties") {
            replacefilter(token: "@HLQ@", value: opts.zTkMvshlq)
            if (opts.zTkMvsvolume !=null && opts.zTkMvsvolume != "") {
                replacefilter(token: "@TVOLSER@", value: opts.zTkMvsvolume)
            }
            replacefilter(token: "@PATH@", value: opts.installAgentBinaryDir + "/mvs")
        }
    }

    private boolean isZosToolkitInstalled(AgentInstallOptions opts) {
        return new File(opts.installAgentConfigDir + "/bin/buztool.sh").exists();
    }

    private void completeZosToolkitInstall(AgentInstallOptions opts) {
        if (isZos && (!opts.zSkipToolkitInstall)) {
            //run buztool.sh to autoupgrade config files
            if (opts.zPrepareForUpgrade) {
                def process = ["${opts.installAgentConfigDir}" + "/bin/buztool.sh","upgradeconfig"].execute()
                OutputStream outText = new ByteArrayOutputStream();
                process.waitForProcessOutput(outText, System.err);
                println outText.toString().trim();
                outText.close();
            }

            println "********************************************************************";
            def type = ant.project.properties['agent.install.type'];
            if(opts.installAgentType){
                type = opts.installAgentType;
            }
            if (!(type && type == "config")) {
                if (!opts.zTkSmpe) {
                    println "- To complete the installation, edit and submit following job to load the data sets. " +
                            "The JCL, except the job parameter, has already been customized based on your input.";
                    if (StringUtils.isBlank(opts.zTkMvsvolume)) {
                        println("Warning: volume serial number is missing from input, please edit the JCL. ");
                    }
                    println("submit ${opts.installAgentBinaryDir}/mvs/BUZEXPD");
                }
            }

            if (!(type && type == "binary")) {
                println ("- After completing the installation, you can test the version import using following script. " +
                    "A zOS component named MYCOMP must be created on UrbanCode Deploy server first.");
                println ("${opts.installAgentConfigDir}/bin/test-create-version.sh")
            }

            println "********************************************************************";
        }
    }

    public String findVolume(String pattern) {
        //CatalogSearch is only available in IBM zOS JDK. Make this dynamically loaded so
        //groovy won't report compilation error in other platforms.
        def catSearch= this.class.classLoader.loadClass("com.ibm.jzos.CatalogSearch").newInstance(pattern);
        catSearch.setEntryTypes(new String("A"));
        catSearch.addFieldName("ENTNAME");
        catSearch.addFieldName("VOLSER");
        catSearch.search();
        while(catSearch.hasNext()) {
            def entry=catSearch.next();
            if (entry.isDatasetEntry()) {
                def volume = entry.getField("VOLSER");
                return volume.getFString().trim();
            }
        }
        return null;
   }
}
