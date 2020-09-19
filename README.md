# Access Android Application

IOTA Android application is a client based smartphone application used to create Access Policies (as device owner) and Access Requests (as device user). Policies can be combined with attributes to create rules that affect resolution of the policy on the embedded device.

There are 4 hardcoded users:
- `alice`
- `bob`
- `carol`
- `dave`

all of them have `IOTApass1234` as password.

## Building instructions

1. Clone the repository:
```
git clone https://github.com/iotaledger/access-mobile-client.git
cd access-mobile-client
```

2. Open the project in Android Studio.

3. Select Build -> Build APK. This will build the Access Mobile Client [APK](https://en.wikipedia.org/wiki/Android_application_package) file.

##  Run instructions
1. In the android security settings select: 
```
Allow instalation of the apps from unknown sources
```
2. Copy generated APK to the Android phone using USB cable
3. From the file explorer of the Android smartphone device select Run file in order to install
