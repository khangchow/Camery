Hello, welcome to Camery - my first public library so there should be a lot of mistakes and bugs >.< . So please feel free to create you MR if you have any advise or contribution for Camery. Thank you <3

Here are steps to import Camery into your own project:

1/ Add "maven { url 'https://jitpack.io' }" into your settings.gradle

2/ Add "implementation 'com.github.khangchow:Camery:lastest_version'" into your build.gradle

3/ How to use Camery's camera feature:

- Register to get image uri 

```
private val imageLauncher = registerImageLauncher {
        if (it == null) return@registerImageLauncher
        // Do whatever you want will the uri
    }
```

- Launch the camera

```
imageLauncher.launch(CameraConfig())
```
