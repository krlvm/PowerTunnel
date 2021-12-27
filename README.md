<div align="center">
<img src="https://raw.githubusercontent.com/krlvm/PowerTunnel/master/.github/images/logo.png" height="192px" width="192px" />
<br><h1>PowerTunnel</h1>
Powerful and extensible proxy server

<a href="https://t.me/powertunnel_dpi">Telegram channel</a>
<br>
<a href="https://github.com/krlvm/PowerTunnel-Android">Check out the Android version!<a/>
<br><br>
<a href="https://github.com/krlvm/PowerTunnel/blob/master/LICENSE"><img src="https://img.shields.io/github/license/krlvm/PowerTunnel?style=flat-square" alt="License"/></a>
<a href="https://github.com/krlvm/PowerTunnel/releases/latest"><img src="https://img.shields.io/github/v/release/krlvm/PowerTunnel?style=flat-square" alt="Latest release"/></a>
<a href="https://github.com/krlvm/PowerTunnel/releases"><img src="https://img.shields.io/github/downloads/krlvm/PowerTunnel/total?style=flat-square" alt="Downloads"/></a>
<a href="https://github.com/krlvm/PowerTunnel/wiki"><img src="https://img.shields.io/badge/help-wiki-yellow?style=flat-square" alt="Help on the Wiki"/></a>
<br>
<img src="https://raw.githubusercontent.com/krlvm/PowerTunnel/master/.github/images/ui.png" alt="PowerTunnel User Interface" style="max-width: 90%; height: auto"/>
</div>

## What is it

PowerTunnel is an extensible proxy server built on top of [LittleProxy](https://github.com/adamfisk/LittleProxy).

PowerTunnel provides an SDK that allows you to extend its functionality however you like, and even handle encrypted HTTPS traffic (powered by [LittleProxy-MITM](https://github.com/ganskef/LittleProxy-mitm)), which can be especially useful in web development. PowerTunnel has an Android version, so any plugin you write can work on almost all devices.


PowerTunnel was originally developed and is best known as a censorship bypass tool. This functionality has been spun off in the [LibertyTunnel](https://github.com/krlvm/LibertyTunnel) plugin which is installed by default, just like [DNS Resolver](https://github.com/krlvm/PowerTunnel-DNS) with DNS over HTTPS support.

#### Anti-censorship tool

Digital censorship has become widespread in authoritarian and developing countries: governments install DPI - Deep Packet Inspection systems - for Internet Service Providers, which allows analyzing and blocking traffic to websites they don't want you to see, forcing you to use slow and often paid proxies or VPN services with dubious privacy policy.

PowerTunnel is an active DPI circumvention utility - it works only on your PC and do not route your traffic through some third-party webservers. It creates a local proxy server on your device and diverts your HTTP(S) traffic there, where PowerTunnel modifies your traffic in a special way to exploit bugs in DPI systems which makes it possible to bypass the lock - without (significantly) slowing down your Internet connection.

Anti-censorship module can be configured in Plugins window - it is called LibertyTunnel.

In this sense, PowerTunnel is a free cross-platform implementation of [GoodbyeDPI](https://github.com/ValdikSS/GoodbyeDPI) written in Java.

Please, note that PowerTunnel does not change your IP address.

## Configuring

### Downloading PowerTunnel

PowerTunnel binary can be downloaded from the [Releases](https://github.com/krlvm/PowerTunnel/releases) page.

If you don't trust the prebuilt binaries, you can build PowerTunnel from source - it is using Gradle build system.

### Launching PowerTunnel

PowerTunnel is a portable Java application, and it does not require additional steps to get it working.

You need to install [Java](https://java.com) to run PowerTunnel.

PowerTunnel can be started by double-clicking the executable .jar file or by starting it from command line ([see below](#launch-arguments)).

After the first launch, PowerTunnel will create directories for storing plugins and configuration files. 

### Installing plugins

To install a plugin, just place its .jar file into `plugins` directory.

Please, make sure you do not have installed different versions of the same plugin.

### Configuring plugins

Installed plugins can be configured from the user interface - click the "Plugins" button of main window to see the list of plugins.

## Launch arguments
You can specify some params that will override settings through CLI:

```
$ java -jar PowerTunnel.jar -help

    --auth-password <arg>            set proxy authorization password
    --auth-username <arg>            set proxy authorization username
    --auto-proxy-setup-ie            setup proxy using Internet Explorer
    --cfg <arg>                      set preference value
    --console                        run application in console mode
    --disable-auto-proxy-setup       disable auto proxy setup
    --disable-native-skin            disable platform native UI skin
    --disable-tray                   disable tray mode
    --disable-ui-scaling             disable UI scaling
    --disable-updater                disable Update Notifier
    --enable-logging                 enable logging to file
    --help                           display help
    --ip <arg>                       set proxy server IP address
    --lang <arg>                     set UI language
    --minimized                      minimize UI to tray after start
    --port <arg>                     set proxy server port
    --set-ui-scale-factor <arg>      set UI scale factor
    --start                          start proxy server after load
    --upstream-auth-username <arg>   set upstream proxy password
    --upstream-proxy-host <arg>      set upstream proxy host
    --upstream-proxy-port <arg>      set upstream proxy port
    --version                        print version details
```

If you need to set a certain plugin preference, use `-cfg pluginID.preferenceKey [value]`

## Bundled Plugins
* [LibertyTunnel](https://github.com/krlvm/LibertyTunnel) - anti-censorship plugin for PowerTunnel
* [DNS Resolver](https://github.com/krlvm/PowerTunnel-DNS) - DNS Resolver with DNS over HTTPS (DoH) support

## Dependencies
* [LittleProxy](https://github.com/adamfisk/LittleProxy) - proxy server, [forked version](https://github.com/mrog/LittleProxy)
* [LittleProxy-MITM](https://github.com/ganskef/LittleProxy-mitm) - LittleProxy SSL extension
* [dnsjava](https://github.com/dnsjava/dnsjava) - DNS and DoH library
* [dnssecjava](https://github.com/ibauersachs/dnssecjava) - DNSSec implementation for dnsjava
* [SLF4J](http://www.slf4j.org/) - logging facade API
* [Log4j](https://logging.apache.org/log4j/2.x/) - logger implementation
* [Java Native Access](https://github.com/java-native-access/jna) - accessing system native API
* [SwingDPI](https://github.com/krlvm/SwingDPI) - High DPI scaling