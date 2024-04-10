需要快速使用UniverseCore,你只需要在`build.gradle`中添加如下依赖项：

    dependencies {
      ...
      compileOnly "com.github.EB-wilson.UniverseCore:core:$uncVersion"
      ...
    }
      
这包含了主要的mod开发库，但更多时候你需要用到一些其他模块才能更好的使用，一组标准的导入语句如下：

    dependencies {
      ...
      compileOnly "com.github.EB-wilson.UniverseCore:core:$uncVersion"//涵盖了大部分mod制作相关的工具
      compileOnly "com.github.EB-wilson.UniverseCore:annotations:$uncVersion"//包括组件化接口在内的注解处理器
      
      annotationProcessor "com.github.EB-wilson.UniverseCore:annotations:$uncVersion"//使用注解处理器
                    
      //以下是通常不会用到的内部模块，但仍然允许导入，便于对一些较为复杂的需求进行自定义行为
      //compileOnly "com.github.EB-wilson.UniverseCore:android:$uncVersion"//平台相关的功能在安卓的默认实现
      //compileOnly "com.github.EB-wilson.UniverseCore:android26:$uncVersion"//在安卓API26以上的安卓平台运行时的实现
      //compileOnly "com.github.EB-wilson.UniverseCore:desktop:$uncVersion"//平台相关的功能在桌面的默认实现
      //compileOnly "com.github.EB-wilson.UniverseCore:desktop9:$uncVersion"//当桌面版运行时VM版本1.9以上的平台实现
      ...
    }

由于mod加载机制的问题，无法很好的控制mod的加载顺序，且为了保证mod可以正常运行，如果你不想自己编写加载控制，那么你需要导入并使用`annotations`模块（如前文所示），并在你的mod主类添加注解`@Annotations.ImportUNC`，下面是一个实例：

    @Annotations.ImportUnc(requireVersion = /*uncVersion*/)
    public class MyMod extends Mod{
      ...
    }

这么做之后，当你的mod启动时会检查该前置是否正确安装，如果前置缺失，游戏会弹出提示，指导玩家安装该前置。关于导入语句当中的`requireVersion`，这表示的是你的mod当前版本要求的最低UniverseCore版本号

（待施工）