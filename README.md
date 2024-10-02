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
      compileOnly "com.github.EB-wilson.UniverseCore:markdown:$uncVersion"//markdown工具支持
      compileOnly "com.github.EB-wilson.UniverseCore:scenes:$uncVersion"//ui相关工具类型
      compileOnly "com.github.EB-wilson.UniverseCore:dynamilizer:$uncVersion"//动态化仓库，实现自JavaDynamilizer
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

----

#### 无依赖模块

部分模块无耦合，可直接使用`implementation`使用模块的功能并不需要添加mod依赖，所有无耦合的工具模块利用如下：

    dependencies {
      ...
      implementation "com.github.EB-wilson.UniverseCore:markdown:$uncVersion"//markdown工具支持
      implementation "com.github.EB-wilson.UniverseCore:scenes:$uncVersion"//ui相关工具类型
      ...
    }

此类模块使用将会把使用的模块打包到你的mod当中，不需要添加mod依赖，但是仅上给出的模块可以如此操作，其余核心模块大多具有平台相关的实现，模块结构不允许进行置入（未来可能会进行解构取消所有模块的平台依赖）

（待施工）
