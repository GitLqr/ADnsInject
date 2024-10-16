# Usage

[ADnsInject](https://github.com/GitLqr/ADnsInject) 是全局 dns 拦截注入库。该库特点：

- Java 层全局 dns 注入。
- 兼容低端 Android 设备（Android 4.x）。

问题：

- 暂对 webview 无效。
- 因为涉及到调用系统隐藏 api，如果工程没有系统签名，则需要使用 [FreeReflection](https://github.com/tiann/FreeReflection) 等第三方库进行豁免。

### 1、依赖

```groovy
implementation "com.github.GitLqr:ADnsInject:latest_version"
```

> latest_version：![Release Version](https://img.shields.io/github/v/release/GitLqr/ADnsInject.svg)

### 2、使用

- 启用/禁用：

```kotlin
ADnsInject.get().start()
ADnsInject.get().stop()
```

- 注入：

```kotlin
private val hostMap = hashMapOf("www.baidu.com" to "8.8.4.8")
ADnsInject.get().setHostMap(hostMap)
```

- 开启日志：

```kotlin
ADnsInject.get().setLoggerEnable(true)
```

# License

```
Copyright 2024 GitLqr

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

# About Me

![](https://github.com/LinXunFeng/LinXunFeng/raw/master/static/img/FSAQR.png)
