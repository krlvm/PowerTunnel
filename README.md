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


PowerTunnel was originally developed and is best known as a censorship bypass tool. This functionality has been spun off in the [LibertyTunnel](https://github.com/krlvm/LibertyTunnel) plugin, it is installed by default, just like [DNS Resolver](https://github.com/krlvm/PowerTunnel-DNS) with DNS over HTTPS support.

Version 2.0 is currently in Beta, the SDK Documentation is coming soon.

### Launch arguments
You can specify some params that will override settings through CLI:

```
$ java -jar PowerTunnel.jar -help

    --auth-password <arg>            set proxy authorization password
    --auth-username <arg>            set proxy authorization username
    --auto-proxy-setup-ie            setup proxy using Internet Explorer
    --cfg <arg>                      set preference value
    --console                        run application in console mode
    --disable-auto-proxy-setup       disable auto proxy setup
    --disable-native-skin <arg>      disable platform native UI skin
    --disable-tray                   disable tray mode
    --disable-ui-scaling <arg>       disable UI scaling
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

## Dependencies
* [LittleProxy](https://github.com/adamfisk/LittleProxy) - proxy server, [forked version](https://github.com/mrog/LittleProxy)
* [LittleProxy-MITM](https://github.com/ganskef/LittleProxy-mitm) - LittleProxy SSL extension
* [dnsjava](https://github.com/dnsjava/dnsjava) - DNS and DoH library
* [dnssecjava](https://github.com/ibauersachs/dnssecjava) - DNSSec implementation for dnsjava
* [SLF4J](http://www.slf4j.org/) - logging facade API
* [Log4j](https://logging.apache.org/log4j/2.x/) - logger implementation
* [Java Native Access](https://github.com/java-native-access/jna) - accessing system native API
* [SwingDPI](https://github.com/krlvm/SwingDPI) - High DPI scaling