需要快速使用UniverseCore,你只需要在`build.gradle`中添加如下依赖项：

    dependencies {
      implementation "com.github.EB-wilson.UniverseCore:mindustry:$uncVersion"
      implementation "com.github.EB-wilson.UniverseCore:utilties:$uncVersion"
    }
      
这包含了主要的mod开发库，但更多时候你需要用到一些其他模块才能更好的使用，一组标准的导入语句如下：

    dependencies {
      implementation "com.github.EB-wilson.UniverseCore:mindustry:$uncVersion"//涵盖了大部分mod制作相关的工具
      implementation "com.github.EB-wilson.UniverseCore:utilties:$uncVersion"//实用工具集，包含了许多通用工具模块
      implementation "com.github.EB-wilson.UniverseCore:abstract:$uncVersion"//功能抽象层，一些平台相关的行为被抽象为接口，要使用平台相关API时必须导入该模块
      implementation "com.github.EB-wilson.UniverseCore:implabstract:$uncVersion"//中间实现层，有一些关于mod和加载器的功能
      implementation "com.github.EB-wilson.UniverseCore:dynamilizer:$uncVersion"//JDER动态化仓库的内置模块
      implementation "com.github.EB-wilson.UniverseCore:annotations:$uncVersion"//包括组件化接口在内的注解处理器
      
      annotationProcessor "com.github.EB-wilson.UniverseCore:annotations:$uncVersion"//使用注解处理器
                    
      //以下是通常不会用到的内部模块，但仍然允许导入，便于对一些较为复杂的需求进行自定义行为
      //implementation "com.github.EB-wilson.UniverseCore:core:$uncVersion"//仅有一个核心静态容器，保存了所有的功能接口的实现单例
      //implementation "com.github.EB-wilson.UniverseCore:android:$uncVersion"//平台相关的功能在安卓的默认实现
      //implementation "com.github.EB-wilson.UniverseCore:android26:$uncVersion"//在安卓API26以上的安卓平台运行时的实现
      //implementation "com.github.EB-wilson.UniverseCore:desktop:$uncVersion"//平台相关的功能在桌面的默认实现
      //implementation "com.github.EB-wilson.UniverseCore:desktop9:$uncVersion"//当桌面版运行时VM版本1.9以上的平台实现
    }

由于mod加载机制的问题，无法很好的控制mod的加载顺序，且为了保证mod可以正常运行，如果你不想自己编写加载控制，那么你需要导入并使用`annotations`模块（如前文所示），并在你的mod主类添加注解`@Annotations.ImportUNC`，下面是一个实例：

    @Annotations.ImportUnc(requireVersion = /*uncVersion*/)
    public class MyMod extends Mod{
      ...
    }

这么做之后，当你的mod启动时会检查该前置是否正确安装，如果前置缺失，游戏会弹出提示，指导玩家安装该前置。关于导入语句当中的`requireVersion`，这表示的是你的mod当前版本要求的最低UniverseCore版本的序列号，目前序列号是UNC版本号前两位的直接数字组合，后续可能会发生变动，不过不会影响兼容，无需担心

另外，如果你用于编译的JDK版本为14以上，那么你可能需要在使用了`annotations`模块的build.gradle文件中额外添加如下代码后才能正常编译：

    tasks.withType(JavaCompile){
      options.fork = true
      options.forkOptions.jvmArgs.addAll([
      "--add-opens", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
      "--add-opens", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
      "--add-opens", "jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
      "--add-opens", "jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
      "--add-opens", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
      "--add-opens", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
      ])
    }

（待施工）